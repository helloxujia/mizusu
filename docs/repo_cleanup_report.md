# MizuSU 仓库脏数据清理报告

## 状态

- 远程最新: `b729cb63` (推送成功)
- 本地 HEAD: `2a790b64` (**3 个提交未推送**)
- 好消息: 脏数据还在本地，GitHub 上**还没有**泄露

## 未推送提交 `cb1cf10b` 中的脏文件

| 文件 | 类型 | 处理 |
|------|------|------|
| `.github_token` | 🚨 GitHub Token | **必须彻底移除**。已在 GitHub 撤销 |
| `.claude.json` | Claude AI 配置 | 加入 .gitignore |
| `.claude/settings.json` | Claude 设置 | 加入 .gitignore |
| `manager/app/src/main/arts.json` | Actions 构建数据 | 加入 .gitignore |
| `manager/app/src/main/assets/test2.zip` | 测试文件 | 加入 .gitignore |
| `manager/app/src/main/assets/test_dl.zip` | 测试文件 | 加入 .gitignore |
| `manager/app/src/main/jniLibs/arm64-v8a/ksud.zip` | 二进制产物 | 加入 .gitignore |
| `docs/issues_v4.1.3_audit.md` | 审计文档 | ✅ 可保留 |
| `userspace/ksud/src/.gitignore` | ksud gitignore | ✅ 可保留 |

## 磁盘上未提交的敏感目录

| 目录 | 内容 | 大小 |
|------|------|------|
| `.codex/` | Codex AI 全量数据(含 `.sandbox-secrets/`, SQLite DB, auth.json) | ~16MB |
| `.claude/` | Claude 会话历史(history.jsonl 753KB), telemetry, tasks | ~1.5MB |
| `.cc-switch/` | cc-switch 数据库 + 5 个备份 + 日志 | ~2.3MB |
| `.codex-session-delete/` | Codex 日志 | 小 |

**`.gitignore` 当前为空文件** — 这是根因。

## 修复方案

1. 创建 `.gitignore` 屏蔽所有 AI 工具/构建产物/密钥
2. 软重置到远程 HEAD (`git reset --soft origin/main`)，保留改动
3. 重新提交，确保不包含脏文件
4. 推送
