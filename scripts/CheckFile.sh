#!/bin/sh

mvn -q exec:java -Dexec.mainClass="ch.tkuhn.hashrdf.CheckFile" -Dexec.args="$*"
