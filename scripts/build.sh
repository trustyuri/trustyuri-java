#!/bin/bash

cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..

mvn clean package dependency:build-classpath -Dmdep.outputFile=classpath.txt
