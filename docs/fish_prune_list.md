# 杂鱼工具箱 · 检测函数切除清单

## 起点文件
```
C:\Users\keike\fish_v5.sh  (可用版, =~=0, CR=0, 语法OK)
```

## 需切除的函数（全部删掉，包括函数体）

### MT管理器检测
- `check_mt_extension_running()` — 检测是否通过MT扩展包运行
- `check_mt_extension()` — 检测MT扩展包存在
- `check_extension_tools()` — 检测扩展工具
- `check_app_exist()` — 检测应用是否存在
- `install_mt_extension()` — 安装MT扩展包
- `install_mt_extension_download_only()` — 仅下载MT扩展
- `install_mt_extension_local()` — 从本地安装MT扩展
- `uninstall_old_extension()` — 卸载旧扩展

### 娱乐动画
- `show_funny_ui()` — 卡密验证界面
- `typewriter_effect()` — 打字机效果
- `fish_swim_effect()` — 游鱼动画
- `sparkle_effect()` — 闪光效果
- `loading_animation()` — 加载动画
- `show_verification_process()` — 验证流程动画
- `show_success_ui()` — 成功界面
- `show_author_page()` — 作者页面
- `show_failure_ui()` — 失败界面
- `generate_fingerprint()` — 指纹生成
- `fish_swim()` — 鱼游泳动画

### 无意义检测
- `check_tool_quiet()` — 静默检测工具
- `show_info()` — 杂鱼信息展示

## 需修改的函数

### clear_screen() — 简化（已完成）
```bash
clear_screen() {
    printf "\033[H\033[2J"
    echo -e "${CYAN}🐟 杂鱼工具箱 · MizuSU${NC}"
    echo ""
}
```

## 切除后会调它们的代码

这些调用方会自动失效（函数不存在时报错）。需要一并处理：
- 找到切除函数的调用点 → 删除调用行（若在 if 中→保留结构，替换条件为 true/false）
- 若调用被删除后 if/while 为空 → 同时删除空块

## 不动的函数

以下**绝对不能删**：
- `print_install_say()` / `print_success_say()` / `print_fail_say()` — UI工具
- `wait_animation()` / `progress_bar()` — 进度显示
- `print_zayu_say()` — 杂鱼发言
- `print_divider()` / `print_divider_bottom()` — UI分隔线
- `check_root()` — root检测（保留，工具箱需要）
- `check_tools()` — 基础工具检测
- `download_module_from_cloud()` — 云端模块下载
- `install_cloud_module()` — 模块安装
- `check_and_clean_existing_modules()` — 已有模块检测
- 所有 `function_feature*` / `function_slot*` — 核心功能
- `main_selection_menu()` — 主菜单
- `clear_screen()` — 清屏（已简化）
