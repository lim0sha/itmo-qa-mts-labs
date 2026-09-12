#!/usr/bin/env bash
# Компактный итог последнего прогона тестов: что запускалось, сколько прошло,
# что упало и пропущено, плюс покрытие по классам. Читает готовые отчёты и сам
# ничего не запускает — вызывать ПОСЛЕ `mvn test` / `mvn clean test`.
#
# Использование:
#   ./test-summary.sh                  итог по всем отчётам (после полного прогона)
#   ./test-summary.sh CalculatorTest   только по одному классу (после -Dtest=...)
#
# Фильтр важен: surefire не чистит старые отчёты, и после прогона одного класса
# без фильтра вы увидите цифры прошлого полного прогона.
cd "$(dirname "$0")" || exit 1
ONLY="${1:-}" exec python3 - <<'PY'
import glob, os, re, xml.etree.ElementTree as ET

REPORTS = "target/surefire-reports"
ONLY = os.environ.get("ONLY", "").strip()
files = sorted(glob.glob(f"{REPORTS}/*.txt"))
if ONLY:
    files = [f for f in files if os.path.basename(f).removesuffix(".txt").endswith(ONLY)]
if not files:
    print(f"Отчётов нет по фильтру «{ONLY}»." if ONLY else "Отчётов нет: сначала прогоните тесты — mvn clean test")
    raise SystemExit(1)

def num(pattern, text, default=0):
    m = re.search(pattern + r":\s*(\d+)", text)
    return int(m.group(1)) if m else default

rows, tot = [], [0, 0, 0, 0]
for f in files:
    text = open(f, encoding="utf-8", errors="replace").read()
    m = re.search(r"^Test set:\s*(\S+)", text, re.M)
    if not m or "Tests run:" not in text:
        continue
    cls = m.group(1).rsplit(".", 1)[-1]
    vals = [num("Tests run", text), num("Failures", text), num("Errors", text), num("Skipped", text)]
    rows.append((cls, vals))
    tot = [a + b for a, b in zip(tot, vals)]

W = 34
print("\n──────── ИТОГ ПРОГОНА ────────" + (f"  (только {ONLY})" if ONLY else ""))
print(f"{'класс':<{W}}{'тестов':>8}{'упало':>8}{'ошибок':>8}{'проп.':>8}")
for cls, v in rows:
    bad = "   <-- ПАДЕНИЕ" if v[1] + v[2] else ""
    print(f"{cls:<{W}}{v[0]:>8}{v[1]:>8}{v[2]:>8}{v[3]:>8}{bad}")
print(f"{'':<{W}}{'-'*8:>8}{'-'*8:>8}{'-'*8:>8}{'-'*8:>8}")
print(f"{'ВСЕГО':<{W}}{tot[0]:>8}{tot[1]:>8}{tot[2]:>8}{tot[3]:>8}")
print("\nВсе тесты зелёные." if tot[1] + tot[2] == 0 else "\nЕсть падения — ищите пометку ПАДЕНИЕ выше.")

if ONLY:
    print("\nПокрытие здесь не показываю: после прогона одного класса оно неполное.")
    print("Полная картина — `mvn clean test`, затем ./test-summary.sh")
    raise SystemExit(0)

JX = "target/site/jacoco/jacoco.xml"
if not os.path.exists(JX):
    print("\nОтчёта покрытия нет — он появляется после `mvn clean test`.")
    raise SystemExit(0)

def pct(c):
    return f"{round(100 * c[0] / (c[0] + c[1]))}%" if c and (c[0] + c[1]) else "-"

root = ET.parse(JX).getroot()
cov = []
for c in root.iter("class"):
    name = c.get("name")
    if "$" in name:
        continue
    d = {x.get("type"): (int(x.get("covered")), int(x.get("missed"))) for x in c.findall("counter")}
    cov.append((name.split("/aqa/")[-1], pct(d.get("LINE")), pct(d.get("BRANCH"))))

print("\n──────── ПОКРЫТИЕ ────────")
print(f"{'класс':<{W}}{'строки':>8}{'ветви':>8}")
for n, l, b in sorted(cov):
    flag = "   <-- тестов нет" if l == "0%" else ""
    print(f"{n:<{W}}{l:>8}{b:>8}{flag}")
d = {x.get("type"): (int(x.get("covered")), int(x.get("missed"))) for x in root.findall("counter")}
print(f"{'':<{W}}{'-'*8:>8}{'-'*8:>8}")
print(f"{'ИТОГО ПО ПРОЕКТУ':<{W}}{pct(d.get('LINE')):>8}{pct(d.get('BRANCH')):>8}")
print("\nСводная цифра низкая намеренно: в проекте живут классы без тестов —")
print("это цели практик и домашнего задания. Смотреть надо по классам.")
print("Полный отчёт: target/site/jacoco/index.html")
PY
