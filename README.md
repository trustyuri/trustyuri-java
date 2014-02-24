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


Build
-----

Assuming that you have Maven installed, you can build this package as follows. First compile
and install nanopub-java (this package is not yet in a Maven repository):

    $ git clone git@github.com:Nanopublication/nanopub-java.git
    $ cd nanopub-java
    $ mvn install

Then you can run the script `build.sh` of this package:

    $ scripts/build.sh


Run
---

Run the following command to check a file or URL with a trusty URI:

    $ scripts/CheckFile.sh [FILE-OR-URL]

The following command adds a hash to a local file using algorithm `FA`:

    $ scripts/ProcessFile.sh [FILE]

To transform an RDF document, use this command (the second argument is optional):

    $ scripts/TransformRdf.sh [FILE] [BASE-URI]

For nanopublications you can use the following command:

    $ scripts/TransformNanopub.sh [FILE]


Run with Single JAR File
------------------------

**This should work in theory, but in practice it doesn't. Maven gives many "already added, skipping"
messages and then when running the commands the required Sesame parser factories are not found...**

To generate a single jar file that includes all dependencies:

    $ mvn compile assembly:single

You may want to give the resulting jar file a shorter name:

    $ mv target/hashuri-1.0-SNAPSHOT-jar-with-dependencies.jar hashuri.jar

Then the commands can be run like this:

    $ java -jar hashuri.jar CheckFile [FILE]


License
-------

trustyuri-java is free software under the MIT License. See LICENSE.txt.
