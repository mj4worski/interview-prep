#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name '*.java' > /tmp/interview-sources.txt
javac --release 21 -d out @/tmp/interview-sources.txt
java -cp out interview.Tests "${1:-}"
