#!/usr/bin/env bash
# 本地构建脚本（Linux / macOS / Git Bash）
# 优先使用项目内置 Maven Wrapper，未安装 Maven 也能一键构建。
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ -f ./mvnw ]]; then
  MVN=(./mvnw)
elif command -v mvn >/dev/null 2>&1; then
  MVN=(mvn)
else
  echo "[ERROR] 未检测到 Maven，请保留项目内的 mvnw / .mvn/wrapper/maven-wrapper.jar" >&2
  exit 1
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  echo "[INFO] JAVA_HOME=$JAVA_HOME"
fi

"${MVN[@]}" -T 1C clean package -DskipTests "$@"
echo "[OK] 产物：inventory-admin/target/inventory-admin.jar"
