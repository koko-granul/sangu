package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.i18n.AppStrings
import com.example.ui.FinanceViewModel
import com.example.ui.screens.AccountsAndPotsScreen
import com.example.ui.screens.AddTransactionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsAndMoreScreen
import com.example.ui.screens.PasswordLockScreen
import com.example.ui.screens.TransactionListScreen
import com.example.ui.theme.LocalDynamicTheme
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FinanceViewModel = viewModel()
            val themeConfig by viewModel.settingsManager.themeConfig.collectAsState()

            MyApplicationTheme(customization = themeConfig) {
                SanguApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SanguApp(viewModel: FinanceViewModel = viewModel()) {
    val dynamicTheme = LocalDynamicTheme.current

    val isPasswordLockEnabled by viewModel.settingsManager.isPasswordLockEnabled.collectAsState()
    val isUnlocked by viewModel.settingsManager.isUnlocked.collectAsState()
    val language by viewModel.settingsManager.language.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    val selectedTab = pagerState.currentPage

    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var initialAddType by remember { mutableStateOf("EXPENSE") }

    val dashboardListState = rememberLazyListState()
    val transactionListState = rememberLazyListState()
    val accountsListState = rememberLazyListState()
    val analyticsListState = rememberLazyListState()
    val moreListState = rememberLazyListState()

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxBarOffsetPx = with(density) { 110.dp.toPx() }
    val bottomBarOffset = remember { Animatable(0f) }

    // Handle tombol back Android untuk kembali ke Dashboard jika sedang di halaman lain
    if (pagerState.currentPage != 0) {
        BackHandler {
            scope.launch {
                pagerState.animateScrollToPage(0)
                bottomBarOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            }
        }
    }

    val activeListState = when (pagerState.currentPage) {
        0 -> dashboardListState
        1 -> transactionListState
        2 -> accountsListState
        3 -> analyticsListState
        4 -> moreListState
        else -> null
    }

    // Periksa apakah halaman aktif benar-benar memiliki konten panjang yang melebihi 1 layar
    val isContentScrollable by remember(activeListState) {
        derivedStateOf {
            val state = activeListState ?: return@derivedStateOf false
            val layoutInfo = state.layoutInfo
            val totalCount = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo
            if (totalCount == 0 || visibleItems.isEmpty()) return@derivedStateOf false

            // Jika jumlah item terlihat kurang dari total item, pasti ada lanjutan konten di bawah
            if (visibleItems.size < totalCount) {
                true
            } else {
                // Semua item termuat di layar. Cek apakah bagian bawah item terakhir melebihi viewport
                val first = visibleItems.first()
                val last = visibleItems.last()
                val contentHeight = (last.offset + last.size) - first.offset
                val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                contentHeight > (viewportHeight - 20)
            }
        }
    }

    // Pada halaman non-scroll atau saat berpindah tab,
    // widget bottom dock wajib tetap diam & tampil di layar (animasi sembunyi disable)
    LaunchedEffect(isContentScrollable, pagerState.currentPage) {
        if (!isContentScrollable) {
            bottomBarOffset.snapTo(0f)
        }
    }

    // Nested scroll connection:
    // 1. Kecepatan gerak widget 100% persis mengikuti kecepatan usapan jari (1:1 displacement)
    //    Tidak ada delay (tidak gerak telat) dan tidak ada animasi paksaan (tidak lari lebih cepat)
    // 2. Pada halaman non-scroll, gerakan animasi sembunyi di-disable total
    val nestedScrollConnection = remember(activeListState, isContentScrollable) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Non-scroll page: disable animasi total
                if (!isContentScrollable) {
                    if (bottomBarOffset.value != 0f) {
                        scope.launch { bottomBarOffset.snapTo(0f) }
                    }
                    return Offset.Zero
                }

                val delta = available.y
                val state = activeListState

                // Usap ke atas (delta < 0, konten scroll ke bawah untuk melihat lanjutan):
                // Widget bergeser ke bawah persis sejarak usapan jari tangan
                if (delta < 0f && (state?.canScrollForward == true)) {
                    val target = (bottomBarOffset.value - delta).coerceIn(0f, maxBarOffsetPx)
                    scope.launch { bottomBarOffset.snapTo(target) }
                }
                // Usap ke bawah (delta > 0, scroll ke atas):
                // Widget langsung naik kembali ke layar mengikuti tangan
                else if (delta > 0f) {
                    val target = (bottomBarOffset.value - delta).coerceIn(0f, maxBarOffsetPx)
                    scope.launch { bottomBarOffset.snapTo(target) }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!isContentScrollable) {
                    bottomBarOffset.animateTo(0f)
                    return Velocity.Zero
                }
                val vy = consumed.y
                // Usapan cepat ke atas: sembunyikan dengan spring alami
                if (vy < -350f && (activeListState?.canScrollForward == true)) {
                    bottomBarOffset.animateTo(
                        targetValue = maxBarOffsetPx,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                    )
                }
                // Usapan cepat ke bawah: munculkan kembali
                else if (vy > 350f || activeListState?.canScrollForward == false) {
                    bottomBarOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                    )
                }
                // Dilepas perlahan di tengah: lengkapi ke posisi terdekat
                else {
                    val target = if (bottomBarOffset.value > maxBarOffsetPx * 0.45f) maxBarOffsetPx else 0f
                    bottomBarOffset.animateTo(
                        targetValue = target,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                    )
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicTheme.background)
    ) {
        // Konten Layar Utama atau Lock Screen
        if (isPasswordLockEnabled && !isUnlocked) {
            PasswordLockScreen(viewModel = viewModel)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .nestedScroll(nestedScrollConnection)
            ) {
                // Perpindahan halaman tab bisa melalui gesture usap (swipe) horizontal maupun tap dock
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            listState = dashboardListState,
                            onOpenAddTransaction = { type ->
                                initialAddType = type
                                showAddTransactionSheet = true
                            },
                            onNavigateToTab = { targetTab ->
                                scope.launch {
                                    pagerState.animateScrollToPage(targetTab)
                                    bottomBarOffset.animateTo(0f)
                                }
                            }
                        )
                        1 -> TransactionListScreen(
                            viewModel = viewModel,
                            listState = transactionListState
                        )
                        2 -> AccountsAndPotsScreen(
                            viewModel = viewModel,
                            listState = accountsListState
                        )
                        3 -> AnalyticsScreen(
                            viewModel = viewModel,
                            listState = analyticsListState
                        )
                        4 -> DebtsAndMoreScreen(
                            viewModel = viewModel,
                            listState = moreListState
                        )
                    }
                }

                // Floating Bottom Navigation Dock & Round Action Button (+) Bersatu dalam Satu Baris
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .graphicsLayer {
                            translationY = bottomBarOffset.value
                            alpha = (1f - (bottomBarOffset.value / maxBarOffsetPx) * 0.75f).coerceIn(0f, 1f)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FloatingNavDock(
                        modifier = Modifier.weight(1f),
                        selectedTab = selectedTab,
                        language = language,
                        onTabSelected = { targetPage ->
                            scope.launch {
                                pagerState.animateScrollToPage(targetPage)
                                bottomBarOffset.animateTo(0f)
                            }
                        }
                    )

                    // Tombol Ijo Bulat (+) Bersatu dengan Menu Dock (Turun/Naik Bersama)
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                spotColor = dynamicTheme.accent.copy(alpha = 0.45f)
                            )
                            .clip(CircleShape)
                            .clickable {
                                initialAddType = "EXPENSE"
                                showAddTransactionSheet = true
                            },
                        shape = CircleShape,
                        color = dynamicTheme.accent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = AppStrings.record(language),
                                tint = dynamicTheme.onAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Bottom Sheet Tambah Transaksi
                if (showAddTransactionSheet) {
                    AddTransactionSheet(
                        viewModel = viewModel,
                        initialType = initialAddType,
                        onDismiss = { showAddTransactionSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingNavDock(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    language: String,
    onTabSelected: (Int) -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current

    val navItems = listOf(
        NavItem(0, AppStrings.tabHome(language), Icons.Default.Dashboard),
        NavItem(1, AppStrings.tabTransactions(language), Icons.AutoMirrored.Filled.ReceiptLong),
        NavItem(2, AppStrings.tabWallets(language), Icons.Default.AccountBalanceWallet),
        NavItem(3, AppStrings.tabAnalytics(language), Icons.Default.BarChart),
        NavItem(4, AppStrings.tabMore(language), Icons.Default.MoreHoriz)
    )

    Surface(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x18000000),
                ambientColor = Color(0x0C000000)
            ),
        shape = RoundedCornerShape(26.dp),
        color = dynamicTheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = selectedTab == item.index

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTabSelected(item.index) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) dynamicTheme.accentLight else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) dynamicTheme.accent else dynamicTheme.textSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) dynamicTheme.accent else dynamicTheme.textSecondary,
                        fontSize = 9.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class NavItem(
    val index: Int,
    val label: String,
    val icon: ImageVector
)
