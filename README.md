<p align="center">
  <img src="icon.png" width="120" alt="MizuSU Icon" />
</p>

<h1 align="center">MizuSU — 杂鱼~杂鱼~♪</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Version-v4.1.3-ff69b4?style=flat-square" />
  <img src="https://img.shields.io/badge/KernelSU-v3.2.4-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/License-GPL%20v3-green?style=flat-square" />
  <img src="https://img.shields.io/badge/Android-8.0+-brightgreen?style=flat-square" />
</p>

<p align="center"><b>KernelSU 第三方分支 · 美化 · 工具箱 · 一键配置</b></p>

---

## 这是什么

MizuSU 是 KernelSU 的第三方美化分支。在保留 KSU 完整 Root 管理能力的基础上，重做了整套 UI，内置了常用工具箱和一键配置。

- **美化** — 双主题（Miuix / Material 3）、8 套图标、毛玻璃效果、启动音效
- **工具箱** — 内置杂鱼工具箱，设备检测、模块管理、伪装卸载一站式
- **一键配置** — 常用配置开箱即用，不用再手动敲命令

内核部分不动，所有 Root 能力来自 KernelSU 官方驱动。

## 特色

| 分类 | 功能 |
|------|------|
| 🎨 美化 | Miuix（小米澎湃）/ Material 3 双主题、液态玻璃、8 套图标 |
| 🐟 工具箱 | 内置杂鱼工具箱：Luna/春秋/Hunter 检测、模块管理、伪装卸载 |
| 🔧 一键配置 | 常用隐藏配置、SELinux 策略、SuSFS 开关 |
| 🔊 音效 | 11 种随机启动音效，可关闭 |
| 📱 兼容 | 支持 5.10 ~ 6.12 全 KMI，Android 8+ |

## 📦 下载

[Releases](https://github.com/helloxujia/mizusu/releases)

## 🏗️ 构建

```bash
git clone https://github.com/helloxujia/mizusu.git
cd mizusu/manager
./gradlew assembleDebug
```

## ⚠️ 声明

本分支仅供学习交流。内核功能与 KernelSU 官方一致，遇到问题请优先排查官方版本。

---

## 分工

- 功能开发：酷安 **@民間の人民の利益を取る**
- UI 设计：酷安 **@小小汐颜**

## 致谢

基于 [KernelSU](https://github.com/tiann/KernelSU) 和 [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) 上游源码。致敬所有开源开发者同志。

如有不妥，联系本分支开发者沟通。

[English](docs/README.md)

## 📄 许可证

kernel/ — GPL-2.0 ｜ 其余 — GPL-3.0
