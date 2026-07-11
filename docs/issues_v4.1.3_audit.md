# MizuSU v4.1.3 代码审查问题清单

> 审查日期: 2026-06-22 | 审查范围: 安全 + 质量 + 一致性
> 状态: 待修复（第一版完善后着手）

---

## 🔴 严重 (CRITICAL) — 可能导致变砖/崩溃/安全漏洞

### C1. KO 提取失败被静默吞掉，刷入无 LKM 的 boot.img
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 343-363
- **问题**: `assets.open(koName)` 抛异常被 catch 吞掉，继续执行但缺少 `-m` 参数 → LKM 模式设备刷入后无 root
- **修复**: 提取失败时中止操作，通过 `onStderr` 向用户报告明确错误

### C2. KMI 正则匹配失败时硬编码回退到 android12-5.10
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 347-350
- **问题**: 正则 `(\d+)\.(\d+)\.\d+-android(\d+)` 不匹配时静默回退 `"android12-5.10"`，可能刷入完全不兼容的 KO
- **修复**: 匹配失败时中止，显示实际 kernel release 字符串让用户手动选择

### C3. libksud.so 复制路径硬编码 debug
- **文件**: `manager/app/build.gradle.kts`
- **行号**: 106
- **问题**: `val dst = file("$buildDir/intermediates/merged_jni_libs/debug/mergeDebugJniLibFolders/out/arm64-v8a/libksud.so")` — Release 构建路径不同，永远缺 native 库
- **修复**: 动态获取 variant 名称构造路径

### C4. Shell 引号语法错误
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 505-506
- **问题**: `"""... "$id" "$escapedTemplate'""""` — 尾部单引号未匹配，命令静默失败
- **修复**: 使用已有的 `shellArg()` 函数或 libsu 参数数组

### C5. lkmFile 永远为 null + bundledKo 从不删除
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 341, 355, 380
- **问题**: `lkmFile` 初始化为 null 后从不赋值，`lkmFile?.delete()` 是死代码；`bundledKo` 泄露在缓存目录
- **修复**: 统一定义一个变量，flash 后删除临时 KO 文件

### C6. Root 辅助静默绕过系统权限检查
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/module/Shortcut.kt`
- **行号**: 415-422, 355-385
- **问题**: `ensureShortcutPermission()` 静默用 root 执行 `appops set` 自授权，用户完全不知情
- **修复**: 至少显示提示告知用户将使用 root 权限

---

## 🟠 高危 (HIGH) — 严重 bug / 功能破损 / 安全风险

### H1. Main-thread I/O — 启动音效阻塞 UI 线程
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/MainActivity.kt`
- **行号**: 165-181, 188
- **问题**: `assets.openFd(name)` + `MediaPlayer.prepare()` 在主线程，慢存储设备触发 ANR
- **修复**: 移到后台线程或延迟到首帧绘制后

### H2. Alias2~7 缺少 VIEW/SEND/SEND_MULTIPLE intent-filter
- **文件**: `manager/app/src/main/AndroidManifest.xml`
- **行号**: 93-169
- **问题**: 只有 MainActivityAlias(1) 有完整 intent-filter，选图标 2-7 时无法关联打开 ZIP/APK
- **修复**: 为 Alias2~7 复制 VIEW + SEND + SEND_MULTIPLE intent-filter 块

### H3. 全局 Shell 命令注入风险
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt` (多处)
- **行号**: 115-123, 167-190, 524-532, 559-565, 583
- **问题**: 模块 ID、包名、路径等参数直接字符串拼接到 root shell 命令，无任何校验
- **修复**: 对所有外部输入做白名单校验（字母数字+短横+下划线），关键路径用 libsu 参数数组

### H4. 约 25 处硬编码中文字符串
- **文件**: `MainActivity.kt`, `CustomIconMaterial.kt`, `CustomIconMiuix.kt`, `SettingsMaterial.kt`, `SettingsMiuix.kt`, `ShortcutNameDialog.kt`
- **行号**: 多处
- **问题**: Toast/Dialog/UI 标签全硬编码中文，无国际化支持，非中文 locale 显示乱码
- **修复**: 全部迁移到 `res/values/strings.xml`

### H5. SusFS 条目引用了错误的 string resource
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/screen/settings/SettingsMaterial.kt`
- **行号**: 259
- **问题**: `stringResource(id = R.string.settings_kpm_summary)` 应为 `R.string.susfs_config_summary`
- **修复**: 改为正确的 string resource ID

