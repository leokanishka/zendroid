#!/bin/bash
export JAVA_HOME=$(find /nix/store -name 'openjdk*' -type d | head -1 2>/dev/null)
if [ -z "$JAVA_HOME" ]; then
  JAVA_BIN=$(which java)
  JAVA_DIR=$(dirname "$JAVA_BIN")
  JAVA_HOME=$(dirname "$JAVA_DIR")
fi
export PATH="$JAVA_HOME/bin:$PATH"
echo "Using JAVA_HOME: $JAVA_HOME"
./gradlew clean assembleDebug
