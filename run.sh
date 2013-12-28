#!/bin/sh
MAVEN_OPTS="-Xms256m -Xmx512m -XX:MaxPermSize=128m" mvn tomcat7:run-war -Dmaven.test.skip=true
