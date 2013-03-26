#!/bin/sh

mvn -q exec:java -Dexec.mainClass="ch.tkuhn.hashuri.CheckFile" -Dexec.args="$*"
