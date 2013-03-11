hashrdf
=======

This code generates and checks URIs that represent sets of RDF graphs and contain a cryptographic
hash value. This hash can be used to check that the respective RDF data has not been accidentally
or deliberately modified. It can be used, for example, to enforce the immutability of
nanopublications. This is an exemplary URI:

    http://www.tkuhn.ch/hashrdf/examples/nanopub1.ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0

Such a URI stands for a set of RDF graphs, such as a nanopublication, and is ideally resolvable to
a file containing that RDF data in a format like TriG. The last 44 characters of the URI (after
'.') are the hash value. The first character defines the version of the algorithm; only 'A' = 0 is
supported at this point. The remaining 43 characters are a SHA-256 hash value in Base64 notation
('-' instead of '+'; '_' instead of '/').


Documentation
-------------

Under construction...

See the [examples](src/main/resources/examples).


License
-------

hashrdf is free software under the MIT License. See LICENSE.txt.
