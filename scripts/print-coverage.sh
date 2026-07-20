#!/bin/sh
# Prints the JaCoCo report-level coverage totals from target/site/jacoco/jacoco.xml.
# Run automatically after `mvn test` (see the antrun execution in pom.xml).
set -eu

XML="$(dirname "$0")/../target/site/jacoco/jacoco.xml"

if [ ! -f "$XML" ]; then
    echo "[JaCoCo] No coverage report found at $XML"
    exit 0
fi

echo ""
echo "[JaCoCo] Coverage summary:"

for TYPE in INSTRUCTION BRANCH LINE METHOD CLASS; do
    LAST=$(grep -oE "<counter type=\"$TYPE\" missed=\"[0-9]+\" covered=\"[0-9]+\"/>" "$XML" | tail -1)
    [ -z "$LAST" ] && continue

    MISSED=$(echo "$LAST" | grep -oE 'missed="[0-9]+"' | grep -oE '[0-9]+')
    COVERED=$(echo "$LAST" | grep -oE 'covered="[0-9]+"' | grep -oE '[0-9]+')
    TOTAL=$((MISSED + COVERED))

    if [ "$TOTAL" -gt 0 ]; then
        PCT=$(awk "BEGIN{printf \"%.1f\", ($COVERED / $TOTAL) * 100}")
    else
        PCT="n/a"
    fi

    printf "  %-11s %s/%s (%s%%)\n" "$TYPE" "$COVERED" "$TOTAL" "$PCT"
done
echo ""