### H6. SettingsViewModel 并发竞态
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/viewmodel/SettingsViewModel.kt`
- **行号**: 多处 setter 函数
- **问题**: 多个 `ksud feature save` 在 `Dispatchers.IO` 并发执行，快速切换设置时内核状态可能不一致
- **修复**: 用 Mutex 或 Channel 序列化 feature 变更

### H7. setSuCompatMode "disable until reboot" 写错持久值
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/viewmodel/SettingsViewModel.kt`
- **行号**: 249-273
- **问题**: mode 1（禁用至重启）写 `su_compat_mode = 0`（启用）到 SP，重启后 su 意外恢复
- **修复**: 区分瞬态和持久态

### H8. setAdbRootEnabled 可能缺 root 权限
- **文件**: `manager/app/src/main/java/com/zayu/mizu/data/repository/SettingsRepositoryImpl.kt`
- **行号**: 152-158
- **问题**: `ShellUtils.fastCmd("setprop ctl.restart adbd")` 单参版本可能不用 root shell
- **修复**: 显式传入 root shell: `ShellUtils.fastCmd(getRootShell(), "setprop ctl.restart adbd")`

### H9. autoJailbreak pref 写入不依赖组件切换成功
- **文件**: `manager/app/src/main/java/com/zayu/mizu/data/repository/SettingsRepositoryImpl.kt`
- **行号**: 100-107
- **问题**: `setComponentEnabledSetting` 抛异常被 catch，但 `putBoolean("auto_jailbreak", value)` 已执行
- **修复**: 只在组件切换成功后写 pref

### H10. bitmap 写入用户可见 MediaStore
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/MainActivity.kt`
- **行号**: 484-494
- **问题**: 快捷方式图标写入 `Pictures/MizuSU/` 公共图库，用户可能反感
- **修复**: 改用 app 私有缓存目录

### H11. cropToCircleBitmap bitmap 泄露
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/MainActivity.kt`
- **行号**: 526
- **问题**: source bitmap 从不回收，square 在 `square === source` 时也不回收
- **修复**: 完善 recycle 逻辑

### H12. 隐藏桌面图标占位功能误导用户
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/MainActivity.kt` + `CustomIconMaterial.kt` + `CustomIconMiuix.kt`
- **行号**: Main L346, CustomIconMaterial L172, CustomIconMiuix L287
- **问题**: UI 中显示可交互的开关/按钮，但点击只弹 Toast "研发中"
- **修复**: 实现功能，或暂时禁用入口（灰色+不可点击）

### H13. Alias 组件名硬编码
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/MainActivity.kt`
- **行号**: 421-430
- **问题**: 数组硬编码 8 个 alias 名，若 Manifest 中不存在对应定义则崩溃
- **修复**: 通过 PackageManager 动态枚举可用 alias

### H14. bin 路径硬编码
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 400, 402
- **问题**: `/system/bin/input`, `/system/bin/svc`, `/system/bin/reboot` 硬编码，GSI/部分设备路径不同
- **修复**: 用 `PATH` 解析或使用 Android API

### H15. concurrent flash module.zip 文件冲突
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 221-233
- **问题**: 临时文件固定为 `module.zip`，两个并发 flash 互相覆盖
- **修复**: 加时间戳或 UUID 唯一文件名

---

## 🟡 中危 (MEDIUM) — 代码质量/一致性/可维护性

