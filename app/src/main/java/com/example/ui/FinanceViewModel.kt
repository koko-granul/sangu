package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.media.MediaManager
import com.example.core.media.ReceiptOcrScanner
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FundPotEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.domain.engine.FinanceEngine
import com.example.domain.model.AccountWithBalance
import com.example.domain.model.CashFlowSummary
import com.example.domain.model.CategoryDistribution
import com.example.domain.model.LocalInsight
import com.example.domain.model.ReceiptScanDraft
import com.example.domain.model.SavingsForecast
import com.example.domain.model.TransactionDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import com.example.core.settings.AppSettingsManager
import java.util.Calendar

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    val settingsManager = AppSettingsManager(application)
    private val database = AppDatabase.getInstance(application)
    private val dao = database.financeDao()
    val repository = FinanceRepository(database, dao)
    val mediaManager = MediaManager(application)
    private val ocrScanner = ReceiptOcrScanner(application)

    // Privacy toggle state (sembunyikan / tampilkan nominal saldo)
    private val _isAmountHidden = MutableStateFlow(false)
    val isAmountHidden: StateFlow<Boolean> = _isAmountHidden.asStateFlow()

    fun toggleAmountVisibility() {
        _isAmountHidden.value = !_isAmountHidden.value
    }

    // Interval filter untuk Analisis: 0 = Bulan ini, 1 = Minggu ini, 2 = Semua
    private val _analyticsPeriodIndex = MutableStateFlow(0)
    val analyticsPeriodIndex: StateFlow<Int> = _analyticsPeriodIndex.asStateFlow()

    fun setAnalyticsPeriod(index: Int) {
        _analyticsPeriodIndex.value = index
    }

    // Accounts with reactive balances calculated purely from ledger entries
    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = combine(
        repository.allAccounts,
        repository.allLedgerEntries
    ) { accounts, entries ->
        FinanceEngine.computeAccountBalances(accounts, entries)
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All categories
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All fund pots
    val fundPots: StateFlow<List<FundPotEntity>> = repository.allFundPots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All debts
    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions with joined details & attachments (Offloaded to background dispatcher)
    val transactions: StateFlow<List<TransactionDetail>> = combine(
        repository.allTransactions,
        repository.allCategories,
        repository.allAccounts,
        repository.allAttachments
    ) { txList, catList, accList, attList ->
        val catMap = catList.associateBy { it.id }
        val accMap = accList.associateBy { it.id }
        val attMap = attList.groupBy { it.transactionId }

        txList.map { tx ->
            TransactionDetail(
                id = tx.id,
                type = tx.type,
                amount = tx.amount,
                category = catMap[tx.categoryId],
                sourceAccount = accMap[tx.sourceAccountId],
                destinationAccount = accMap[tx.destinationAccountId],
                description = tx.description,
                notes = tx.notes,
                dateMillis = tx.dateMillis,
                classification = tx.classification,
                status = tx.status,
                attachments = attMap[tx.id] ?: emptyList()
            )
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cash flow summary calculated from FinanceEngine
    val cashFlowSummary: StateFlow<CashFlowSummary> = combine(
        accountsWithBalances,
        repository.allTransactions,
        _analyticsPeriodIndex
    ) { accBalances, txList, periodIndex ->
        val (start, end) = getPeriodRange(periodIndex)
        FinanceEngine.computeCashFlowSummary(accBalances, txList, start, end)
    }.flowOn(Dispatchers.Default)
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CashFlowSummary(0L, 0L, 0L, 0L, 0L, emptyList())
    )

    // Category distribution for expenses
    val categoryDistribution: StateFlow<List<CategoryDistribution>> = combine(
        repository.allTransactions,
        repository.allCategories,
        _analyticsPeriodIndex
    ) { txList, catList, periodIndex ->
        val (start, end) = getPeriodRange(periodIndex)
        FinanceEngine.computeCategoryDistribution(txList, catList, start, end)
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Savings forecast
    val savingsForecasts: StateFlow<List<SavingsForecast>> = combine(
        fundPots,
        cashFlowSummary
    ) { pots, summary ->
        FinanceEngine.computeSavingsForecast(pots, summary.netSurplus.coerceAtLeast(0L))
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Local Insights
    val localInsights: StateFlow<List<LocalInsight>> = combine(
        accountsWithBalances,
        repository.allTransactions,
        categories,
        fundPots,
        debts
    ) { accList, txList, catList, potList, debtList ->
        FinanceEngine.generateLocalInsights(accList, txList, catList, potList, debtList)
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Action Methods ---

    fun recordExpense(
        accountId: Long,
        amount: Long,
        categoryId: Long?,
        description: String,
        notes: String?,
        dateMillis: Long,
        classification: String = "PERSONAL",
        attachments: List<AttachmentEntity> = emptyList(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordExpense(
                accountId = accountId,
                amount = amount,
                categoryId = categoryId,
                description = description,
                notes = notes,
                dateMillis = dateMillis,
                classification = classification,
                attachments = attachments
            )
            onSuccess()
        }
    }

    fun recordIncome(
        accountId: Long,
        amount: Long,
        categoryId: Long?,
        description: String,
        notes: String?,
        dateMillis: Long,
        classification: String = "PERSONAL",
        attachments: List<AttachmentEntity> = emptyList(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordIncome(
                accountId = accountId,
                amount = amount,
                categoryId = categoryId,
                description = description,
                notes = notes,
                dateMillis = dateMillis,
                classification = classification,
                attachments = attachments
            )
            onSuccess()
        }
    }

    fun recordTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        description: String,
        notes: String?,
        dateMillis: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (fromAccountId == toAccountId) {
            onError("Akun sumber dan akun tujuan tidak boleh sama.")
            return
        }
        viewModelScope.launch {
            try {
                repository.recordTransfer(
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    description = description,
                    notes = notes,
                    dateMillis = dateMillis
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Gagal memproses transfer.")
            }
        }
    }

    fun createAccount(
        name: String,
        type: String,
        colorHex: String,
        iconName: String,
        initialBalance: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.createAccountWithBalance(name, type, colorHex, iconName, initialBalance)
            onSuccess()
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
        }
    }

    fun addFundPot(
        title: String,
        targetAmount: Long,
        colorHex: String,
        iconName: String,
        notes: String?,
        imagePath: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addFundPot(title, targetAmount, colorHex, iconName, notes, imagePath)
            onSuccess()
        }
    }

    fun allocateToFundPot(
        potId: Long,
        sourceAccountId: Long,
        amount: Long,
        notes: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.allocateToFundPot(potId, sourceAccountId, amount, notes)
            onSuccess()
        }
    }

    fun deleteAccount(accountId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
            onSuccess()
        }
    }

    fun deleteAccountAndTransfer(
        sourceAccountId: Long,
        targetAccountId: Long,
        balance: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.deleteAccountAndTransferBalance(sourceAccountId, targetAccountId, balance)
            onSuccess()
        }
    }

    fun spendFromFundPot(
        potId: Long,
        accountId: Long,
        amount: Long,
        categoryId: Long?,
        description: String,
        notes: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.spendFromFundPot(potId, accountId, amount, categoryId, description, notes)
            onSuccess()
        }
    }

    fun updateAccount(account: AccountEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateAccount(account)
            onSuccess()
        }
    }

    fun deleteFundPot(potId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteFundPot(potId)
            onSuccess()
        }
    }

    fun updateFundPot(pot: FundPotEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateFundPot(pot)
            onSuccess()
        }
    }

    fun withdrawFromFundPot(
        potId: Long,
        destinationAccountId: Long,
        amount: Long,
        notes: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.withdrawFromFundPot(potId, destinationAccountId, amount, notes)
            onSuccess()
        }
    }

    fun deleteDebt(debtId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteDebt(debtId)
            onSuccess()
        }
    }

    fun addDebt(
        type: String,
        personName: String,
        amount: Long,
        dueDateMillis: Long?,
        notes: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addDebt(type, personName, amount, dueDateMillis, notes)
            onSuccess()
        }
    }

    fun payDebt(
        debtId: Long,
        accountId: Long,
        amount: Long,
        dateMillis: Long,
        notes: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.payDebt(debtId, accountId, amount, dateMillis, notes)
            onSuccess()
        }
    }

    suspend fun scanReceipt(uri: Uri): ReceiptScanDraft {
        return ocrScanner.scanReceipt(uri)
    }

    suspend fun processImageAttachment(uri: Uri, purpose: String): AttachmentEntity {
        return mediaManager.processAndSaveImage(uri, purpose)
    }

    fun saveUserProfileImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val att = mediaManager.processAndSaveImage(uri, "PROFILE")
                val file = mediaManager.getFileFromRelativePath(att.relativePath)
                settingsManager.setUserProfileUri(file.absolutePath)
            } catch (_: Exception) {
                settingsManager.setUserProfileUri(uri.toString())
            }
        }
    }

    /**
     * Ekspor Data Lokal ke string JSON terstruktur
     */
    suspend fun exportDataJson(): String {
        val root = JSONObject()
        val accArray = JSONArray()
        accountsWithBalances.value.forEach { acc ->
            val obj = JSONObject()
            obj.put("id", acc.account.id)
            obj.put("name", acc.account.name)
            obj.put("type", acc.account.type)
            obj.put("balance", acc.balance)
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        val txArray = JSONArray()
        transactions.value.forEach { tx ->
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("type", tx.type)
            obj.put("amount", tx.amount)
            obj.put("description", tx.description)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("classification", tx.classification)
            obj.put("category", tx.category?.name ?: "")
            txArray.put(obj)
        }
        root.put("transactions", txArray)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("app", "Sangu Offline v1")
        return root.toString(2)
    }

    private fun getPeriodRange(periodIndex: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (periodIndex) {
            0 -> {
                // Bulan Ini
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            1 -> {
                // Minggu Ini
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            else -> {
                // Semua waktu
                Pair(0L, now)
            }
        }
    }
}
