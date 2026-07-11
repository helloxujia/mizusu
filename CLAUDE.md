# CLAUDE.md — MizuSU (杂鱼MizuSU) Project

> 每次新会话自动加载此文件。保持更新，记录一切关键信息。

## 项目身份

| 项 | 值 |
|---|-----|
| **名称** | MizuSU / 杂鱼杂鱼MizuSU |
| **类型** | KernelSU (KSU) 第三方 UI 美化分支 |
| **包名** | `com.zayu.mizu` |
| **版本** | v4.1.3 (manager code 40817, kernel 40798) |
| **许可** | GPL v3.0 |
| **仓库** | `https://github.com/helloxujia/mizusu` |
| **本地** | `E:\projects\SukiSU-Ultra\` |
| **基础** | 基于 SukiSU-Ultra → KernelSU (tiann/weishu) |
| **维护者** | 酷安 @民間の人民の利益を取る |

## 技术栈

- **语言**: Kotlin (Android), C++/C (JNI/内核)
- **UI**: Jetpack Compose — **双主题**: Material Design 3 + Miuix (小米澎湃风格)
- **架构**: MVVM (ViewModel + Repository + UiState)
- **导航**: Jetpack Navigation Compose (`ui/navigation3/`)
- **构建**: Gradle 9.5.1 + Kotlin DSL + CMake
- **Android SDK**: min 26, target 37, compile 37, NDK r27c
- **关键依赖**: libsu (topjohnwu), miuix-kmp, okhttp, hiddenapibypass, commonmark
- **内核**: Kbuild (GKI 5.10/5.15/6.1/6.6/6.12)

## 目录结构

```
E:\projects\SukiSU-Ultra\
├── CLAUDE.md              ← 你在看这个
├── manager/               ← Android 管理器 App
│   ├── build.gradle.kts   ← 根构建 (版本号在此!)
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/
│   │       ├── AndroidManifest.xml  ← 8 个 activity-alias
│   │       ├── cpp/                 ← JNI native (jni.cc, ksu.cc, adbroot.cc)
│   │       ├── java/com/zayu/mizu/
│   │       │   ├── KernelSUApplication.kt
│   │       │   ├── Kernels.kt / Natives.kt
│   │       │   ├── data/
│   │       │   │   ├── model/       ← AppInfo, Module, RepoModule etc
│   │       │   │   └── repository/  ← Settings/Moudule/SuperUser/Kpm/Template
│   │       │   ├── profile/         ← Capabilities, Groups
│   │       │   └── ui/
│   │       │       ├── MainActivity.kt  ← 核心 (启动音效、导航、图标切换)
│   │       │       ├── theme/           ← Material + Miuix 双主题
│   │       │       ├── navigation3/     ← Routes.kt
│   │       │       ├── component/       ← 可复用 Compose 组件
│   │       │       │   ├── material/    ← Material 风格组件
│   │       │       │   ├── miuix/       ← Miuix 风格组件 + 液态玻璃效果
│   │       │       │   └── liquid/      ← 毛玻璃/果冻动画
│   │       │       ├── screen/          ← 页面 (每页 *Material + *Miuix + *Screen + *UiState)
│   │       │       │   ├── home/        ← 首页 (工作状态/LKM/模块)
│   │       │       │   ├── settings/    ← 设置 (音效/版本检查/图标)
│   │       │       │   ├── about/       ← 关于页
│   │       │       │   ├── customicon/  ← 自定义图标选择器
│   │       │       │   └── kernelFlash/ ← AnyKernel3 刷入
│   │       │       └── viewmodel/
│   │       ├── res/
│   │       │   ├── mipmap-anydpi-v26/   ← 自适应图标 XML
│   │       │   ├── drawable/            ← 图标 foreground PNG
│   │       │   └── values/strings.xml
│   │       └── assets/
│   │           ├── sounds/              ← 11 个 MP3 音效
│   │           └── *_kernelsu.ko        ← 7 个 KMI 内核模块
│   └── jniLibs/arm64-v8a/
│       └── libksud.so      ← KSU 守护进程 (已二进制补丁)
├── kernel/                 ← 内核模块 Kbuild
│   ├── Kbuild              ← 签名值、版本号、包名配置
│   └── core/init.c
├── userspace/              ← ksud Rust 源码
├── scripts/                ← 构建/部署脚本
├── .github/workflows/      ← CI/CD (Build Manager + Build LKM)
└── README.md
```

## 架构要点

### 双主题系统
- 每个页面有 `*Material.kt` (Material 3) 和 `*Miuix.kt` (小米主题) 两个实现
- 通过 `LocalUiMode.current` 决定渲染哪个
- Material 用 `SegmentedSwitchItem`，Miuix 用 `SwitchPreference`，**不可互换**

### 图标切换 (Activity-Alias 机制)
- `AndroidManifest.xml` 中定义 8 个 `activity-alias` (MainActivityAlias ~ Alias7)
- `RestartActivityUtils.setLauncherIconStyle()` 通过 `PackageManager.setComponentEnabledSetting` 切换
- 桌面图标用 `{name}_fg.png` (72px 安全区内边距)，预览用 `{name}.png` (全填充)

### Manager 激活验证
- 内核模块 (Kbuild) 编译时嵌入: `KSU_EXPECTED_SIZE=0x2e8` + `KSU_EXPECTED_HASH=defc6d23...` + `KSU_MANAGER_PACKAGE=com.zayu.mizu`
- 运行时通过 ioctl 验证 APK 签名证书 → 匹配才显示"工作中"

### 设置项添加流程
1. `SettingsRepository` → 加字段
2. `SettingsRepositoryImpl` → 加 SharedPreferences 读写
3. `SettingsUiState` → 加 data class 字段 + 回调
4. `SettingsViewModel` → 加初始化 + 更新函数
5. `SettingsScreen` → 加回调绑定
6. `SettingsMaterial` / `SettingsMiuix` → 加 UI

## 开发工作流

### 本地构建
```bash
cd E:\projects\SukiSU-Ultra\manager
./gradlew assembleDebug
# APK 输出: app/build/outputs/apk/debug/MizuSU_v4.1.3_<code>-debug.apk
```

### Git Push (需要代理)
```bash
cd E:\projects\SukiSU-Ultra
git add -A && git commit -m "..."
all_proxy="socks5://127.0.0.1:10808" git push https://helloxujia:TOKEN@github.com/helloxujia/mizusu.git HEAD:main
```

### GitHub Actions
- Push → 自动触发 **Build Manager** (编译 APK)
- **Build LKM** 需手动触发 (github.com/helloxujia/mizusu/actions → Run workflow)
- 内核模块必须在 GitHub Actions 编译 (Windows/WSL 无法本地编译)

### 设备部署
```bash
# 安装 APK
adb push <apk> /data/local/tmp/mizusu.apk
adb shell pm install -r /data/local/tmp/mizusu.apk

