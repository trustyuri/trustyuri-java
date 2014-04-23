#!/bin/bash
#
# The following environment variables can be used:
# - RUN_VIA: If set to "MAVEN", uses Maven to run Java (sometimes faster)
# - JAVA_OPTS: Can be used to set Java command line options

CLASS=net.trustyuri.rdf.CheckRdfGraph

DIR=`pwd`
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..
TRUSTYURIJAVADIR=`pwd`

if [ "$RUN_VIA" = "MAVEN" ]; then

  cd $DIR

  mvn -q -e -f $TRUSTYURIJAVADIR/pom.xml exec:java -Dexec.mainClass="$CLASS" -Dexec.args="$*"

else

  if [ ! -f target/trustyuri-*.jar ]; then
    echo "trustyuri-*.jar not found: Run 'mvn clean package' first."
    exit 1
  fi

  if [ ! -f classpath.txt ]; then
    echo "classpath.txt not found: Run 'mvn clean package' first."
    exit 1
  fi

  CP=$(cat classpath.txt):$TRUSTYURIJAVADIR/$(ls target/trustyuri-*.jar)

  cd $DIR

  java -cp $CP $JAVA_OPTS $CLASS $*

fi
