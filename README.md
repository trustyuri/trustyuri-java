hashuri-java
============

This code generates and checks URIs that represent content such as plain bytes or RDF data, and
contain a cryptographic hash value. This hash can be used to check that the respective content has
not been accidentally or deliberately modified. It can be used, for example, to enforce the
immutability of nanopublications. This is an exemplary URI:

    http://purl.org/hashuri/examples/nanopub1.RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s

This URI stands for a nanopublication. Ideally, such URIs are resolvable to files containing the
respective content. The last 45 characters of the URI (after '.') are the hash value. The first two
characters define the type and version of the algorithm; only 'FA' for plain file content and 'RA'
for sets of RDF graphs are supported at this point. The remaining 43 characters are a SHA-256 hash
value in Base64 notation ('-' instead of '+'; '_' instead of '/').


Documentation
-------------

Under construction...

See the [examples](src/main/resources/examples).


License
-------

hashuri-java is free software under the MIT License. See LICENSE.txt.
