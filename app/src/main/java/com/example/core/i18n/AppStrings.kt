package com.example.core.i18n

object AppStrings {

    fun getGreeting(language: String, hour: Int, userName: String): String {
        val nameSuffix = if (userName.isNotBlank()) ", $userName" else ""
        return if (language == "en") {
            val time = when (hour) {
                in 4..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..20 -> "Good evening"
                else -> "Good night"
            }
            "$time$nameSuffix 👋"
        } else {
            val time = when (hour) {
                in 4..10 -> "Selamat pagi"
                in 11..14 -> "Selamat siang"
                in 15..18 -> "Selamat sore"
                else -> "Selamat malam"
            }
            "$time$nameSuffix 👋"
        }
    }

    fun tabHome(lang: String) = if (lang == "en") "Home" else "Utama"
    fun tabTransactions(lang: String) = if (lang == "en") "Transactions" else "Transaksi"
    fun tabWallets(lang: String) = if (lang == "en") "Wallets" else "Dompet"
    fun tabAnalytics(lang: String) = if (lang == "en") "Analytics" else "Statistik"
    fun tabMore(lang: String) = if (lang == "en") "Settings" else "Lainnya"

    fun totalBalance(lang: String) = if (lang == "en") "Total Balance" else "Total Saldo"
    fun record(lang: String) = if (lang == "en") "Record" else "Catat"
    fun transfer(lang: String) = if (lang == "en") "Transfer" else "Transfer"
    fun savingsPots(lang: String) = if (lang == "en") "Savings Pots" else "Pos Tabungan"
    fun debts(lang: String) = if (lang == "en") "Debts" else "Utang"

    fun recentTransactions(lang: String) = if (lang == "en") "Recent Transactions" else "Transaksi Terakhir"
    fun viewAll(lang: String) = if (lang == "en") "View All" else "Lihat Semua"
    fun noTransactions(lang: String) = if (lang == "en") "No transactions yet" else "Belum ada transaksi"

    fun income(lang: String) = if (lang == "en") "Income" else "Pemasukan"
    fun expense(lang: String) = if (lang == "en") "Expense" else "Pengeluaran"
    fun all(lang: String) = if (lang == "en") "All" else "Semua"
    fun monthlyCashflow(lang: String) = if (lang == "en") "Monthly Cash Flow" else "Arus Kas Bulan Ini"

    fun addWallet(lang: String) = if (lang == "en") "Add Wallet" else "Tambah Dompet"
    fun editWallet(lang: String) = if (lang == "en") "Edit Wallet" else "Ubah Dompet"
    fun deleteWallet(lang: String) = if (lang == "en") "Delete Wallet" else "Hapus Dompet"
    fun walletsAndAccounts(lang: String) = if (lang == "en") "Wallets & Accounts" else "Dompet & Rekening"

    fun addPot(lang: String) = if (lang == "en") "New Pot" else "Pos Baru"
    fun addFunds(lang: String) = if (lang == "en") "Add Funds" else "Tambah"
    fun withdrawFunds(lang: String) = if (lang == "en") "Spend / Withdraw" else "Tarik / Pakai"
    fun collected(lang: String) = if (lang == "en") "Saved" else "Terkumpul"
    fun target(lang: String) = if (lang == "en") "Target" else "Target"
    fun potGoalImage(lang: String) = if (lang == "en") "Goal / Dream Photo" else "Foto Impian / Target"
    fun addGoalPhoto(lang: String) = if (lang == "en") "Attach Photo" else "Lampirkan Foto"
    fun changeGoalPhoto(lang: String) = if (lang == "en") "Change Photo" else "Ganti Foto"
    fun goalPhotoPreview(lang: String) = if (lang == "en") "Target / Goal Photo" else "Foto Target Celengan"

    fun save(lang: String) = if (lang == "en") "Save" else "Simpan"
    fun cancel(lang: String) = if (lang == "en") "Cancel" else "Batal"
    fun delete(lang: String) = if (lang == "en") "Delete" else "Hapus"
    fun edit(lang: String) = if (lang == "en") "Edit" else "Ubah"

    fun transactionDetails(lang: String) = if (lang == "en") "Transaction Details" else "Detail Transaksi"
    fun receiptPhoto(lang: String) = if (lang == "en") "Receipt & Photos" else "Bukti & Lampiran Gambar"
    fun noReceipt(lang: String) = if (lang == "en") "No receipt attached" else "Tidak ada lampiran foto"
    fun zoom(lang: String) = if (lang == "en") "Zoom" else "Perbesar"

    fun profile(lang: String) = if (lang == "en") "Profile" else "Profil"
    fun name(lang: String) = if (lang == "en") "Display Name" else "Nama Tampilan"
    fun security(lang: String) = if (lang == "en") "Password Security" else "Kunci Kata Sandi"
    fun enablePassword(lang: String) = if (lang == "en") "Require Password to Open" else "Kunci Aplikasi dengan Sandi"
    fun enterPassword(lang: String) = if (lang == "en") "Enter Password" else "Masukkan Kata Sandi"
    fun setPassword(lang: String) = if (lang == "en") "Set Password" else "Atur Kata Sandi"
    fun unlock(lang: String) = if (lang == "en") "Unlock" else "Buka Kunci"
    fun wrongPassword(lang: String) = if (lang == "en") "Incorrect password" else "Kata sandi salah"

