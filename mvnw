#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

set -euf

BASE_DIR=$(cd "$(dirname "$0")" && pwd)

MAVEN_PROJECTBASEDIR=${MAVEN_PROJECTBASEDIR:-$BASE_DIR}
MAVEN_WRAPPER_DIR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper"

if [ -n "${MAVEN_USER_HOME:-}" ]; then
  MAVEN_USER_HOME=${MAVEN_USER_HOME}
else
  MAVEN_USER_HOME="$HOME/.m2"
fi

WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

JAVA_EXEC="${JAVA_HOME:-}/bin/java"
if [ ! -x "$JAVA_EXEC" ]; then
  JAVA_EXEC=java
fi

exec "$JAVA_EXEC" \
  -classpath "$MAVEN_WRAPPER_DIR/maven-wrapper.jar" \
  -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  $WRAPPER_LAUNCHER "$@"
