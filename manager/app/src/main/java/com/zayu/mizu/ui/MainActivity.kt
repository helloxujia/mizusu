package com.zayu.mizu.ui

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.zayu.mizu.ui.util.module.Shortcut
import com.zayu.mizu.ui.util.restoreLauncherIcon
import com.zayu.mizu.ui.util.setLauncherIconStyle
import kotlin.random.Random
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.MutableStateFlow
import com.zayu.mizu.Natives
import com.zayu.mizu.ui.component.bottombar.BottomBar
import com.zayu.mizu.ui.component.bottombar.MainPagerState
import com.zayu.mizu.ui.component.bottombar.SideRail
import com.zayu.mizu.ui.component.bottombar.rememberMainPagerState
import com.zayu.mizu.ui.kernelFlash.KernelFlashScreen
import com.zayu.mizu.ui.navigation3.HandleDeepLink
import com.zayu.mizu.ui.navigation3.LocalNavigator
import com.zayu.mizu.ui.navigation3.Navigator
import com.zayu.mizu.ui.navigation3.Route
import com.zayu.mizu.ui.navigation3.rememberNavigator
import com.zayu.mizu.ui.screen.about.AboutScreen
import com.zayu.mizu.ui.screen.appprofile.AppProfileScreen
import com.zayu.mizu.ui.screen.colorpalette.ColorPaletteScreen
import com.zayu.mizu.ui.screen.customicon.CropDialog
import com.zayu.mizu.ui.screen.customicon.CustomIconScreen
import com.zayu.mizu.ui.screen.customicon.IconShape
import com.zayu.mizu.ui.screen.customicon.ShortcutNameDialog
import com.zayu.mizu.ui.screen.executemoduleaction.ExecuteModuleActionScreen
import com.zayu.mizu.ui.screen.flash.FlashScreen
import com.zayu.mizu.ui.screen.home.HomePager
import com.zayu.mizu.ui.screen.install.InstallScreen
import com.zayu.mizu.ui.screen.kpm.KpmScreen
import com.zayu.mizu.ui.screen.module.ModulePager
import com.zayu.mizu.ui.screen.modulerepo.ModuleRepoDetailScreen
import com.zayu.mizu.ui.screen.modulerepo.ModuleRepoScreen
import com.zayu.mizu.ui.screen.settings.SettingPager
import com.zayu.mizu.ui.screen.settings.tools.ToolsScreen
import com.zayu.mizu.ui.screen.sulog.SulogScreen
import com.zayu.mizu.ui.screen.superuser.SuperUserPager
import com.zayu.mizu.ui.screen.susfs.SuSFSScreen
import com.zayu.mizu.ui.screen.template.AppProfileTemplateScreen
import com.zayu.mizu.ui.screen.templateeditor.TemplateEditorScreen
import com.zayu.mizu.ui.screen.umountmanager.UmountManagerScreen
import com.zayu.mizu.ui.theme.KernelSUTheme
import com.zayu.mizu.ui.theme.LocalColorMode
import com.zayu.mizu.ui.theme.LocalEnableBlur
import com.zayu.mizu.ui.theme.LocalEnableFloatingBottomBar
import com.zayu.mizu.ui.theme.LocalEnableFloatingBottomBarBlur
import com.zayu.mizu.ui.util.install
import com.zayu.mizu.ui.util.rememberBlurBackdrop
import com.zayu.mizu.ui.util.rememberContentReady
import com.zayu.mizu.ui.util.rootAvailable
import com.zayu.mizu.ui.viewmodel.MainActivityViewModel
import com.zayu.mizu.ui.viewmodel.MainPagerConfig
import com.zayu.mizu.ui.webui.WebUIActivity
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val KEY_INTENT_STATE = "intent_state"

class MainActivity : ComponentActivity() {

    private var intentStateValue by mutableIntStateOf(0)
    private val intentStateFlow = MutableStateFlow(0)
    private val intentState: MutableStateFlow<Int>
        get() {
            if (intentStateFlow.value != intentStateValue) {
                intentStateFlow.value = intentStateValue
            }
            return intentStateFlow
        }

