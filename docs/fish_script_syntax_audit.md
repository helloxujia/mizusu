# 杂鱼工具箱 · Shell 语法兼容性审计报告

## 运行环境

| 项目 | 设备 | 脚本要求 |
|------|------|---------|
| Shell | mksh (MirBSD Korn Shell) | `#!/system/bin/sh` |
| PTY | ❌ 无（ProcessBuilder pipe） | ✅ 需要 PTY（read -t, read -n 1, select） |
| curl | ✅ 有 | ✅ |
| wget | ❌ 无 | 有 fallback |
| busybox | ❌ 无 | 有 fallback |
| aapt | ❌ 无 | 有 fallback |

---

## 一、致命问题（必须处理）

### 1. `read -t` — 超时读取
- **用法**: 9处。`auto_detect_with_countdown` 中 `read -t 1 -n 1`
- **mksh 行为**: ❌ mksh 的 `read -t` 含义与 bash 不同。在 pipe 中会立即返回错误。
- **后果**: 倒计时循环可能异常，脚本卡在循环中
- **方案**: 替换 `auto_detect_with_countdown` → 即时检测，跳过倒计时

### 2. Pipe 缓冲（无 PTY）
- **用法**: 全部 stdout
- **行为**: 没有 PTY 时，shell 使用 4KB 块缓冲。菜单打印后不立即 flush。
- **后果**: 用户看到空白界面，输入后菜单突然出现
- **方案**: 终端层每 100ms poll 读取，或使用 `stdbuf -oL`（如果可用）

### 3. `clear` 命令
- **用法**: 1处（`clear_screen()` 内有 fallback）
- **mksh 行为**: 发送 `\033[H\033[2J`，pipe 中也能工作
- **后果**: 无。fallback 已处理。
- **方案**: 无需改动。

### 4. `read -n 1` — 单字符读取
- **用法**: 5处
- **mksh + pipe 行为**: ⚠️ mksh 支持 `read -n 1`，但在 pipe 中字符不会立即到达（需要 \n 刷新？实际上 pipe 是字符设备，单字符应该能到达）
- **后果**: 菜单中按数字键 → 可能需等 Enter。取决于 ProcessBuilder 是否逐字符传递 stdin
- **方案**: 终端的逐字直传方案已处理。写入 Shell stdin 是逐字符的 → Shell 能收到每个字符

---

## 二、需适配的功能

### 5. 自更新模块
- `check_network_connectivity` → curl Gitee 检查网络 → pipe 中能工作
- `fetch_remote_version` → curl 下载 version.json → pipe 中能工作  
- `download_update_file` → curl 下载新脚本 → pipe 中能工作
- **问题不在功能，在冲突**: App 已管理版本，脚本自更新会导致版本混乱
- **方案**: stub 掉，保留函数签名（已完成）

### 6. `auto_detect_with_countdown`
- 10秒倒计时 + `read -t` → mksh 不支持
- **方案**: 替换为即时检测（已完成）

---

## 三、无需改动的部分

| 语法 | 数量 | mksh 支持 | 说明 |
|------|------|-----------|------|
| `[[` | 218 | ✅ | 完全支持 |
| `select` + `PS3` | 13/13 | ✅ | mksh 支持 |
| `read -r` | 175 | ✅ | POSIX 标准 |
| `read -p` | 10 | ✅ | mksh 支持 |
| `local` | 1486 | ✅ | mksh 支持 |
| `<<< here-string` | 35 | ✅ | mksh 支持 |
| `echo -e` | 3050 | ✅ | mksh 支持 |
| `function_*` | 27 | ✅ | 只是命名约定 |
| `curl` | 35 | ✅ 设备有 | — |
| `grep/sed/awk` | 259/74/62 | ✅ 设备有 | — |
| `tar/unzip/dd/du` | — | ✅ 设备有 | — |

---

## 四、精简脚本适配方案

> **原则**: 只砍纯装饰性内容，不砍函数。用 stub（保留签名+return）替代函数体删除。

| 操作 | 目标 | 方式 | 行数影响 |
|------|------|------|----------|
| stub 自更新函数 | L128-850 内的8个函数 | 保留名+return | ~700→~8 |
| 简化 countdown | `auto_detect_with_countdown` | 即时检测 | ~80→~4 |
| 精简初始注释 | L1-103 中的 ASCII art | 保留颜色变量 | ~80 |
| 精简佛祖动画 | L3351-3474 | — | ~124 |

**保留100%**: `print_install_say`, `download_module_from_cloud`, `install_cloud_module`, 全部 feature/slot 函数, 主菜单, 颜色定义

---

## 五、终端层需要的改进

| 问题 | 当前状态 | 需要改进 |
|------|---------|----------|
| Pipe 缓冲 | BufferReader 行读取 | ✅ 已逐块读取(4096字) |
| 逐字输入 | BasicTextField → writeStdin(char) | ✅ 已实现 |
| IME 遮挡 | imePadding | ✅ 已实现 |
| 状态栏遮挡 | statusBarsPadding | ✅ 已实现 |
| 错误处理 | try-catch + errorMessage | ✅ 已实现 |
| ANSI 渲染 | parseAnsiToAnnotated | ✅ 已实现 |
