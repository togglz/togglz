#!/bin/bash

echo "====================[ Snapshot deployer ]=========================="

IS_JDK7=`test $(java -version 2>&1 | fgrep -c '1.7.0') -gt 0; echo $?`
IS_WILDFLY=`test "$CONTAINER" = 'wildfly-managed'; echo $?`
IS_SECURE=`test "$TRAVIS_SECURE_ENV_VARS" = 'true'; echo $?`

echo "Built against JDK7:         $IS_JDK7"
echo "Wildfly profile active:     $IS_WILDFLY"
echo "Secure variables available: $IS_SECURE"

if [ $IS_JDK7 -eq 0 -a $IS_WILDFLY -eq 0 -a $IS_SECURE -eq 0  ]; then
  mvn -s .travis-sonatype-settings.xml -DperformRelease -DskipTests -DJBOSS_REPO=true deploy
else
  echo "Not deploying snapshots"
fi

