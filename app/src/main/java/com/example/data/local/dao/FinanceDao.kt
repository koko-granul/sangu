package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface FinanceDao {

    // Accounts
    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY id ASC")
    fun getAllActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    // Ledger Entries
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getLedgerEntriesForAccount(accountId: Long): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<LedgerEntryEntity>)

    @Query("DELETE FROM ledger_entries WHERE transactionId = :transactionId")
    suspend fun deleteLedgerEntriesForTransaction(transactionId: Long)

    // Transactions
    @Query("SELECT * FROM transactions WHERE status != 'CANCELLED' ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status != 'CANCELLED' ORDER BY dateMillis DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = 'CANCELLED' WHERE id = :transactionId")
    suspend fun cancelTransaction(transactionId: Long)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Categories
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    // Fund Pots
    @Query("SELECT * FROM fund_pots ORDER BY id ASC")
    fun getAllFundPots(): Flow<List<FundPotEntity>>

    @Query("SELECT * FROM fund_pots WHERE id = :id LIMIT 1")
    suspend fun getFundPotById(id: Long): FundPotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFundPot(pot: FundPotEntity): Long

    @Update
    suspend fun updateFundPot(pot: FundPotEntity)

    @Delete
    suspend fun deleteFundPot(pot: FundPotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFundMovement(movement: FundMovementEntity): Long

    @Query("SELECT * FROM fund_movements WHERE fundPotId = :potId ORDER BY timestamp DESC")
    fun getFundMovements(potId: Long): Flow<List<FundMovementEntity>>

    // Debts & Receivables
    @Query("SELECT * FROM debts ORDER BY status ASC, createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPayment(payment: DebtPaymentEntity): Long

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY dateMillis DESC")
    fun getDebtPayments(debtId: Long): Flow<List<DebtPaymentEntity>>

    // Attachments
    @Query("SELECT * FROM transaction_attachments WHERE transactionId = :transactionId")
    fun getAttachmentsForTransaction(transactionId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM transaction_attachments ORDER BY createdAt DESC")
    fun getAllAttachments(): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Query("DELETE FROM transaction_attachments WHERE id = :attachmentId")
    suspend fun deleteAttachment(attachmentId: String)
}