    fun appearance(lang: String) = if (lang == "en") "Theme & Customization" else "Tema & Tampilan"
    fun accentColor(lang: String) = if (lang == "en") "Accent Color" else "Warna Aksen"
    fun wallpaper(lang: String) = if (lang == "en") "Wallpaper" else "Wallpaper Latar"
    fun fontContrast(lang: String) = if (lang == "en") "Font Contrast" else "Kontras Warna Font"
    fun fontContrastAuto(lang: String) = if (lang == "en") "Auto (Adaptive)" else "Otomatis (Adaptif)"
    fun fontContrastLight(lang: String) = if (lang == "en") "Light Text (White)" else "Teks Terang (Putih)"
    fun fontContrastDark(lang: String) = if (lang == "en") "Dark Text (Slate)" else "Teks Gelap (Pekat)"
    fun themeMode(lang: String) = if (lang == "en") "Theme Mode" else "Mode Tema"
    fun light(lang: String) = if (lang == "en") "Light" else "Terang"
    fun dark(lang: String) = if (lang == "en") "Dark" else "Gelap"
    fun languageLabel(lang: String) = if (lang == "en") "Language" else "Bahasa"

    fun balanceTrendFlat(lang: String) = if (lang == "en") "Stable (Rp 0) • No balance movement yet" else "Stabil (Rp 0) • Belum ada pergerakan saldo"
    fun balanceTrendUp(lang: String, amount: String) = if (lang == "en") "▲ Uptrend (+$amount)" else "▲ Tren Naik (+$amount)"
    fun balanceTrendDown(lang: String, amount: String) = if (lang == "en") "▼ Downtrend ($amount)" else "▼ Tren Turun ($amount)"
    fun balanceTrendStable(lang: String) = if (lang == "en") "― Stable balance" else "― Saldo stabil"

    fun addTransaction(lang: String) = if (lang == "en") "Record Transaction" else "Catat Transaksi"
    fun close(lang: String) = if (lang == "en") "Close" else "Tutup"
    fun amountLabel(lang: String) = if (lang == "en") "Amount (IDR)" else "Nominal (Rupiah)"
    fun amountPlaceholder(lang: String) = if (lang == "en") "e.g. 20000" else "Contoh: 20000"
    fun fromAccountLabel(lang: String) = if (lang == "en") "From Account (Source)" else "Dari Akun (Sumber Dana)"
    fun toAccountLabel(lang: String) = if (lang == "en") "To Account (Destination)" else "Ke Akun (Tujuan Dana)"
    fun sourceAccountLabel(lang: String) = if (lang == "en") "Source Account" else "Sumber Dana"
    fun destAccountLabel(lang: String) = if (lang == "en") "Destination Account" else "Akun Tujuan"
    fun categoryLabel(lang: String) = if (lang == "en") "Category" else "Kategori"
    fun descriptionLabel(lang: String) = if (lang == "en") "Description / Merchant" else "Deskripsi / Merchant"
    fun descriptionPlaceholder(lang: String) = if (lang == "en") "e.g. Lunch, Groceries" else "Contoh: Makan siang, Belanja Indomaret"
    fun fundClassification(lang: String) = if (lang == "en") "Fund Classification" else "Klasifikasi Dana"
    fun personalFund(lang: String) = if (lang == "en") "Personal Fund" else "Dana Pribadi"
    fun obligationFund(lang: String) = if (lang == "en") "Deposit / Debt" else "Titipan / Kewajiban"
    fun unspecifiedFund(lang: String) = if (lang == "en") "Unspecified" else "Belum Ditentukan"
    fun attachmentsTitle(lang: String) = if (lang == "en") "Attachments & Receipt OCR" else "Lampiran Foto & OCR Struk (Lokal)"
    fun scanReceipt(lang: String) = if (lang == "en") "Scan Receipt (OCR)" else "Scan Struk (OCR)"
    fun scanning(lang: String) = if (lang == "en") "Scanning..." else "Membaca..."
    fun choosePhoto(lang: String) = if (lang == "en") "Choose Photo" else "Pilih Foto"
    fun confirmAndSave(lang: String) = if (lang == "en") "Confirm & Save Transaction" else "Konfirmasi & Simpan Transaksi"
    fun ocrSuccess(lang: String) = if (lang == "en") "OCR Successful! Check detected amount and merchant." else "OCR Berhasil! Periksa nominal dan merchant yang terdeteksi."
    fun ocrFailed(lang: String, err: String) = if (lang == "en") "OCR Failed: $err" else "OCR Gagal: $err"
    fun amountMustBePositive(lang: String) = if (lang == "en") "Transaction amount must be greater than 0" else "Nominal transaksi harus lebih dari 0"
    fun sameSourceDestError(lang: String) = if (lang == "en") "Source and destination accounts cannot be the same" else "Akun asal dan akun tujuan tidak boleh sama"
    fun interAccountTransfer(lang: String) = if (lang == "en") "Inter-account Transfer" else "Transfer Antarakun"
    fun newTransactionTitle(lang: String) = if (lang == "en") "New Transaction" else "Transaksi Baru"
    fun exceedsBalance(lang: String, accName: String, remaining: String) =
        if (lang == "en") "This expense exceeds $accName balance ($remaining remaining)." else "Pengeluaran ini melebihi saldo $accName (Sisa $remaining)."

    fun categoryName(lang: String, rawName: String): String {
        if (lang != "en") return rawName
        return when (rawName.trim()) {
            "Makanan & Minuman" -> "Food & Drinks"
            "Transportasi" -> "Transportation"
            "Belanja" -> "Shopping"
            "Tagihan & Utilitas", "Tagihan" -> "Bills & Utilities"
            "Hiburan" -> "Entertainment"
            "Kesehatan" -> "Healthcare"
            "Pendidikan" -> "Education"
            "Gaji" -> "Salary"
            "Bonus" -> "Bonus"
            "Investasi" -> "Investment"
            "Penjualan" -> "Sales"
            "Lainnya" -> "Other"
            else -> rawName
        }
    }
}