# 刷新内核模块 (如需)
adb push android12-5.10_kernelsu.ko /sdcard/mizusu.ko
adb shell "su -c 'ksud boot-patch -b /dev/block/by-name/boot_a -m /sdcard/mizusu.ko -o /data/local/tmp/out/'"
# --out 参数是目录，不是文件路径!
adb shell "su -c 'dd if=/data/local/tmp/out/kernelsu_patched_*.img of=/dev/block/by-name/boot_a bs=4096 && reboot'"
```

## 合规规则

大改动后检查这几项，通过再提交：

- [ ] `.gitignore` 拦截了 AI 工具目录和密钥文件
- [ ] `git diff --cached` 里没有 token、.claude.json、.apk/.so 等
- [ ] 新文件加了 SPDX 头 (kernel: GPL-2.0-only, 其他: GPL-3.0)
- [ ] 第三方库都记在 NOTICE 里
- [ ] 图片是自己的，没有 CC 版权问题

提交信息格式: `feat/fix/chore: 干了什么`  (AI 辅助的加个 `AI-assisted` 就行)

### 已完成
- [x] 包名重塑 com.sukisu.ultra → com.zayu.mizu
- [x] 双主题 (Material + Miuix) 全页面
- [x] 8 个自定义图标 + 运行时切换
- [x] 11 个随机启动音效 + 开关
- [x] 桌面快捷方式 (ShortcutManagerCompat)
- [x] 首页欢迎提示 + 版本不匹配警告
- [x] 关于页
- [x] 全 7 个 KMI 内核模块编译通过 (android12-5.10 ~ android16-6.12)
- [x] GitHub Actions CI/CD
- [x] libksud.so 二进制补丁 (3 处 HEX 替换)
- [x] PJE110 设备激活成功
- [x] Git 历史清理 (移除 .so/.ko/.apk 二进制)

### 待完成
- [ ] 自定义图片上传作为图标 (入口已建，upload 逻辑未实现)
- [ ] 毛玻璃/液态玻璃效果移植 (FolkPatch `GlassEffectHelper`)
- [ ] Miuix 关于页 (`AboutMiuix.kt`) 未完善
- [ ] 桌面图标仍是 SukiSU 矢量画风格，需重绘
- [ ] GitHub Token 需轮换 (已在对话和记忆中明文暴露)

## 常见坑

| 坑 | 解决方案 |
|----|---------|
| **改了 Kbuild 必须全部重建 7 个 KMI** | 只替换一个 KO → 其他 KMI 用旧值 → 其他设备不工作 |
| **`--out` 参数是目录** | `ksud boot-patch -o /data/local/tmp/out/` 不是文件路径 |
| **OPPO Pad 2 用 `boot_a`** | 64MB，不是 `init_boot_a` (8MB) |
| **Git push 必须用 `all_proxy`** | 不能用 `git config http.proxy` |
| **KO 编译必须走 GitHub Actions** | Windows/WSL 都无法本地编译内核模块 |
| **`startForegroundService` 闪退** | 改用 `startService` |
| **Miuix 组件不能混用** | Material 用 `SegmentedSwitchItem`，Miuix 用 `SwitchPreference` |
| **部署后必须重启设备** | ZygiskNext 缓存旧 .so，force-stop 无效 |
| **`KSU_MANAGER_PACKAGE` 必须设置** | 否则内核只检查签名不检查包名 |
| **阿里云 OSS 图片压缩参数** | URL 后面加 `?x-oss-process=image/resize,s_100` |

## 相关项目

| 项目 | 路径 | 说明 |
|------|------|------|
| **Luna 1435 逆向** | `C:\temp\apk_tools\analysis_luna1435\` | Root 检测引擎逆向分析，2232 字符串已 dump |
| **selinux_hide 模块** | `E:\projects\device-attest\ksu-selinux-hide-module\` | Zygisk 17-hook 隐藏模块 (PJE110 生产运行) |
| **FolkPatch 参考** | `D:\projects\FolkPatch\` | UI 设计参考 (毛玻璃/果冻效果) |

## 记忆文件

关键信息已持久化在 `D:\Users\keike\.claude\projects\C--Users-keike\memory\`:
- `mizusu_ops_manual.md` — 完整操作 SOP
- `sukisu_ui_fork_design.md` — UI 美化设计文档
- `mizusu_icon_customizer_plan.md` — 自定义图标规划

以及 `D:\Users\keike\.claude\projects\C--Windows-system32\memory\`:
- `luna1435_re_project.md` — 逆向工程进度
- `pje110_device_profile.md` — 主力设备档案
- `selinux_hide_session_20260528.md` — 17 hooks 最终状态
- `reverse_engineering_lessons_20260514.md` — 逆向经验
- `zygisk_inline_hook_lessons.md` — inline hook 开发经验
