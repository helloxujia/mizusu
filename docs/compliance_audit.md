# 合规检查笔记

2026-07-11

## 许可证

缺少 COPYING（GPL-2.0），只有 LICENSE（GPL-3.0）。KernelSU 的内核代码是 GPL-2.0-only，应该有双份。——已补上。

ksud 的 Cargo.toml 没写 license 字段。——还没改，下次记得。

内核文件 SPDX 头不全，patch_memory.c 写了但 main.c 和 kpm 没写。——慢慢补。

## CC 图片

原 SukiSU 的图标是怡子曰曰画的，CC BY-NC-SA。我们的主图标换掉了（MD5 跟 icon.png 一致），Alt 系列是自己找的图。风险基本没了。

## 依赖

所有第三方库都是 MIT、Apache-2.0、BSD——跟 GPL 不冲突。这个没问题。

## 代码标记

多数源文件没有修改注释。新加的 fishtoolbox 文件应该加 SPDX 头，忘了。——下次补。

## 安全

之前有个 .github_token 差点推上去，好在 push 失败了。Token 已撤销。

.claude.json 在远程历史里（两个旧提交），但里面只有使用统计和机器 ID，没密钥。

远程 HEAD 现在干净——截图和调试脚本都删了。

## 总结

大问题都处理了。待办：ksud license 字段、SPDX 头、修改注释。不紧急。
