# 杂鱼工具箱 Shell 脚本 · mksh 兼容改造规格书

## 目标

将 `杂鱼工具箱3.0.sh`（为 Termux/bash 编写，~24,000行）改造为 Android `/system/bin/sh`（mksh R59）可执行，**在嵌入式终端环境**中正常工作。

## 运行环境

| 项目 | 值 |
|------|-----|
| Shell | `/system/bin/sh` = mksh R59 (MirBSD Korn Shell) |
| 终端 | 无 PTY，ProcessBuilder pipe 桥接 |
| stdin | 逐字符传入（用户每按一个键立即到达 shell） |
| stdout | 逐块读取（4096 字节缓冲区） |
| Root | 设备已 root（KernelSU），脚本内 `su` 不可用，curl 可用 |
| 工作目录 | `/data/data/com.zayu.mizu/files/fish_toolbox/` |

## 功能要求

### 脚本启动流程
1. 用户从 MizuSU 管理器 App 点击进入 → 嵌入式终端自动执行此脚本
2. 脚本启动后应**直接进入主菜单**（`show_fish_ui` 中的 `select` 交互菜单）
3. **不要**执行 10 秒倒计时（`auto_detect_with_countdown` 已简化）
4. **不要**检查云端更新（自更新函数已 stub）
5. 应正常执行管理器检测（KSU/Magisk/APatch），然后展示功能菜单

### 必须保留的核心功能（100%不动）
- 主菜单 `select` + `PS3`（用户键入数字+回车选择功能）
- 所有 `function_slot*` 和 `function_feature*`（具体功能函数）
- `function_slot_manager`（槽位管理菜单）
- `function_slot3_hide`（伪装卸载管理）
- `download_module_from_cloud`（云端模块下载——从 Gitee 拉 ZIP 包）
- `install_cloud_module`（模块安装——根据 KSU/Magisk/APatch 类型安装）
- `check_and_clean_existing_modules`（已有模块检测清理）
- `print_install_say` 及所有 UI 工具函数
- 所有颜色变量、样式变量
- 网络检测函数（部分功能需要 curl 下载）
- `/data/adb/` 路径访问

### 必须禁用的功能
- **脚本自更新**: 脚本内有一套完整的自更新系统（拉取 version.json → 下载新脚本 → 替换自身）。MizuSU App 已通过自己的 version.json 管理脚本版本，脚本内自更新会导致版本冲突。已通过 stub 处理。
- **10 秒倒计时**: `auto_detect_with_countdown` 使用 `read -t` 实现倒计时自动检测。mksh 的 `read -t` 行为与 bash 不同，在 pipe 中会立即返回错误。已简化。
- **扩展包检测**: 脚本尝试检测 Termux 扩展包，在非 Termux 环境无意义。已 stub。

### 脚本入口
- 脚本末尾有顶层 `main` 调用，是执行入口
- 不要改变入口方式

## 问题函数影响分析

以下是脚本中**实际会导致运行失败或行为异常**的函数，需要修改/禁用：

### 需要 stub 的函数（自更新链）

| 函数 | 行号 | 问题 | 处理 |
|------|------|------|------|
| `check_network_connectivity()` | L153 | 启动时调用，curl 检测网络。pipe 中可工作但无意义 | stub: `return 1` |
| `fetch_remote_version()` | L182 | 拉取 Gitee version.json，下载新脚本。与 App 版本管理冲突 | stub: `return 1` |
| `download_update_file()` | L295 | curl 下载新脚本覆盖自身。App 已管理版本 | stub: `return 1` |
| `perform_update_process()` | L444 | 自更新主流程：停止旧进程→下载→替换→重启。会导致脚本被替换后 App 不知道 | stub: `return 1` |
| `log_update_event()` | L365 | 写更新日志到文件，无功能影响 | stub: `return 0` |

**不需要 stub 但会被间接影响的函数：**

| 函数 | 行号 | 影响 | 处理 |
|------|------|------|------|
| `extract_json_string()` | L166 | 仅被 `fetch_remote_version` 调用，后者 stub 后无影响 | 不动 |
| `compare_versions()` | L216 | 被更新函数调用，也使用了 `=~` | 仅做 `=~` 替换 |
| `show_update_dialog()` | L267 | 仅被更新流程调用 | 不动 |
| `execute_update_replace()` | L321 | 仅被更新流程调用 | 不动 |
| `force_update_toolbox()` | L506 | 仅被更新流程调用 | 不动 |

### 需要简化的函数（mksh 不兼容）

| 函数 | 行号 | 问题 | 处理 |
|------|------|------|------|
| `auto_detect_with_countdown()` | L4102 | `read -t 1 -n 1` 是 bash 专有，mksh 行为不同会导致 10 秒卡死 | 替换为即时检测（sleep 0.3） |

### 使用 `=~` 的函数（~20+ 个，遍布全脚本）

`=~` 是 bash 正则运算符，mksh 不支持。涉及函数（部分列表）：

| 函数 | 典型用法 | 影响 |
|------|---------|------|
| `function_feature1()` 及其子函数 | `=~ ^[Yy]$` 确认提示 | 用户按 y/n 时报语法错误 |
| `function_slot3_hide()` | `=~ \.apk\.[0-9]+$` 文件名匹配 | 伪装卸载功能失效 |
| `function_feature2()` | `=~ \.(img\|bin\|mbn\|elf)$` 文件类型检查 | fastboot 刷写功能失效 |
| `download_module_from_cloud()` | `=~ filename=.*\.zip` URL解析 | 云端模块下载失败 |
| `install_cloud_module()` | `=~ ^[0-9]+$` 数值验证 | 模块安装选项失效 |
| 散落各处的确认提示 | `=~ ^[Yy]$` / `=~ ^[Nn]$` | 多处交互菜单报错退出 |

