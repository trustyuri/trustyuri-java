#!/bin/bash

sha256sum $1 | awk '{print $1}' | xxd -r -p | base64 | sed 's/+/-/g' | sed 's|/|_|g' | sed 's/=$//' | sed 's/^/FA/'