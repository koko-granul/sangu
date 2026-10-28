package com.example

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.LedgerEntryEntity
import com.example.domain.engine.FinanceEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMandatoryBcaBriExpenseScenario() {
        // Akun BCA dan BRI
        val bca = AccountEntity(id = 1L, name = "BCA", type = "BANK", colorHex = "#0060AF", iconName = "account_balance")
        val bri = AccountEntity(id = 2L, name = "BRI", type = "BANK", colorHex = "#00529C", iconName = "account_balance")
        val accounts = listOf(bca, bri)

        // Saldo Awal: BCA = Rp50.000, BRI = Rp50.000
        val ledger = mutableListOf(
            LedgerEntryEntity(
                id = 1L,
                transactionId = 1L,
                accountId = bca.id,
                direction = "INFLOW",
                amount = 50000L,
                entryType = "OPENING_BALANCE",
                timestamp = 1000L
            ),
            LedgerEntryEntity(
                id = 2L,
                transactionId = 2L,
                accountId = bri.id,
                direction = "INFLOW",
                amount = 50000L,
                entryType = "OPENING_BALANCE",
                timestamp = 1000L
            )
        )

        // Verifikasi kondisi awal
        val initialBalances = FinanceEngine.computeAccountBalances(accounts, ledger).associate { it.account.id to it.balance }
        assertEquals(50000L, initialBalances[bca.id])
        assertEquals(50000L, initialBalances[bri.id])

        // Pengguna membuat pengeluaran Rp20.000 dan memilih BCA pada field Sumber dana
        ledger.add(
            LedgerEntryEntity(
                id = 3L,
                transactionId = 3L,
                accountId = bca.id,
                direction = "OUTFLOW",
                amount = 20000L,
                entryType = "OPERATIONAL",
                timestamp = 2000L
            )
        )

        // Hasil yang benar setelah disimpan:
        // BCA: Rp50.000 - Rp20.000 = Rp30.000
        // BRI: Rp50.000 - Rp0 = Rp50.000
        val afterExpenseBalances = FinanceEngine.computeAccountBalances(accounts, ledger).associate { it.account.id to it.balance }
        assertEquals(30000L, afterExpenseBalances[bca.id])
        assertEquals(50000L, afterExpenseBalances[bri.id])

        // Verifikasi transfer antarakun: Transfer Rp10.000 dari BCA ke BRI
        ledger.add(
            LedgerEntryEntity(
                id = 4L,
                transactionId = 4L,
                accountId = bca.id,
                direction = "OUTFLOW",
                amount = 10000L,
                entryType = "TRANSFER",
                timestamp = 3000L
            )
        )
        ledger.add(
            LedgerEntryEntity(
                id = 5L,
                transactionId = 4L,
                accountId = bri.id,
                direction = "INFLOW",
                amount = 10000L,
                entryType = "TRANSFER",
                timestamp = 3000L
            )
        )

        val afterTransferBalances = FinanceEngine.computeAccountBalances(accounts, ledger).associate { it.account.id to it.balance }
        assertEquals(20000L, afterTransferBalances[bca.id])
        assertEquals(60000L, afterTransferBalances[bri.id])

        // Total kekayaan harus tetap Rp80.000 (tidak ada uang tercipta atau hilang dari transfer)
        val totalNetWorth = FinanceEngine.computeTotalNetWorth(FinanceEngine.computeAccountBalances(accounts, ledger))
        assertEquals(80000L, totalNetWorth)
    }
}
