package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.FinanceDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.FundMovementEntity
import com.example.data.local.entity.FundPotEntity
import com.example.data.local.entity.LedgerEntryEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val database: AppDatabase,
    private val dao: FinanceDao
) {
    val allActiveAccounts: Flow<List<AccountEntity>> = dao.getAllActiveAccounts()
    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    val allLedgerEntries: Flow<List<LedgerEntryEntity>> = dao.getAllLedgerEntries()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allFundPots: Flow<List<FundPotEntity>> = dao.getAllFundPots()
    val allDebts: Flow<List<DebtEntity>> = dao.getAllDebts()
    val allAttachments: Flow<List<AttachmentEntity>> = dao.getAllAttachments()

    suspend fun recordExpense(
        accountId: Long,
        amount: Long,
        categoryId: Long?,
        description: String,
        notes: String?,
        dateMillis: Long,
        classification: String = "PERSONAL",
        attachments: List<AttachmentEntity> = emptyList()
    ): Long = database.withTransaction {
        val txId = dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                amount = amount,
                categoryId = categoryId,
                sourceAccountId = accountId,
                destinationAccountId = null,
                description = description,
                notes = notes,
                dateMillis = dateMillis,
                classification = classification,
                status = "CONFIRMED"
            )
        )

        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = accountId,
                direction = "OUTFLOW",
                amount = amount,
                entryType = "OPERATIONAL",
                timestamp = dateMillis
            )
        )

        attachments.forEach { att ->
            dao.insertAttachment(att.copy(transactionId = txId))
        }

        txId
    }

    suspend fun recordIncome(
        accountId: Long,
        amount: Long,
        categoryId: Long?,
        description: String,
        notes: String?,
        dateMillis: Long,
        classification: String = "PERSONAL",
        attachments: List<AttachmentEntity> = emptyList()
    ): Long = database.withTransaction {
        val txId = dao.insertTransaction(
            TransactionEntity(
                type = "INCOME",
                amount = amount,
                categoryId = categoryId,
                sourceAccountId = null,
                destinationAccountId = accountId,
                description = description,
                notes = notes,
                dateMillis = dateMillis,
                classification = classification,
                status = "CONFIRMED"
            )
        )

        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = accountId,
                direction = "INFLOW",
                amount = amount,
                entryType = "OPERATIONAL",
                timestamp = dateMillis
            )
        )

        attachments.forEach { att ->
            dao.insertAttachment(att.copy(transactionId = txId))
        }

        txId
    }

    suspend fun recordTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        description: String,
        notes: String?,
        dateMillis: Long
    ): Long = database.withTransaction {
        require(fromAccountId != toAccountId) { "Akun asal dan akun tujuan tidak boleh sama" }

        val txId = dao.insertTransaction(
            TransactionEntity(
                type = "TRANSFER",
                amount = amount,
                categoryId = null,
                sourceAccountId = fromAccountId,
                destinationAccountId = toAccountId,
                description = description.ifBlank { "Transfer Antarakun" },
                notes = notes,
                dateMillis = dateMillis,
                classification = "PERSONAL",
                status = "CONFIRMED"
            )
        )

        // Balanced ledger entries
        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = fromAccountId,
                direction = "OUTFLOW",
                amount = amount,
                entryType = "TRANSFER",
                timestamp = dateMillis
            )
        )

        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = toAccountId,
                direction = "INFLOW",
                amount = amount,
                entryType = "TRANSFER",
                timestamp = dateMillis
            )
        )

        txId
    }

    suspend fun createAccountWithBalance(
        name: String,
        type: String,
        colorHex: String,
        iconName: String,
        initialBalance: Long
    ): Long = database.withTransaction {
        val accountId = dao.insertAccount(
            AccountEntity(
                name = name,
                type = type,
                colorHex = colorHex,
                iconName = iconName,
                isActive = true
            )
        )

        if (initialBalance > 0) {
            val now = System.currentTimeMillis()
            val txId = dao.insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    amount = initialBalance,
                    destinationAccountId = accountId,
                    description = "Saldo Awal $name",
                    dateMillis = now,
                    status = "CONFIRMED"
                )
            )

            dao.insertLedgerEntry(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountId = accountId,
                    direction = "INFLOW",
                    amount = initialBalance,
                    entryType = "OPENING_BALANCE",
                    timestamp = now
                )
            )
        }

        accountId
    }

    suspend fun deleteTransaction(txId: Long) = database.withTransaction {
        dao.deleteLedgerEntriesForTransaction(txId)
        val tx = dao.getTransactionById(txId)
        if (tx != null) {
            dao.deleteTransaction(tx)
        }
    }

    suspend fun addFundPot(
        title: String,
        targetAmount: Long,
        colorHex: String,
        iconName: String,
        notes: String?,
        imagePath: String? = null
    ): Long = dao.insertFundPot(
        FundPotEntity(
            title = title,
            targetAmount = targetAmount,
            currentAmount = 0L,
            colorHex = colorHex,
            iconName = iconName,
            notes = notes,
            imagePath = imagePath
        )
    )

    suspend fun allocateToFundPot(
        potId: Long,
        sourceAccountId: Long,
        amount: Long,
        notes: String?
    ) = database.withTransaction {
        val pot = dao.getFundPotById(potId) ?: return@withTransaction
        val newCurrent = pot.currentAmount + amount
        dao.updateFundPot(pot.copy(currentAmount = newCurrent))

        val now = System.currentTimeMillis()
        dao.insertFundMovement(
            FundMovementEntity(
                fundPotId = potId,
                amount = amount,
                type = "ALLOCATION",
                timestamp = now,
                notes = notes
            )
        )

        val txId = dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                amount = amount,
                sourceAccountId = sourceAccountId,
                description = "Alokasi Pos: ${pot.title}",
                notes = notes,
                dateMillis = now,
                classification = "PERSONAL",
                status = "CONFIRMED"
            )
        )

        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = sourceAccountId,
                direction = "OUTFLOW",
                amount = amount,
                entryType = "FUND_ALLOCATION",
                timestamp = now
            )
        )
    }

    suspend fun updateAccount(account: AccountEntity) {
        dao.updateAccount(account)
    }

    suspend fun deleteAccount(accountId: Long) = database.withTransaction {
        val account = dao.getAccountById(accountId) ?: return@withTransaction
        dao.deleteAccount(account)
    }

    suspend fun deleteAccountAndTransferBalance(
        sourceAccountId: Long,
        targetAccountId: Long,
        balance: Long
    ) = database.withTransaction {
        if (balance > 0L) {
            val now = System.currentTimeMillis()
            val txId = dao.insertTransaction(
                TransactionEntity(
                    type = "TRANSFER",
                    amount = balance,
                    sourceAccountId = sourceAccountId,
                    destinationAccountId = targetAccountId,
                    description = "Pengalihan Saldo Sebelum Hapus Akun",
                    dateMillis = now,
                    classification = "PERSONAL",
                    status = "CONFIRMED"
                )
            )
            dao.insertLedgerEntry(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountId = sourceAccountId,
                    direction = "OUTFLOW",
                    amount = balance,
                    entryType = "TRANSFER",
                    timestamp = now
                )
            )
            dao.insertLedgerEntry(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountId = targetAccountId,
                    direction = "INFLOW",
                    amount = balance,
                    entryType = "TRANSFER",
                    timestamp = now
                )
            )
        }
        val account = dao.getAccountById(sourceAccountId) ?: return@withTransaction
        dao.deleteAccount(account)
    }

    suspend fun deleteFundPot(potId: Long) = database.withTransaction {
        val pot = dao.getFundPotById(potId) ?: return@withTransaction
        dao.deleteFundPot(pot)
    }

    suspend fun updateFundPot(pot: FundPotEntity) {
        dao.updateFundPot(pot)
    }

    suspend fun withdrawFromFundPot(
        potId: Long,
        destinationAccountId: Long,
        amount: Long,
        notes: String?
    ) = database.withTransaction {
        val pot = dao.getFundPotById(potId) ?: return@withTransaction
        val newCurrent = (pot.currentAmount - amount).coerceAtLeast(0L)
        dao.updateFundPot(pot.copy(currentAmount = newCurrent))

        val now = System.currentTimeMillis()
        dao.insertFundMovement(
            FundMovementEntity(
                fundPotId = potId,
                amount = amount,
                type = "WITHDRAWAL",
                timestamp = now,
                notes = notes
            )
        )

        val txId = dao.insertTransaction(
            TransactionEntity(
                type = "INCOME",
                amount = amount,
                destinationAccountId = destinationAccountId,
                description = "Pencairan Pos: ${pot.title}",
                notes = notes,
                dateMillis = now,
                classification = "PERSONAL",
                status = "CONFIRMED"
            )
        )

        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = destinationAccountId,
                direction = "INFLOW",
                amount = amount,
                entryType = "FUND_ALLOCATION",
                timestamp = now
            )
        )
    }

    suspend fun spendFromFundPot(
        potId: Long,
        accountId: Long,
        amount: Long,
        categoryId: Long?,
        description: String,
        notes: String?
    ) = database.withTransaction {
        val pot = dao.getFundPotById(potId) ?: return@withTransaction
        val newCurrent = (pot.currentAmount - amount).coerceAtLeast(0L)
        dao.updateFundPot(pot.copy(currentAmount = newCurrent))

        val now = System.currentTimeMillis()
        dao.insertFundMovement(
            FundMovementEntity(
                fundPotId = potId,
                amount = amount,
                type = "WITHDRAWAL",
                timestamp = now,
                notes = "Pengeluaran langsung: $description"
            )
        )

        // Catat pengeluaran langsung
        val txId = dao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                amount = amount,
                categoryId = categoryId,
                sourceAccountId = accountId,
                destinationAccountId = null,
                description = description,
                notes = notes ?: "Pengeluaran dari Pos: ${pot.title}",
                dateMillis = now,
                classification = "PERSONAL",
                status = "CONFIRMED"
            )
        )

        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = accountId,
                direction = "OUTFLOW",
                amount = amount,
                entryType = "OPERATIONAL",
                timestamp = now
            )
        )
    }

    suspend fun deleteDebt(debtId: Long) = database.withTransaction {
        val debt = dao.getDebtById(debtId) ?: return@withTransaction
        dao.deleteDebt(debt)
    }

    suspend fun addDebt(
        type: String, // "UTANG" or "PIUTANG"
        personName: String,
        amount: Long,
        dueDateMillis: Long?,
        notes: String?
    ): Long = dao.insertDebt(
        DebtEntity(
            type = type,
            personName = personName,
            amount = amount,
            paidAmount = 0L,
            dueDateMillis = dueDateMillis,
            notes = notes,
            status = "ACTIVE"
        )
    )

    suspend fun payDebt(
        debtId: Long,
        accountId: Long,
        amount: Long,
        dateMillis: Long,
        notes: String?
    ) = database.withTransaction {
        val debt = dao.getDebtById(debtId) ?: return@withTransaction
        val newPaid = debt.paidAmount + amount
        val newStatus = if (newPaid >= debt.amount) "PAID_OFF" else "ACTIVE"
        dao.updateDebt(debt.copy(paidAmount = newPaid, status = newStatus))

        dao.insertDebtPayment(
            DebtPaymentEntity(
                debtId = debtId,
                accountId = accountId,
                amount = amount,
                dateMillis = dateMillis,
                notes = notes
            )
        )

        val txType = if (debt.type == "UTANG") "EXPENSE" else "INCOME"
        val desc = if (debt.type == "UTANG") "Bayar Utang: ${debt.personName}" else "Terima Piutang: ${debt.personName}"

        val txId = dao.insertTransaction(
            TransactionEntity(
                type = txType,
                amount = amount,
                sourceAccountId = if (debt.type == "UTANG") accountId else null,
                destinationAccountId = if (debt.type == "PIUTANG") accountId else null,
                description = desc,
                notes = notes,
                dateMillis = dateMillis,
                classification = "TITIPAN_KEWAJIBAN",
                status = "CONFIRMED"
            )
        )

        dao.insertLedgerEntry(
            LedgerEntryEntity(
                transactionId = txId,
                accountId = accountId,
                direction = if (debt.type == "UTANG") "OUTFLOW" else "INFLOW",
                amount = amount,
                entryType = "DEBT_PAYMENT",
                timestamp = dateMillis
            )
        )
    }

    suspend fun addCategory(name: String, type: String, iconName: String, colorHex: String): Long {
        return dao.insertCategory(
            CategoryEntity(name = name, type = type, iconName = iconName, colorHex = colorHex)
        )
    }
}
