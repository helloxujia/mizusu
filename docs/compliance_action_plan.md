# MizuSU 许可证合规修复方案

> 基于 GitHub 社区标准、KernelSU 生态规范、GNU GPL 法律要求

---

## 一、许可证文件（必须）

| 操作 | 说明 |
|------|------|
| 保留 `LICENSE` | 已有 GPL-3.0 全文 ✅ |
| 新建 `COPYING` | GPL-2.0 全文（内核部分许可证） |
| 新建 `NOTICE` | 列出所有第三方依赖及许可证 |

**NOTICE 模板**：
```
MizuSU — KernelSU 美化分支
Copyright (C) 2024-2026 <Your Name>

This product includes software developed by:
- KernelSU (tiann/weishu) — GPL-2.0 / GPL-3.0
- SukiSU-Ultra (rsuntk) — GPL-3.0
- libsu (topjohnwu) — Apache-2.0
- Miuix (yukonga) — Apache-2.0
- material-kolor — MIT
- jackpal/Android-Terminal-Emulator — Apache-2.0
- okhttp (Square) — Apache-2.0
- commonmark — BSD-2-Clause
- hiddenapibypass (LSPosed) — Apache-2.0
- appiconloader (zhanghai) — Apache-2.0
```

## 二、许可证头声明（推荐）

内核文件头部添加 SPDX 标识：
```c
// SPDX-License-Identifier: GPL-2.0-only
```

Kotlin/Java 文件头部：
```kotlin
// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MizuSU Contributors
```

**优先级**：新文件必须加，旧文件逐步补

## 三、CC BY-NC-SA 图片（高优先级）

> **依据**：SukiSU 原文档明确要求："需要联系作者获得授权才能使用这些艺术内容"

| 方案 | 操作 | 风险 |
|------|------|------|
| A（推荐） | 删除所有 `ic_launcher` 原图，替换为自有素材 | 零风险 |
| B | 联系怡子曰曰(@bilibili/10545509) + 明风OuO(@bilibili/274939213)获取授权 | 不确定能否得到回复 |

**当前状态**：MizuSU 已使用自定义 Alt 图标（`ic_launcher_alt*`），但根 `ic_launcher.webp` 若仍是原素材则需替换。

## 四、README 补充（推荐）

基于社区研究，README 应包含：

```markdown
## 声明
- **本项目是 [SukiSU](原链接) 的第三方修改版本，非官方发布**
- **遵循原项目的 GPL-2.0/GPL-3.0 许可证**
- **使用风险自负，本项目不提供任何担保**

## AI 辅助开发声明
本项目在开发过程中使用了 AI 编程助手（Claude Code）。
所有 AI 生成的代码均经过人工审查、测试和修改。
若发现问题，欢迎提交 Issue。

## 致谢
- 原 KernelSU 项目 (tiann/weishu)
- SukiSU-Ultra (rsuntk)
- 图标设计: 怡子曰曰, 明风 OuO, @MiRinChan
- 所有贡献者

## 许可证
| 代码区域      | 许可证              |
|---------------|---------------------|
| kernel/       | GPL-2.0-only        |
| 其他所有代码  | GPL-3.0-or-later    |
| 图片/艺术资源 | CC BY-NC-SA 4.0 (或替换后移除此行) |
```

## 五、AI 使用规范（推荐）

1. **`CONTRIBUTING.md`** 中明确 AI 政策：
   - AI 代码须标注 `Assisted-by: Claude Code`
   - 人工审查后方可合并
   - 不接受纯 AI 生成的 Issue/PR

2. **PR 模板**：添加 AI 使用声明栏

## 六、安全检查（必须）

- [x] `.github_token` — 已在设备端删除，需确认远程 GitHub 已撤销
- [x] `.gitignore` — 已创建，屏蔽 AI 工具数据/密钥/构建产物
- [ ] 远程仓库确认无 Token 泄露

---

## 修复顺序

1. 🚨 撤销旧 GitHub Token → 已完成（`ghp_PMpM...` 无效）
2. 创建 `COPYING` + `NOTICE` 文件
3. 确认图片已替换（检查 `ic_launcher.webp`）
4. 更新 `README.md`
5. 创建 `CONTRIBUTING.md`（AI 政策）
6. 新建 PR 模板
7. 推送
