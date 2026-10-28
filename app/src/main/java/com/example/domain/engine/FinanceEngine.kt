package com.example.domain.engine

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FundPotEntity
import com.example.data.local.entity.LedgerEntryEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.AccountWithBalance
import com.example.domain.model.CashFlowSummary
import com.example.domain.model.CategoryDistribution
import com.example.domain.model.InsightSeverity
import com.example.domain.model.LocalInsight
import com.example.domain.model.SavingsForecast
import java.util.Calendar

object FinanceEngine {

    /**
     * Menghitung saldo setiap akun dari seluruh ledger entries akun tersebut.
     * Saldo = Total Inflow - Total Outflow
     * Menggunakan single-pass hash map tanpa alokasi intermediate grouping lists.
     */
    fun computeAccountBalances(
        accounts: List<AccountEntity>,
        ledgerEntries: List<LedgerEntryEntity>
    ): List<AccountWithBalance> {
        val balanceMap = mutableMapOf<Long, Long>()
        for (i in 0 until ledgerEntries.size) {
            val entry = ledgerEntries[i]
            val current = balanceMap[entry.accountId] ?: 0L
            if (entry.direction == "INFLOW") {
                balanceMap[entry.accountId] = current + entry.amount
            } else if (entry.direction == "OUTFLOW") {
                balanceMap[entry.accountId] = current - entry.amount
            }
        }

        return accounts.map { account ->
            AccountWithBalance(account = account, balance = balanceMap[account.id] ?: 0L)
        }
    }

    /**
     * Menghitung total kekayaan bersih (Net Worth) dari semua akun aktif.
     */
    fun computeTotalNetWorth(accountBalances: List<AccountWithBalance>): Long {
        var total = 0L
        for (i in 0 until accountBalances.size) {
            val acc = accountBalances[i]
            if (acc.account.isActive) {
                total += acc.balance
            }
        }
        return total
    }

    /**
     * Menghitung ringkasan arus kas (Cash Flow) operasional dalam rentang waktu tertentu.
     * Single-pass traversal tanpa alokasi list filter.
     */
    fun computeCashFlowSummary(
        accounts: List<AccountWithBalance>,
        transactions: List<TransactionEntity>,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): CashFlowSummary {
        val totalNetWorth = computeTotalNetWorth(accounts)

        var totalIncome = 0L
        var totalExpense = 0L

        for (i in 0 until transactions.size) {
            val tx = transactions[i]
            if (tx.status != "CONFIRMED" || tx.dateMillis !in startTimeMillis..endTimeMillis) {
                continue
            }
            when (tx.type) {
                "INCOME" -> {
                    if (!tx.description.startsWith("Saldo Awal", ignoreCase = true)) {
                        totalIncome += tx.amount
                    }
                }
                "EXPENSE" -> {
                    totalExpense += tx.amount
                }
            }
        }

        val netSurplus = totalIncome - totalExpense

        // Rata-rata pengeluaran per hari kalender
        val diffMillis = (endTimeMillis - startTimeMillis).coerceAtLeast(1L)
        val days = (diffMillis / (1000L * 60 * 60 * 24)).coerceAtLeast(1L)
        val dailyAverage = totalExpense / days

        return CashFlowSummary(
            totalNetWorth = totalNetWorth,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netSurplus = netSurplus,
            dailyAverageExpense = dailyAverage,
            accounts = accounts
        )
    }

