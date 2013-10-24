#!/bin/bash

DIR=`pwd`
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..

if [ ! -f target/hashuri-*.jar ]; then
  echo "hashuri-*.jar not found: Run scripts/build.sh first."
  exit 1
fi

if [ ! -f classpath.txt ]; then
  echo "classpath.txt not found: Run scripts/build.sh first."
  exit 1
fi

HASHURIJAVADIR=`pwd`

CP=$(cat classpath.txt):$HASHURIJAVADIR/$(ls target/hashuri-*.jar)

cd $DIR

java -cp $CP $JAVA_OPTS ch.tkuhn.hashuri.rdf.TransformNanopub $*