### M1. toggleLauncherIcon() 死代码
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/RestartActivityUtils.kt`
- **行号**: 9-25
- **问题**: 只支持二选一切换，多图标系统中完全无用且无调用者
- **修复**: 删除或标记 @Deprecated

### M2. 双主题 checkUpdate/checkVersion 包裹不一致
- **文件**: `SettingsMaterial.kt:108-141` / `SettingsMiuix.kt:128-171`
- **问题**: Material 中三个 toggle 全在 KsuIsValid 内，Miuix 中 checkUpdate 和 checkVersionMatch 在外面。切换 UI 模式时行为不同
- **修复**: 统一包裹策略

### M3. SusFS 可见性门控不一致
- **文件**: `SettingsMaterial.kt:250-271` (双条件) / `SettingsMiuix.kt:306-330` (单条件)
- **问题**: Material 要求 isKpmAvailable + isSusfsSupported；Miuix 只要求 isSusfsSupported
- **修复**: 统一为同一条件

### M4. IconPreset 数据类重复定义
- **文件**: `CustomIconMaterial.kt:188` / `CustomIconMiuix.kt:67-74`
- **问题**: 完全相同的数据类和 8 个预设在两个文件中各定义一份
- **修复**: 提取到共享文件

### M5. showFullStatus 用 key "show_fingerprint"
- **文件**: `manager/app/src/main/java/com/zayu/mizu/data/repository/SettingsRepositoryImpl.kt`
- **行号**: 93-95
- **问题**: getter/setter 用 key `"show_fingerprint"` 但变量名是 `showFullStatus` — 旧功能名残留
- **修复**: 迁移 key 为 `"show_full_status"`，或至少加注释说明

### M6. "TODO" Toast 直接显示给用户
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/component/uninstalldialog/UninstallDialogMiuix.kt`
- **行号**: 47
- **问题**: `Toast.makeText(context, "TODO", ...)` — 用户直接看到 "TODO"
- **修复**: 实现功能或删除该路径

### M7. computePos() 返回 0 的桩函数
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/component/filter/BaseFieldFilter.kt`
- **行号**: 19
- **问题**: `// TODO` 注释的 `computePos()` 始终返回 0
- **修复**: 实现或移除

### M8. 内核编译时 curl GitHub API
- **文件**: `kernel/Kbuild`
- **行号**: 109
- **问题**: `make` 时自动 curl GitHub API 获取最新 release tag，离线/限速时编译失败
- **修复**: 加超时和 fallback

### M9. Alias 名构造方式不一致
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/RestartActivityUtils.kt`
- **行号**: 29-38 (显式列表) / 79-81 (map 拼接)
- **问题**: 同一组 alias 用两种不同方式构造，增删 alias 需改两处
- **修复**: 提取为共享常量列表

### M10. "settings" 字符串重复 3 次
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/MainActivity.kt`
- **行号**: 186, 273, 419
- **问题**: SP 文件名 `"settings"` 硬编码三次
- **修复**: 提取为 companion object 常量

### M11. ksud 静默降级
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 91-113
- **问题**: ksud 失败静默回退到 su → sh，调用者不知道降级了
- **修复**: 至少 log warning，考虑 UI 提示

### M12. getModuleCount() 吞 JSON 解析异常
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 155-161
- **问题**: `runCatching { JSONArray(result) }.getOrElse { return 0 }` — 无法区分"0 模块"和"解析错误"
- **修复**: 区分两种情况，解析错误时 log

### M13. "KernelSU" log tag 残留
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/util/KsuCli.kt`
- **行号**: 228, 256, 377
- **问题**: log tag 仍为 "KernelSU"
- **修复**: 改为 "MizuSU" 或 "KsuCli"

### M14. Repository 层直接执行 root 命令
- **文件**: `manager/app/src/main/java/com/zayu/mizu/data/repository/SettingsRepositoryImpl.kt`
- **行号**: 166-168
- **问题**: 数据层直接 shell out 到 root，违反分层，无法单元测试
- **修复**: 提取到专用 service/use-case 层

### M15. setUiMode 无事务回滚
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/viewmodel/SettingsViewModel.kt`
- **行号**: 119-147
- **问题**: uiMode 写入成功但 themeMode 写入失败时状态不一致
- **修复**: 失败时回滚第一个写入

