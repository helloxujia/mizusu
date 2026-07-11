# MizuSU 开源合规审计报告

审计时间: 2026-07-11 | 审计范围: `D:/projects/SukiSU-Ultra`

---

## 一、许可证核心义务

| # | 检查项 | 状态 | 发现 |
|---|--------|------|------|
| 1 | 内核 GPL-2.0 声明 | ⚠️ 部分 | `patch_memory.c` 有 SPDX，但 `kpm.c` 写成 "GPL-2.0-or-later"（应为 "only"），`main.c` 无头部声明 |
| 2 | 内核未改 GPL-3.0 | ✅ | 未发现 |
| 3 | 用户空间 GPL-3.0 | ❌ | **ksud `Cargo.toml` 缺少 license 字段**；多数 .kt/.rs 文件无 SPDX 头 |
| 4 | 未删除原许可证 | ✅ | `LICENSE` 文件完整 |
| 5 | CC BY-NC-SA 图片 | ❌ 高风险 | 原 SukiSU 的 `ic_launcher` 系列图片受 怡子曰曰+明风OuO 版权保护，需要**联系作者获得授权或替换为自有素材**。Alt 图标 (`ic_launcher_alt*`) 可能另算，需确认 |
| 6 | GPL-2.0 文本 | ❌ | **缺少 `COPYING` (GPL-2.0) 文件**，仅有 `LICENSE` (GPL-3.0) |
| 7 | GPL-3.0 文本 | ✅ | `LICENSE` 包含 GPL-3.0 全文 |
| 8 | 公开仓库 | ✅ | `github.com/helloxujia/mizusu` |
| 9 | 源码二进制对应 | ⚠️ | APK 通过 GitHub Actions 构建，需验证二进制与 tag/commit 对应 |
| 10 | 未修改许可证文本 | ✅ | |

---

## 二、第三方依赖与许可证兼容性

| 库 | 许可证 | GPL兼容 |
|----|--------|---------|
| `libsu` (topjohnwu) | Apache-2.0 | ✅ |
| `miuix` (yukonga) | Apache-2.0 | ✅ |
| `material-kolor` | MIT | ✅ |
| `hiddenapibypass` (LSPosed) | Apache-2.0 | ✅ |
| `okhttp` | Apache-2.0 | ✅ |
| `commonmark` | BSD-2 | ✅ |
| `appiconloader` | Apache-2.0 | ✅ |
| `jackpal/Android-Terminal-Emulator` | Apache-2.0 | ✅ |
| `gson` | Apache-2.0 | ✅ |

| # | 检查项 | 状态 |
|---|--------|------|
| 11 | 许可证扫描 | ❌ 未进行 |
| 12 | 新增依赖 GPL-compatible | ✅ 所有依赖许可证兼容 |
| 13 | GPL-2.0-only vs GPL-3.0 冲突 | ⚠️ 内核 (GPL-2.0-only) 和用户空间 (GPL-3.0) 分离编译，不链接，默认合规 |
| 14 | SSPL/AGPL | ✅ 未发现 |
| 15 | NOTICE 文件 | ❌ **文件不存在** |
| 16 | 构建脚本 | ✅ 未发现不兼容选项 |

---

## 三、AI 代码与安全

| # | 检查项 | 状态 | 发现 |
|---|--------|------|------|
| `.github_token` | **GitHub Token 泄露！** | 🚨 | 仓库中 `.github_token` 文件包含明文 Token (`ghp_PMpM...`)，**立即撤销并在 GitHub 重新生成**，将文件加入 `.gitignore` |
| `manager/app/src/main/arts.json` | GitHub Actions 产物 | ⚠️ | 包含构建产物数据，非源码 |
| `manager/app/src/main/assets/test*.zip` | 测试文件 | ⚠️ | 测试用 zip 文件，不必要 |

---

## 四、代码标记与差异化

| # | 检查项 | 状态 |
|---|--------|------|
| 23 | 修改注释 | ❌ 多数文件无修改注释 |
| 24 | 注释格式 | ❌ 未实施 |
| 25 | App 名称 | ✅ `MizuSU`（区别于 `SukiSU`） |
| 26 | 包名 | ✅ `com.zayu.mizu`（区别于 `com.sukisu.ultra`） |
| 27 | 启动图标 | ✅ 已更换 |
| 28 | NOTICE 文件 | ❌ 不存在 |

---

## 五、文档与声明

| # | 检查项 | 状态 |
|---|--------|------|
| 29 | 基于 SukiSU 声明 | ⚠️ README 有"KernelSU 的第三方美化分支"但未明确写"SukiSU" |
| 30 | AI 辅助声明 | ❌ 未提及 |
| 31 | 非官方版本 | ⚠️ 仅在副标题提及 |
| 32 | 免责声明 | ❌ 无 |
| 33 | 许可证列表 | ❌ 仅 badge 显示 GPL v3 |
| 34 | 致谢原项目作者 | ❌ 未提及 rsuntk/SukiSU 原作者 |
| 35 | 完整使用说明 | ⚠️ 只有功能列表 |

---

##六、优先修复清单

### 🚨 立即（安全/法律风险）

1. **撤销 GitHub Token**：`.github_token` 已泄露，立即到 GitHub Settings → Developer settings → Tokens 撤销该 token，重新生成
2. **删除 token 文件**：`git rm .github_token && echo '.github_token' >> .gitignore`
3. **CC 图片**：决定方案 A（替换自有素材）或 B（联系作者获取授权）

### ⚠️ 本周

4. 创建 `COPYING` (GPL-2.0) 文件
5. 创建 `NOTICE` 文件列出所有第三方依赖及许可证
6. ksud `Cargo.toml` 添加 `license = "GPL-3.0-or-later"`
7. README 补充：AI 使用声明、免责声明、致谢、许可证列表
8. 内核文件统一 SPDX 标识（GPL-2.0-only）

### 📋 后续

9. CI 集成许可证扫描
10. 源码头部添加修改注释
11. 创建 SBOM
