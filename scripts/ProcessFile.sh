#!/bin/sh

mvn -q exec:java -Dexec.mainClass="ch.tkuhn.hashuri.file.ProcessFile" -Dexec.args="$*"
