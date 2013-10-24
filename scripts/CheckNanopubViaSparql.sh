#!/bin/bash

DIR=`pwd`
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..
HASHURIJAVADIR=`pwd`
cd $DIR

if [ -f $HASHURIJAVADIR/target/hashuri-*-jar-with-dependencies.jar ]; then
  java -cp $HASHURIJAVADIR/target/hashuri-*-jar-with-dependencies.jar ch.tkuhn.hashuri.rdf.CheckNanopubViaSparql $*
else
  echo "hashuri-*-jar-with-dependencies.jar not found: Run scripts/build.sh first."
fi
