#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <release-version>"
    exit 1
fi

RELEASE_VERSION="$1"

./mvnw versions:set -DnewVersion="$RELEASE_VERSION"

./mvnw clean deploy -P central-deploy -DskipTests=true \
  -pl .,amazon-s3,appengine,benchmarks,cdi,cloud-datastore,cloud-spanner,cloud-storage,console,core,dynamodb,hazelcast,junit,kotlin,mongodb,redis,servlet,shiro,slack,spock,spring-core,spring-security,spring-web,test-harness,testing,spring-boot,spring-boot/autoconfigure,spring-boot/starter,spring-boot/starter-actuator,spring-boot/starter-console,spring-boot/starter-core,spring-boot/starter-security,spring-boot/starter-thymeleaf,spring-boot/starter-webmvc