    private var soundPlayer: MediaPlayer? = null

    private fun playRandomSound() {
        try {
            val sounds = assets.list("sounds")?.filter { it.endsWith(".mp3") } ?: return
            if (sounds.isEmpty()) return
            val name = "sounds/${sounds[Random.nextInt(sounds.size)]}"
            val fd = assets.openFd(name)
            soundPlayer?.release()
            soundPlayer = MediaPlayer().apply {
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                setOnCompletionListener {
                    it.release()
                    soundPlayer = null
                }
                prepare()
                start()
            }
        } catch (_: Exception) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val soundEnabled = prefs.getBoolean("enable_sound_effect", true)
        if (soundEnabled) {
            playRandomSound()
        }
        intentStateValue = savedInstanceState?.getInt(KEY_INTENT_STATE, 0) ?: 0
        intentStateFlow.value = intentStateValue

        val isManager = Natives.isManager
        if (isManager && !Natives.requireNewKernel()) install()

        setContent {
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val selectedMainPage by viewModel.selectedMainPage.collectAsStateWithLifecycle()
            val appSettings = uiState.appSettings
            val uiMode = uiState.uiMode
            val darkMode = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && isSystemInDarkTheme())

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                )
                window.isNavigationBarContrastEnforced = false
                onDispose { }
            }

            val navigator = rememberNavigator(Route.Main)
            val systemDensity = LocalDensity.current
            val density = remember(systemDensity, uiState.pageScale) {
                Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
            }

            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalDensity provides density,
                LocalColorMode provides appSettings.colorMode.value,
                LocalEnableBlur provides uiState.enableBlur,
                LocalEnableFloatingBottomBar provides uiState.enableFloatingBottomBar,
                LocalEnableFloatingBottomBarBlur provides uiState.enableFloatingBottomBarBlur,
                LocalUiMode provides uiMode,
            ) {
                KernelSUTheme(appSettings = appSettings, uiMode = uiMode) {
                    HandleDeepLink(intentState = intentState.collectAsStateWithLifecycle())
                    ShortcutIntentHandler(intentState = intentState)
                    HandleZipFileIntent(intentState = intentState)
                    val mainScreenEntry = @Composable {
                        MainScreen(
                            initialPage = selectedMainPage,
                            onPageChanged = viewModel::setSelectedMainPage,
                        )
                    }

                    val navDisplay = @Composable {
                        NavDisplay(
                            backStack = navigator.backStack,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            onBack = {
                                when (val top = navigator.current()) {
                                    is Route.TemplateEditor -> {
                                        if (!top.readOnly) {
                                            navigator.setResult("template_edit", true)
                                        } else {
                                            navigator.pop()
                                        }
                                    }

                                    else -> navigator.pop()
                                }
                            },
                            entryProvider = entryProvider {
                                entry<Route.Main> { mainScreenEntry() }
                                entry<Route.About> { AboutScreen() }
                                entry<Route.Sulog> { SulogScreen() }
                                entry<Route.ColorPalette> { ColorPaletteScreen() }
                                entry<Route.CustomIcon> {
                                    val activity = this@MainActivity
                                    val prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                    var style by remember { mutableIntStateOf(prefs.getInt("icon_style", 0)) }
                                    var pendingCroppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
                                    var showNameDialog by remember { mutableStateOf(false) }

                                    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
                                    var showCrop by remember { mutableStateOf(false) }
                                    val isLaunchingCustom = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
                                    val scope = rememberCoroutineScope()

                                    val imagePicker = rememberLauncherForActivityResult(contract = PickVisualMedia()) { uri ->
                                        if (uri != null) {
                                            val bmp = try {
                                                val bytes = activity.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                                    ?: throw Exception("Failed to read image")
                                                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                                                val maxDim = maxOf(opts.outWidth, opts.outHeight)
                                                opts.inSampleSize = if (maxDim > 2048) maxDim / 2048 else 1
                                                opts.inJustDecodeBounds = false
                                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                                            } catch (_: Throwable) { null }
                                            if (bmp != null) {
                                                sourceBitmap = bmp
                                                showCrop = true
                                            } else {
                                                Toast.makeText(activity, "无法加载图片", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        isLaunchingCustom.set(false)
                                    }

                                    if (showCrop && sourceBitmap != null) {
                                        CropDialog(
                                            bitmap = sourceBitmap!!,
                                            onConfirm = { cropped ->
                                                showCrop = false
                                                pendingCroppedBitmap = cropped
                                                showNameDialog = true
                                            },
                                            onDismiss = { showCrop = false; sourceBitmap = null }
                                        )
                                    }

                                    if (showNameDialog && pendingCroppedBitmap != null) {
                                        ShortcutNameDialog(
                                            previewBitmap = pendingCroppedBitmap!!,
                                            onConfirm = { name, shape ->
                                                showNameDialog = false
                                                activity.createCustomShortcut(pendingCroppedBitmap!!, name, shape)
                                                pendingCroppedBitmap = null
                                            },
                                            onDismiss = {
                                                showNameDialog = false
                                                pendingCroppedBitmap = null
                                            }
                                        )
                                    }

                                    CustomIconScreen(
                                        iconStyle = style,
                                        onSelect = { s ->
                                            style = s
                                            prefs.edit().putInt("icon_style", s).apply()
                                            setLauncherIconStyle(activity, s)
                                            Toast.makeText(activity, "图标已切换，返回桌面即可查看新图标", Toast.LENGTH_SHORT).show()
                                        },
                                        onBack = { navigator.pop() },
                                        onRestore = {
                                            restoreLauncherIcon(activity)
                                            Toast.makeText(activity, "桌面图标已还原", Toast.LENGTH_SHORT).show()
                                        },
                                        onToggleHide = {
                                            Toast.makeText(activity, "隐藏桌面图标功能研发中，敬请期待", Toast.LENGTH_SHORT).show()
                                        },
                                        onCustomUpload = {
                                            if (!isLaunchingCustom.compareAndSet(false, true)) return@CustomIconScreen
                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                val permitted = Shortcut.ensureShortcutPermission(activity)
                                                withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    if (permitted) {
                                                        Toast.makeText(activity, "请选择一张正方形图片，以获得最佳桌面图标效果", Toast.LENGTH_LONG).show()
                                                        imagePicker.launch(
                                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        )
                                                    } else {
                                                        isLaunchingCustom.set(false)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                                entry<Route.AppProfileTemplate> { AppProfileTemplateScreen() }
                                entry<Route.TemplateEditor> { key -> TemplateEditorScreen(key.template, key.readOnly) }
                                entry<Route.AppProfile> { key -> AppProfileScreen(key.uid) }
                                entry<Route.ModuleRepo> { ModuleRepoScreen() }
                                entry<Route.ModuleRepoDetail> { key -> ModuleRepoDetailScreen(key.module) }
                                entry<Route.Install> { key -> InstallScreen(preselectedKernelUri = key.preselectedKernelUri) }
                                entry<Route.Flash> { key -> FlashScreen(key.flashIt) }
                                entry<Route.ExecuteModuleAction> { key -> ExecuteModuleActionScreen(key.moduleId, key.fromShortcut) }
                                entry<Route.Home> { mainScreenEntry() }
                                entry<Route.SuperUser> { mainScreenEntry() }
                                entry<Route.Module> { mainScreenEntry() }
                                entry<Route.Settings> { mainScreenEntry() }
                                entry<Route.KernelFlash> { key -> KernelFlashScreen(key.kernelUri, key.selectedSlot, key.kpmPatchEnabled, key.kpmUndoPatch) }
                                entry<Route.Kpm> { KpmScreen() }
                                entry<Route.SuSFS> { SuSFSScreen() }
                                entry<Route.Tool> { ToolsScreen() }
                                entry<Route.UmountManager> { UmountManagerScreen() }
                            }
                        )
                    }

                    when (uiMode) {
                        UiMode.Material -> androidx.compose.material3.Scaffold { navDisplay() }
                        UiMode.Miuix -> Scaffold { navDisplay() }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Increment intentState to trigger LaunchedEffect re-execution
        intentStateValue += 1
        intentStateFlow.value = intentStateValue
    }

    private fun createCustomShortcut(bitmap: Bitmap, name: String, shape: IconShape) {
        try {
            val square = if (bitmap.width != 432 || bitmap.height != 432) {
                Bitmap.createScaledBitmap(bitmap, 432, 432, true)
            } else bitmap

            val iconBmp = when (shape) {
                IconShape.Circle -> cropToCircleBitmap(square, 432)
                IconShape.RoundedSquare -> createRoundedSquareBitmap(square, 432, 48f)
            }

            val icon = IconCompat.createWithBitmap(iconBmp)
            val shortcutId = "custom_launcher"

            // 快捷方式指向当前选中的图标风格（别名/MainActivity）
            val iconStyle = getSharedPreferences("settings", MODE_PRIVATE).getInt("icon_style", 0)
            val base = MainActivity::class.java.name
            val aliasNames = arrayOf(
                base,             // 0
                "${base}Alias",   // 1
                "${base}Alias2",  // 2
                "${base}Alias3",  // 3
                "${base}Alias4",  // 4
                "${base}Alias5",  // 5
                "${base}Alias6",  // 6
                "${base}Alias7"   // 7
            )
            val target = aliasNames.getOrElse(iconStyle) { MainActivity::class.java.name }
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                component = ComponentName(this@MainActivity, target)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            val shortcut = ShortcutInfoCompat.Builder(this, shortcutId)
                .setShortLabel(name.ifBlank { "MizuSU" })
                .setIntent(intent)
                .setIcon(icon)
                .build()

            // 先推送到动态快捷方式（已有 pinned 的会自动刷新图标和名称）
            ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)

            // 检查是否已固定到桌面
            val alreadyPinned = try {
                ShortcutManagerCompat.getShortcuts(this, ShortcutManagerCompat.FLAG_MATCH_PINNED)
                    .any { it.id == shortcutId && it.isEnabled }
            } catch (_: Throwable) { false }

            if (alreadyPinned) {
                Toast.makeText(this, "桌面图标已更新！", Toast.LENGTH_SHORT).show()
            } else {
                if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                    Toast.makeText(this, "当前启动器不支持快捷方式", Toast.LENGTH_LONG).show()
                    return
                }
                val pinned = ShortcutManagerCompat.requestPinShortcut(this, shortcut, null)
                if (pinned) {
                    Toast.makeText(this, "快捷方式已创建！", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "请在弹出的系统对话框中确认添加", Toast.LENGTH_LONG).show()
                }
            }
        } catch (t: Throwable) {
            Log.w("CustomShortcut", "createCustomShortcut failed", t)
            Toast.makeText(this, "快捷方式创建失败: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INTENT_STATE, intentStateValue)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer?.release()
        soundPlayer = null
    }

    private fun bitmapToUri(bmp: Bitmap): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "shortcut_icon_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MizuSU")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw Exception("Failed to create MediaStore entry")
        contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return uri
    }
}

private fun createRoundedSquareBitmap(source: Bitmap, size: Int = 432, cornerRadius: Float): Bitmap {
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = android.graphics.RectF(0f, 0f, size.toFloat(), size.toFloat())
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(source, null, android.graphics.Rect(0, 0, size, size), paint)
    paint.xfermode = null
    return output
}

private fun cropToCircleBitmap(source: Bitmap, size: Int = 432): Bitmap {
    val side = minOf(source.width, source.height)
    val x = (source.width - side) / 2
    val y = (source.height - side) / 2
    val square = Bitmap.createBitmap(source, x, y, side, side)

    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val r = size / 2f

    canvas.drawCircle(r, r, r, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(square, Rect(0, 0, side, side), Rect(0, 0, size, size), paint)
    paint.xfermode = null

    if (square !== source) square.recycle()
    return output
}

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
) {
    val navController = LocalNavigator.current
    val enableBlur = LocalEnableBlur.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { MainPagerConfig.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)
    val isManager = Natives.isManager
    val isFullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    var userScrollEnabled by remember(isFullFeatured) { mutableStateOf(isFullFeatured) }
    val uiMode = LocalUiMode.current
    val surfaceColor = when (uiMode) {
        UiMode.Material -> MaterialTheme.colorScheme.surface // Blur is not used in Material, this is just a placeholder
        UiMode.Miuix -> MiuixTheme.colorScheme.surface
    }
    val blurBackdrop = rememberBlurBackdrop(enableBlur)

    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    MainScreenBackHandler(mainPagerState, navController)

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useNavigationRail = isLandscape && !(uiMode == UiMode.Miuix && enableFloatingBottomBar)

    CompositionLocalProvider(
        LocalMainPagerState provides mainPagerState
    ) {
        val contentReady = rememberContentReady()
        val pagerContent = @Composable { bottomInnerPadding: Dp ->
            Box(modifier = if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier) {
                HorizontalPager(
                    modifier = Modifier
                        .then(if (enableFloatingBottomBar && enableFloatingBottomBarBlur) Modifier.layerBackdrop(backdrop) else Modifier),
                    state = mainPagerState.pagerState,
                    beyondViewportPageCount = if (contentReady) 3 else 0,
                    userScrollEnabled = userScrollEnabled,
                ) { page ->
                    val isCurrentPage = page == settledPage
                    when (page) {
                        0 -> if (isCurrentPage || contentReady) HomePager(navController, bottomInnerPadding, isCurrentPage)
                        1 -> if (isCurrentPage || contentReady) SuperUserPager(navController, bottomInnerPadding, isCurrentPage)
                        2 -> if (isCurrentPage || contentReady) ModulePager(bottomInnerPadding, isCurrentPage)
                        3 -> if (isCurrentPage || contentReady) SettingPager(navController, bottomInnerPadding)
                    }
                }
            }
        }

        if (useNavigationRail) {
            val startInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Start)
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold {
                    Row {
                        SideRail(
                            blurBackdrop = blurBackdrop,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }

                UiMode.Miuix -> Scaffold { _ ->
                    Row {
                        SideRail(
                            blurBackdrop = blurBackdrop,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }
            }
        } else {
            val bottomBar = @Composable {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BottomBar(
                        blurBackdrop = blurBackdrop,
                        backdrop = backdrop,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold(bottomBar = bottomBar) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }

                UiMode.Miuix -> Scaffold(bottomBar = bottomBar) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }
            }
        }
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navController.current() is Route.Main && navController.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        }
    )
}

@Composable
private fun ShortcutIntentHandler(
    intentState: MutableStateFlow<Int>,
) {
    val activity = LocalActivity.current ?: return
    val context = LocalContext.current
    val intentStateValue by intentState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    LaunchedEffect(intentStateValue) {
        val intent = activity.intent
        val type = intent?.getStringExtra("shortcut_type") ?: return@LaunchedEffect

        when (type) {
            "module_action" -> {
                val moduleId = intent.getStringExtra("module_id") ?: return@LaunchedEffect
                navigator.push(Route.ExecuteModuleAction(moduleId, fromShortcut = true))
                intent.removeExtra("shortcut_type")
                intent.removeExtra("module_id")
            }

            "module_webui" -> {
                val moduleId = intent.getStringExtra("module_id") ?: return@LaunchedEffect
                val webIntent = Intent(context, WebUIActivity::class.java)
                    .setData("kernelsu://webui/$moduleId".toUri())
                context.startActivity(webIntent)
            }
            
            else -> return@LaunchedEffect
        }
    }
}