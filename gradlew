#!/bin/bash
export JAVA_HOME=$(dirname $(dirname $(find /nix/store -name java -type f 2>/dev/null | head -1)))
exec ./gradlew.original "$@"
