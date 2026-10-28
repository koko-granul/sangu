package com.example.domain.model

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.CategoryEntity

data class AccountWithBalance(
    val account: AccountEntity,
    val balance: Long
)

data class TransactionDetail(
    val id: Long,
    val type: String, // "INCOME", "EXPENSE", "TRANSFER", "CORRECTION"
    val amount: Long,
    val category: CategoryEntity?,
    val sourceAccount: AccountEntity?,
    val destinationAccount: AccountEntity?,
    val description: String,
    val notes: String?,
    val dateMillis: Long,
    val classification: String,
    val status: String,
    val attachments: List<AttachmentEntity> = emptyList()
)

data class CashFlowSummary(
    val totalNetWorth: Long,
    val totalIncome: Long,
    val totalExpense: Long,
    val netSurplus: Long,
    val dailyAverageExpense: Long,
    val accounts: List<AccountWithBalance>
)

data class CategoryDistribution(
    val categoryName: String,
    val colorHex: String,
    val totalAmount: Long,
    val percentage: Float
)

data class SavingsForecast(
    val potId: Long,
    val title: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val remainingAmount: Long,
    val progress: Float,
    val estimatedMonthsLeft: Double?,
    val statusText: String,
    val imagePath: String? = null
)

enum class InsightSeverity {
    INFO, SUCCESS, WARNING, ALERT
}

data class LocalInsight(
    val id: String,
    val title: String,
    val description: String,
    val severity: InsightSeverity,
    val basis: String
)

data class ReceiptScanDraft(
    val merchant: String?,
    val dateMillis: Long?,
    val totalAmount: Long?,
    val subtotalAmount: Long?,
    val rawText: String
)
