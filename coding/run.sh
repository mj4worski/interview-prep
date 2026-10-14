#!/usr/bin/env bash
# macOS ships bash 3.2. Keep this script compatible with it.
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

Which JDK this script will use:
  ./run.sh --jdk
EOF
}

# "javac 21.0.2" -> 21, "javac 1.8.0_272" -> 8. javac writes -version to stderr.
javac_major() {
  local line spec major
  line=$("$1" -version 2>&1 || true)
  spec=$(printf '%s\n' "$line" | awk 'NR==1 { print $2; exit }')
  spec=${spec%%-*}
  case "$spec" in
    1.*)
      major=${spec#1.}
      major=${major%%.*}
      ;;
    "")
      major=0
      ;;
    *)
      major=${spec%%.*}
      ;;
  esac
  case "$major" in
    ''|*[!0-9]*) major=0 ;;
  esac
  printf '%s' "$major"
}

resolve_bin() {
  local p="$1" i=0 target
  while [[ -L "$p" && $i -lt 10 ]]; do
    target=$(readlink "$p")
    if [[ "$target" != /* ]]; then
      target="$(cd "$(dirname "$p")" && pwd)/$target"
    fi
    p=$target
    i=$((i + 1))
  done
  printf '%s' "$p"
}

BEST_HOME=""
BEST_MAJOR=0

# Prefer 21. Otherwise take the newest JDK that is at least 11.
consider_home() {
  local home="$1" bin major
  [[ -n "$home" && -x "$home/bin/javac" ]] || return 0
  bin=$(resolve_bin "$home/bin/javac")
  home=$(cd "$(dirname "$(dirname "$bin")")" && pwd)
  major=$(javac_major "$home/bin/javac")
  [[ "$major" -ge 11 ]] || return 0
  if [[ "$BEST_MAJOR" -eq 21 ]]; then
    return 0
  fi
  if [[ "$major" -eq 21 || "$major" -gt "$BEST_MAJOR" ]]; then
    BEST_HOME=$home
    BEST_MAJOR=$major
  fi
}

# One pattern per call. An empty match must not become `for x in ;` — bash 3.2
# on macOS treats that as a syntax error under `set -u`.
consider_glob() {
  local pattern="$1" home i n
  local matches
  matches=()
  shopt -s nullglob
  # Intentional word-split: the pattern is a glob, not a single path.
  # shellcheck disable=SC2206
  matches=( $pattern )
  shopt -u nullglob
  n=${#matches[@]}
  i=0
  while [[ "$i" -lt "$n" ]]; do
    home=${matches[$i]}
    consider_home "$home"
    i=$((i + 1))
  done
}

discover_jdk() {
  local v home resolved
  BEST_HOME=""
  BEST_MAJOR=0

  if [[ -x /usr/libexec/java_home ]]; then
    for v in 21 25 24 23 22 17 11; do
      home=$(/usr/libexec/java_home -v "$v" 2>/dev/null || true)
      consider_home "$home"
    done
  fi

  consider_glob "/Library/Java/JavaVirtualMachines/*/Contents/Home"
  consider_glob "$HOME/Library/Java/JavaVirtualMachines/*/Contents/Home"
  consider_glob "/opt/homebrew/opt/openjdk@*/libexec/openjdk.jdk/Contents/Home"
  consider_glob "/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home"
  consider_glob "/usr/local/opt/openjdk@*/libexec/openjdk.jdk/Contents/Home"
  consider_glob "/usr/local/opt/openjdk/libexec/openjdk.jdk/Contents/Home"
  consider_glob "/usr/lib/jvm/*"
  consider_glob "$HOME/.sdkman/candidates/java/*"

  if [[ -n "${JAVA_HOME:-}" ]]; then
    consider_home "$JAVA_HOME"
  fi

  if command -v javac >/dev/null 2>&1; then
    resolved=$(resolve_bin "$(command -v javac)")
    consider_home "$(dirname "$(dirname "$resolved")")"
  fi
}

need_jdk() {
  local found=""
  if command -v javac >/dev/null 2>&1; then
    found=$(javac_major "$(resolve_bin "$(command -v javac)")")
  fi
  if [[ -n "$found" && "$found" != "0" ]]; then
    printf 'Na PATH jest JDK %s. ' "$found" >&2
  fi
  cat >&2 <<'EOF'
Potrzebny JDK 11 albo nowszy (na Macu weź 21).
javac z ósemki nie zna flagi --release — stąd "invalid flag: --release".

Na Macu:
  brew install --cask temurin@21

Potem:
  /usr/libexec/java_home -v 21
  ./run.sh W1

./run.sh sam weźmie Temurin 21, nawet gdy `java -version` nadal pokazuje 1.8.
Żeby `java` w terminalu też było 21, w ~/.zshrc zostaw tylko:

  export JAVA_HOME=$(/usr/libexec/java_home -v 21)
  export PATH="$JAVA_HOME/bin:$PATH"

i usuń starą ścieżkę AdoptOpenJDK 8.
EOF
  exit 1
}

arg="${1:-}"
if [[ -z "$arg" || "$arg" == "-h" || "$arg" == "--help" ]]; then
  usage
  exit 0
fi

discover_jdk
if [[ -z "$BEST_HOME" ]]; then
  need_jdk
fi

if [[ "$arg" == "--jdk" ]]; then
  printf '%s\nJDK %s\n' "$BEST_HOME" "$BEST_MAJOR"
  exit 0
fi

if command -v javac >/dev/null 2>&1; then
  path_major=$(javac_major "$(resolve_bin "$(command -v javac)")")
  if [[ "$path_major" -lt 11 ]]; then
    printf 'javac na PATH to JDK %s. Kompiluję JDK %s: %s\n' \
      "$path_major" "$BEST_MAJOR" "$BEST_HOME" >&2
  fi
fi

release=21
if [[ "$BEST_MAJOR" -lt 21 ]]; then
  release=$BEST_MAJOR
fi

rm -rf out
mkdir -p out
find src -name '*.java' > /tmp/interview-sources.txt
"$BEST_HOME/bin/javac" --release "$release" -d out @/tmp/interview-sources.txt

upper="$(printf '%s' "$arg" | tr '[:lower:]' '[:upper:]')"
case "$upper" in
  W1|K1|K1A|K1B|K1C|K2|K2A|K2B|K2C|K3|K3A|K3B|K3C|KALL)
    exec "$BEST_HOME/bin/java" -cp out karat.Tests "$upper"
    ;;
  *)
    exec "$BEST_HOME/bin/java" -cp out interview.Tests "$upper"
    ;;
esac
