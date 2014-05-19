trustyuri-java
==============

This is a Java library to generate and check _trusty URIs_ (previously called
_hash-URIs_). Trusty URIs contain cryptographic hash values that can be used to
verify the respective content.
See the [trusty URI specification](https://github.com/trustyuri/trustyuri-spec)
and the [preprint article](http://arxiv.org/abs/1401.5775) describing the
approach.


Examples
--------

See the [examples](src/main/resources/examples).


Usage
-----

The easiest way to use this library in your project is to let Maven download it from The Central
Repository. Just include the following lines in your `pom.xml` file:

    <dependency>
      <groupId>net.trustyuri</groupId>
      <artifactId>trustyuri</artifactId>
      <version>1.1</version>
    </dependency>


Build
-----

Maven is required to build this library:

    $ mvn clean package


Run
---

Run the following command to check a file or URL with a trusty URI:

    $ scripts/CheckFile.sh [FILE-OR-URL]

The following command adds a hash to a local file using module `FA`:

    $ scripts/ProcessFile.sh [FILE]

To transform an RDF document, use this command (the second argument is optional):

    $ scripts/TransformRdf.sh [FILE] [BASE-URI]

For nanopublications you can use the following command:

    $ scripts/TransformNanopub.sh [FILE]

Running from a single JAR file should be possible, but does not work at the moment,
see [Issue 2](https://github.com/trustyuri/trustyuri-java/issues/2).


License
-------

trustyuri-java is free software under the MIT License. See LICENSE.txt.
