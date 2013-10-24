#!/bin/bash

cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..

mvn clean compile assembly:single
