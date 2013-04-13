#!/bin/sh

mvn -q -e exec:java -Dexec.mainClass="ch.tkuhn.hashuri.CheckFile" -Dexec.args="$*"
