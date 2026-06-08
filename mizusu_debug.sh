#!/system/bin/sh
# MizuSU 调试脚本 — 修补前后各运行一次
# 用法: su -c 'sh mizusu_debug.sh before'   (修补前)
#       su -c 'sh mizusu_debug.sh after'    (修补后)
# 输出文件: /sdcard/mizusu_debug_*.txt

MODE="$1"
OUT="/sdcard/mizusu_debug_${MODE}_$(date +%H%M%S).txt"

echo "=== MizuSU Debug: $MODE ===" > "$OUT"
echo "Date: $(date)" >> "$OUT"

# 内核信息
echo "--- uname ---" >> "$OUT"
uname -a >> "$OUT" 2>&1
echo "--- KMI ---" >> "$OUT"
echo "$(uname -r)" | grep -oE '[0-9]+\.[0-9]+\.[0-9]+-android[0-9]+' >> "$OUT" 2>&1

# KSU 状态
echo "--- ksud ---" >> "$OUT"
ls -la /data/adb/ksud >> "$OUT" 2>&1
echo "--- ksud version ---" >> "$OUT"
/data/adb/ksud debug version >> "$OUT" 2>&1

# 日志抓取
if [ "$MODE" = "before" ]; then
    echo "--- starting logcat (30s) ---" >> "$OUT"
    logcat -c 2>/dev/null
    logcat -s MizuSU:I -s selinux_hide_hook:I -s AndroidRuntime:E &
    LPID=$!
    sleep 30
    kill $LPID 2>/dev/null
    logcat -d -s MizuSU:I >> "$OUT" 2>&1
elif [ "$MODE" = "after" ]; then
    echo "--- logcat dump ---" >> "$OUT"
    logcat -d -s MizuSU:I >> "$OUT" 2>&1
    echo "--- dmesg KSU ---" >> "$OUT"
    dmesg | grep -i ksu >> "$OUT" 2>&1
    echo "--- patched images ---" >> "$OUT"
    ls -la /sdcard/Download/kernelsu_patched_*.img >> "$OUT" 2>&1
fi

echo "=== Done ===" >> "$OUT"
echo "Output: $OUT"
echo "请将此文件发给开发者"