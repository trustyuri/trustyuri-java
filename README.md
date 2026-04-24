Trusty URI implementation for Java
==============

This is a Java library to generate and check _trusty URIs_ (previously called
_hash-URIs_). Trusty URIs contain cryptographic hash values that can be used to
verify the respective content.
See https://github.com/trustyuri/trustyuri.


Examples
--------

See the [examples](src/main/resources/examples).


Usage
-----

The easiest way to use this library in your project is to let Maven download it from The Central
Repository. Just include the following lines in your `pom.xml` file, replacing `VERSION` with the version number you
want to use:

    <dependency>
      <groupId>net.trustyuri</groupId>
      <artifactId>trustyuri</artifactId>
      <version>VERSION</version>
    </dependency>

Build
-----

Maven is required to build and run this library:

    $ ./mvnw clean install

Run as CLI tool
-----------------

You can download a prebuilt JAR file: https://github.com/trustyuri/trustyuri-java/releases/latest

Then you can use the following command (replace `VERSION` with the version number
you are using):

    $ java -jar trustyuri-VERSION-jar-with-dependencies.jar [COMMAND] [ARGS]

For example:

    $ java -jar trustyuri-VERSION-jar-with-dependencies.jar CheckFile http://trustyuri.net/spec/v1.FADQoZWcYugekAb4jW-Zm3_5Cd9tmkkYEV0bxK2fLSKao.md

Run from Source
---------------

Run the following command to check a file or URL with a trusty URI:

    $ scripts/CheckFile.sh [FILE-OR-URL]

The following command adds a hash to a local file using module `FA`:

    $ scripts/ProcessFile.sh [FILE]

To transform an RDF document, use this command (the second argument is optional):

    $ scripts/TransformRdf.sh [FILE] [BASE-URI]

License
-------

**trustyuri-java** is free software under the MIT License. See LICENSE.txt.