    /**
     * Menghitung distribusi pengeluaran per kategori secara efisien tanpa intermediate list.
     */
    fun computeCategoryDistribution(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<CategoryDistribution> {
        if (categories.isEmpty() || transactions.isEmpty()) return emptyList()

        val categoryMap = categories.associateBy { it.id }
        val expenseByCat = mutableMapOf<Long, Long>()
        var uncatExpense = 0L
        var totalExpense = 0L

        for (i in 0 until transactions.size) {
            val tx = transactions[i]
            if (tx.type == "EXPENSE" &&
                tx.status == "CONFIRMED" &&
                tx.dateMillis in startTimeMillis..endTimeMillis
            ) {
                totalExpense += tx.amount
                val catId = tx.categoryId
                if (catId != null) {
                    expenseByCat[catId] = (expenseByCat[catId] ?: 0L) + tx.amount
                } else {
                    uncatExpense += tx.amount
                }
            }
        }

        if (totalExpense <= 0L) return emptyList()

        val totalFloat = totalExpense.toFloat()
        val result = mutableListOf<CategoryDistribution>()

        for ((catId, amount) in expenseByCat) {
            val cat = categoryMap[catId]
            val catName = cat?.name ?: "Tanpa Kategori"
            val colorHex = cat?.colorHex ?: "#777F7B"
            val fraction = amount.toFloat() / totalFloat
            result.add(
                CategoryDistribution(
                    categoryName = catName,
                    colorHex = colorHex,
                    totalAmount = amount,
                    percentage = fraction * 100f
                )
            )
        }

        if (uncatExpense > 0L) {
            val fraction = uncatExpense.toFloat() / totalFloat
            result.add(
                CategoryDistribution(
                    categoryName = "Tanpa Kategori",
                    colorHex = "#777F7B",
                    totalAmount = uncatExpense,
                    percentage = fraction * 100f
                )
            )
        }

        result.sortByDescending { it.totalAmount }
        return result
    }

    /**
     * Estimasi pencapaian target pos tabungan berbasis surplus riwayat.
     */
    fun computeSavingsForecast(
        pots: List<FundPotEntity>,
        monthlySurplus: Long
    ): List<SavingsForecast> {
        return pots.map { pot ->
            val remaining = (pot.targetAmount - pot.currentAmount).coerceAtLeast(0L)
            val progress = if (pot.targetAmount > 0) {
                (pot.currentAmount.toFloat() / pot.targetAmount.toFloat()).coerceIn(0f, 1f)
            } else 1f

            val (months, status) = when {
                remaining == 0L -> {
                    Pair(0.0, "Target telah tercapai!")
                }
                monthlySurplus > 0L -> {
                    val m = remaining.toDouble() / monthlySurplus.toDouble()
                    Pair(m, "Estimasi tercapai dalam ~${String.format("%.1f", m)} bulan")
                }
                else -> {
                    Pair(null, "Surplus riwayat belum cukup untuk estimasi")
                }
            }

            SavingsForecast(
                potId = pot.id,
                title = pot.title,
                targetAmount = pot.targetAmount,
                currentAmount = pot.currentAmount,
                remainingAmount = remaining,
                progress = progress,
                estimatedMonthsLeft = months,
                statusText = status,
                imagePath = pot.imagePath
            )
        }
    }

    /**
     * Menghasilkan Insight Lokal berbasis aturan & statistik (100% offline).
     */
    fun generateLocalInsights(
        accounts: List<AccountWithBalance>,
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        pots: List<FundPotEntity>,
        debts: List<DebtEntity>
    ): List<LocalInsight> {
        val insights = mutableListOf<LocalInsight>()
        val now = System.currentTimeMillis()

        // 1. Cek Utang / Piutang mendekati jatuh tempo (dalam 7 hari)
        val sevenDaysFromNow = now + 7L * 24 * 60 * 60 * 1000
        val upcomingDebts = debts.filter {
            it.status == "ACTIVE" &&
                it.dueDateMillis != null &&
                it.dueDateMillis in now..sevenDaysFromNow
        }

        for (debt in upcomingDebts) {
            val sisa = debt.amount - debt.paidAmount
            val jenis = if (debt.type == "UTANG") "Kewajiban Utang" else "Tagihan Piutang"
            insights.add(
                LocalInsight(
                    id = "debt_${debt.id}",
                    title = "$jenis Mendekati Jatuh Tempo",
                    description = "$jenis kepada ${debt.personName} sebesar Rp${formatThousand(sisa)} jatuh tempo dalam kurang dari 7 hari.",
                    severity = if (debt.type == "UTANG") InsightSeverity.ALERT else InsightSeverity.INFO,
                    basis = "Jatuh tempo terdaftar pada catatan utang lokal"
                )
            )
        }

        // 2. Evaluasi Arus Kas Bulan Berjalan
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfMonth = cal.timeInMillis

        val monthTxs = transactions.filter { it.dateMillis >= startOfMonth && it.status == "CONFIRMED" }
        val monthIncome = monthTxs.filter { it.type == "INCOME" && !it.description.startsWith("Saldo Awal", true) }.sumOf { it.amount }
        val monthExpense = monthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        if (monthExpense > monthIncome && monthIncome > 0) {
            val defisit = monthExpense - monthIncome
            insights.add(
                LocalInsight(
                    id = "cashflow_deficit",
                    title = "Laju Pengeluaran Melampaui Pemasukan",
                    description = "Pengeluaran bulan ini (Rp${formatThousand(monthExpense)}) melebihi pemasukan operasional sebesar Rp${formatThousand(defisit)}. Pertimbangkan menahan pos tersier.",
                    severity = InsightSeverity.WARNING,
                    basis = "Arus kas tercatat 1 ${cal.get(Calendar.MONTH) + 1} s/d hari ini"
                )
            )
        } else if (monthIncome > monthExpense && monthExpense > 0) {
            val surplus = monthIncome - monthExpense
            insights.add(
                LocalInsight(
                    id = "cashflow_surplus",
                    title = "Arus Kas Positif",
                    description = "Bagus! Anda memiliki surplus Rp${formatThousand(surplus)} bulan ini yang dapat dialokasikan ke pos tabungan atau dana darurat.",
                    severity = InsightSeverity.SUCCESS,
                    basis = "Pemasukan melebihi pengeluaran bulan berjalan"
                )
            )
        }

        // 3. Peringatan Target Tabungan
        pots.forEach { pot ->
            if (pot.targetAmount > 0) {
                val ratio = pot.currentAmount.toDouble() / pot.targetAmount.toDouble()
                if (ratio >= 0.8 && ratio < 1.0) {
                    insights.add(
                        LocalInsight(
                            id = "pot_${pot.id}_milestone",
                            title = "Pos '${pot.title}' Hampir Penuh!",
                            description = "Tabungan telah mencapai ${(ratio * 100).toInt()}%. Tinggal sedikit lagi untuk mencapai target Rp${formatThousand(pot.targetAmount)}.",
                            severity = InsightSeverity.SUCCESS,
                            basis = "Progres pos dana tercatat di database lokal"
                        )
                    )
                }
            }
        }

        // 4. Saldo Akun Negatif
        accounts.forEach { acc ->
            if (acc.balance < 0) {
                insights.add(
                    LocalInsight(
                        id = "acc_neg_${acc.account.id}",
                        title = "Saldo ${acc.account.name} Negatif",
                        description = "Akun ${acc.account.name} bersaldo -Rp${formatThousand(-acc.balance)}.",
                        severity = InsightSeverity.ALERT,
                        basis = "Catatan akun ${acc.account.name}"
                    )
                )
            }
        }

        // Default healthy tip if no alerts
        if (insights.isEmpty()) {
            insights.add(
                LocalInsight(
                    id = "general_tip",
                    title = "Kondisi Keuangan Stabil",
                    description = "Semua pencatatan keuangan dan saldo Anda dalam kondisi teratur.",
                    severity = InsightSeverity.INFO,
                    basis = "Ringkasan transaksi"
                )
            )
        }

        return insights
    }

    private fun formatThousand(num: Long): String {
        return String.format("%,d", num).replace(',', '.')
    }
}
