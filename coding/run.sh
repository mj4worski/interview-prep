#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

usage() {
  cat <<'EOF'
Karat drills (this screen):
  ./run.sh W1
  ./run.sh K1A     # then K1B, then K1C
  ./run.sh K1      # A+B+C
  ./run.sh K2A | K2B | K2C | K2
  ./run.sh K3A | K3B | K3C | K3

Old LeetCode-style set, not this screen:
  ./run.sh P01
EOF
}

arg="${1:-}"
if [[ -z "$arg" || "$arg" == "-h" || "$arg" == "--help" ]]; then
  usage
  exit 0
fi

rm -rf out
mkdir -p out
find src -name '*.java' > /tmp/interview-sources.txt
javac --release 21 -d out @/tmp/interview-sources.txt

upper="$(printf '%s' "$arg" | tr '[:lower:]' '[:upper:]')"
case "$upper" in
  W1|K1|K1A|K1B|K1C|K2|K2A|K2B|K2C|K3|K3A|K3B|K3C|KALL)
    exec java -cp out karat.Tests "$upper"
    ;;
  *)
    exec java -cp out interview.Tests "$upper"
    ;;
esac
