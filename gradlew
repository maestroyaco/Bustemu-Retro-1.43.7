#!/bin/sh
#
# Copyright 2015 the original author or authors.
# Licensed under the Apache License, Version 2.0.
#

set -e

APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAVA_OPTS="${JAVA_OPTS:--Xmx64m -Xms64m}"

if [ -n "$JAVA_HOME" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE=java
fi

exec "$JAVA_EXE" $JAVA_OPTS $GRADLE_OPTS "-Dorg.gradle.appname=$0" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