### 完全无害的函数（不处理）

- `print_install_say()` / `print_success_say()` / `print_fail_say()` — 纯 echo 输出
- `wait_animation()` / `typewriter_effect()` 等 — 纯视觉效果
- `check_root()` — 基础权限检查
- `clear_screen()` — 已有 ANSI fallback
- 所有 `function_feature*` / `function_slot*` — 核心功能，仅内部有 `=~` 需替换
- `main()` — 入口函数

## 输入文件

原始脚本，UTF-8 编码，`\r\n` 行尾。

## 改造步骤（按顺序执行）

### Step 1: 修复行尾

```
所有 \r\n → \n
所有 \r   → \n
```

### Step 2: 在 #!/system/bin/sh 后插入 extglob 开关

```bash
#!/system/bin/sh
shopt -s extglob 2>/dev/null || true
```

> 注意：整个文件只有**第一行**的 `#!/system/bin/sh` 是真正的 shebang。文件中间还有一处 `#!/system/bin/sh` 在函数体内，不要动。

### Step 3: 替换所有 bash `=~` 正则运算符

mksh 的 `[[ ]]` 支持 extglob 模式但不支持 `=~`。将脚本中**全部** `[[ ... =~ ... ]]` 替换为 extglob 等价形式。

如有 `!` 取反，一并转换。

**转换表：**

| bash regex | mksh extglob |
|---|---|
| `=~ ^[Yy]$` | `== [Yy]` |
| `=~ ^[Nn]$` | `== [Nn]` |
| `=~ ^[0-9]+$` | `== +([0-9])` |
| `=~ \.zip$` 或 `=~ ^(.+)\.zip$` | `== *.zip` |
| `=~ \.ZIP$` | `== *.ZIP` |
| `=~ \.zip\?` | `== *.zip?*` |
| `=~ \.(img\|bin\|mbn\|elf)$` | `== *.@(img\|bin\|mbn\|elf)` |
| `=~ \.apk\.[0-9]+$` | `== *.apk.+([0-9])` |
| `=~ ^https?://` | `== http?(s)://*` |
| `=~ /zip/` | `== */zip/*` |
| `=~ filename=.*\.zip` 或 `=~ filename=([^&]+\.zip)` | `== *filename=*.zip*` |
| `=~ filename=.*%5[Bb]%` | `== *filename=*%5[Bb]%*` |
| `=~ keybox\|Keybox` | `== *@(keybox\|Keybox)*` |
| `=~ ^\.` | `== .*` |
| `=~ \..+$` | `== *.?*` |
| `=~ extracting:\ (.*)$` | `== *extracting:*` |

**数组包含检查**（特殊处理）：

```bash
# 原:
[[ ! " ${clean_items[@]} " =~ " ${matched} " ]]
# 改:
! echo " ${clean_items[*]} " | grep -q " ${matched} "
```

```bash
# 原:
[[ " ${local_files[@]} " =~ " ${file} " ]]
# 改:
echo " ${local_files[*]} " | grep -q " ${file} "
```

**BASH_REMATCH 捕获组**（特殊处理）：

```bash
# 原: BASH_REMATCH[1] 配合 [[ =~ filename=([^&]+\.zip) ]]
# 改: 在 =~ 替换为 == 后，原来的捕获组提取逻辑需要改写
#     ${BASH_REMATCH[1]} → $(echo "$download_url" | sed 's/.*filename=\([^&]*\.zip\).*/\1/')
# 整个脚本搜索 BASH_REMATCH，全部替换为 sed 提取写法
```

### Step 4: 简化 `auto_detect_with_countdown()`

找到函数定义，将其函数体整个替换为：

```bash
auto_detect_with_countdown() {
    echo -e "${GREEN}[MizuSU] 自动检测Root环境...${NC}"
    sleep 0.3
}
```

> 原因：原函数使用 `read -t 1 -n 1`（bash 超时读取），mksh 行为不同，会卡死。

### Step 5: stub 自更新函数

找到以下函数的定义（均在前 2000 行内），将函数体替换为一行 `return`：

```bash
check_network_connectivity() { return 1; }
fetch_remote_version()       { return 1; }
download_update_file()       { return 1; }
perform_update_process()     { return 1; }
log_update_event()           { return 0; }
```

> 说明：这些函数实现脚本自更新逻辑（拉取版本 JSON → 下载新脚本 → 替换自身）。原脚本以 root 运行，新脚本下载到 /data/local/tmp 后替换自身。但在嵌入 MizuSU 管理器后，版本由 App 管理，脚本自更新会导致版本冲突。只需保留函数签名（让调用方不报错），体换成 return。

**不要去 stub 以下内容：**
- `print_install_say`（UI工具函数）
- `download_module_from_cloud`（云端模块下载）
- `install_cloud_module`（模块安装）
- `compare_versions`（版本比较，仅去 `=~` 即可）

### Step 6: 验证

- `bash -n` 或 `sh -n` 语法检查
- 确认 `=~` 已清零
- 确认 `BASH_REMATCH` 已清零
- 确认 `\r` 已清零

## 输出

改造后的脚本文件，UTF-8 编码，LF 行尾。
