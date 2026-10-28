package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "BANK", "CASH", "EWALLET", "OTHER"
    val colorHex: String,
    val iconName: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "type"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "EXPENSE", "INCOME"
    val iconName: String,
    val colorHex: String
)

@Entity(
    tableName = "transactions",
    indices = [
        Index("dateMillis"),
        Index("sourceAccountId"),
        Index("destinationAccountId"),
        Index("categoryId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME", "EXPENSE", "TRANSFER", "CORRECTION"
    val amount: Long, // Nominal dalam Rupiah integer
    val categoryId: Long? = null,
    val sourceAccountId: Long? = null,
    val destinationAccountId: Long? = null,
    val description: String,
    val notes: String? = null,
    val dateMillis: Long,
    val classification: String = "PERSONAL", // "PERSONAL", "TITIPAN_KEWAJIBAN", "UNSPECIFIED"
    val status: String = "CONFIRMED", // "CONFIRMED", "DRAFT", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ledger_entries",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("transactionId"),
        Index("accountId"),
        Index("timestamp")
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val accountId: Long,
    val direction: String, // "INFLOW", "OUTFLOW"
    val amount: Long, // Nominal positif
    val entryType: String, // "OPENING_BALANCE", "OPERATIONAL", "TRANSFER", "DEBT_PAYMENT", "FUND_ALLOCATION", "CORRECTION"
    val timestamp: Long
)

@Entity(tableName = "fund_pots")
data class FundPotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Long,
    val currentAmount: Long = 0L,
    val colorHex: String,
    val iconName: String,
    val targetDateMillis: Long? = null,
    val notes: String? = null,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "fund_movements",
    foreignKeys = [
        ForeignKey(
            entity = FundPotEntity::class,
            parentColumns = ["id"],
            childColumns = ["fundPotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fundPotId")]
)
data class FundMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fundPotId: Long,
    val amount: Long,
    val type: String, // "ALLOCATION", "WITHDRAWAL"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "UTANG" (Saya berutang), "PIUTANG" (Orang berutang ke saya)
    val personName: String,
    val amount: Long, // Pokok utang
    val paidAmount: Long = 0L,
    val dueDateMillis: Long? = null,
    val notes: String? = null,
    val status: String = "ACTIVE", // "ACTIVE", "PAID_OFF"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "debt_payments",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("debtId"), Index("accountId")]
)
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val accountId: Long,
    val amount: Long,
    val dateMillis: Long,
    val notes: String? = null
)

@Entity(
    tableName = "transaction_attachments",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class AttachmentEntity(
    @PrimaryKey val id: String, // UUID
    val transactionId: Long,
    val relativePath: String,
    val thumbnailRelativePath: String,
    val purpose: String, // "RECEIPT", "CONTEXT_PHOTO"
    val mimeType: String = "image/jpeg",
    val width: Int,
    val height: Int,
    val byteSize: Long,
    val createdAt: Long = System.currentTimeMillis()
)
