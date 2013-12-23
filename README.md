hashuri-java
============

This code generates and checks URIs that represent content such as plain bytes or RDF data, and
contain a cryptographic hash value. This hash can be used to check that the respective content has
not been accidentally or deliberately modified. This is an examle of a hash-URI:

> http://example.org/np1.RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s

See the hash-URI specification (work in progress): https://github.com/tkuhn/hashuri-spec


Examples
--------

See the [examples](src/main/resources/examples).


Build
-----

To build this package, simply run the script `build.sh` (requires Maven):

    $ scripts/build.sh

To generate a single jar file that includes all dependencies:

    $ mvn compile assembly:single

(**Problem: this does not work, because Maven gives many "already added, skipping" messages
and then the required Sesame parser factories are not found...**)

You may want to give the resulting jar file a shorter name:

    $ mv target/hashuri-1.0-SNAPSHOT-jar-with-dependencies.jar hashuri.jar


Run
---

Run the following command to check a file or URL with a hash-URI:

    $ scripts/CheckFile.sh [FILE-OR-URL]

The following command adds a hash to a local file using algorithm `FA`:

    $ scripts/ProcessFile.sh [FILE]

To transform an RDF document, use this command (the second argument is optional):

    $ scripts/TransformRdf.sh [FILE] [BASE-URI]

For nanopublications you can use the following command:

    $ scripts/TransformNanopub.sh [FILE]

If you have generated a single jar file (see above), you can run these commands also like this:

    $ java -jar hashuri.jar CheckFile [FILE]


License
-------

hashuri-java is free software under the MIT License. See LICENSE.txt.
