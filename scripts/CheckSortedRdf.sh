#!/bin/bash
#
# The following environment variables can be used:
# - RUN_VIA: If set to "MAVEN", uses Maven to run Java (sometimes faster)
# - JAVA_OPTS: Can be used to set Java command line options

CLASS=ch.tkuhn.hashuri.rdf.CheckSortedRdf

DIR=`pwd`
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..
HASHURIJAVADIR=`pwd`

if [ "$RUN_VIA" = "MAVEN" ]; then

  cd $DIR

  mvn -q -e -f $HASHURIJAVADIR/pom.xml exec:java -Dexec.mainClass="$CLASS" -Dexec.args="$*"

else

  if [ ! -f target/hashuri-*.jar ]; then
    echo "hashuri-*.jar not found: Run scripts/build.sh first."
    exit 1
  fi

  if [ ! -f classpath.txt ]; then
    echo "classpath.txt not found: Run scripts/build.sh first."
    exit 1
  fi

  CP=$(cat classpath.txt):$HASHURIJAVADIR/$(ls target/hashuri-*.jar)

  cd $DIR

  java -cp $CP $JAVA_OPTS $CLASS $*

fi