### M16. setIconStyle 污染 alternativeIcon
- **文件**: `manager/app/src/main/java/com/zayu/mizu/ui/viewmodel/SettingsViewModel.kt`
- **行号**: 164-168
- **问题**: 设 iconStyle 自动覆盖 alternativeIcon，两个独立设置被耦合
- **修复**: 保持独立

---

## 🔵 低危 (LOW) — 清理/优化

### L1. getVersionName() 定义但未调用
- **文件**: `manager/build.gradle.kts:36-38`
- **修复**: 删除或使用

### L2. KSU_EXPECTED_SIZE2/HASH2 代码块重复
- **文件**: `kernel/Kbuild:185-193`
- **修复**: 删除重复块

### L3. auto git fetch --unshallow 副作用
- **文件**: `kernel/Kbuild:106`
- **修复**: 至少加 warning 提示用户

### L4. getGitCommitCount() 在 shallow clone 崩溃
- **文件**: `manager/build.gradle.kts:20`
- **修复**: try/catch + fallback

### L5. 版本偏移量在两处重复定义
- **文件**: `build.gradle.kts:32` / `kernel/Kbuild:91-93`
- **修复**: 提取到共享配置

### L6. iconStyle setter 用 +3 魔法数字
- **文件**: `SettingsViewModel.kt:170-178`
- **修复**: 用命名映射函数

### L7. `remember` import 重复
- **文件**: `MainActivity.kt:56,76`
- **修复**: 删除重复行

### L8. 空的 onDispose
- **文件**: `MainActivity.kt:217`
- **修复**: 实现清理或删除

### L9. Intent 计数器可能溢出
- **文件**: `MainActivity.kt:153`
- **修复**: 用 Long 或 AtomicInteger

### L10. onSaveInstanceState 不完整
- **文件**: `MainActivity.kt:473-475`
- **修复**: 保存更多瞬态

### L11. @Composable 在 utility object 中
- **文件**: `KsuCli.kt:703-714`
- **修复**: 移到 Composable 文件

### L12. SUKISU_KPM_* C 常量名未改
- **文件**: `manager/app/src/main/cpp/uapi/supercall.h:166-172`
- **注意**: 需内核侧协调才能改

### L13. Miuix CustomIcon 的 divider 和 glass 效果 Material 没有
- **文件**: `CustomIconMaterial.kt` / `CustomIconMiuix.kt`
- **修复**: 功能对齐

### L14. Alias2~7 缺少 _round 图标引用
- **文件**: `AndroidManifest.xml:99,112,etc`
- **修复**: 补充或移除 roundIcon 属性

### L15. install() 返回值未被检查
- **文件**: `KsuCli.kt:140-145` → `MainActivity.kt:195`
- **修复**: 检查返回值并处理失败

### L16. 非 suspend JNI 调用风险
- **文件**: `SettingsRepositoryImpl.kt` 多处
- **修复**: 加文档说明需在后台线程调用

---

## ✅ 已验证通过

- ✅ `com.sukisu.ultra` 引用全部清理干净（所有 .kt 文件）
- ✅ 包名迁移 `com.zayu.mizu` 完整

---

## 📊 统计

| 级别 | 数量 |
|------|------|
| 🔴 Critical | 6 |
| 🟠 High | 15 |
| 🟡 Medium | 16 |
| 🔵 Low | 16 |
| **总计** | **53** |

---

## 🔧 建议修复顺序

1. **第一优先级 (C1-C6)**: 变砖/崩溃/安全 — 发布前必修
2. **第二优先级 (H1-H4, H12)**: 所有用户受影响 — ANR、无国际化、假功能
3. **第三优先级 (H5-H11, H13-H15)**: 特定场景触发 — SusFS 错字、竞态、文件泄露
4. **第四优先级 (M1-M16)**: 代码健康 — 死代码、不一致、分层
5. **第五优先级 (L1-L16)**: 锦上添花 — 清理优化
