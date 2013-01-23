Examples
========

Transform preliminary nanopub into final version:

    $ scripts/TransformNanopub.sh src/main/resources/examples/nanopub1-pre.trig

Check integrity of resulting file:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.ORpz6mGGUigbYIxSkGOSNn-Kbiz3DRlSV0NLta48AtE 
    Correct hash: ORpz6mGGUigbYIxSkGOSNn-Kbiz3DRlSV0NLta48AtE

Change creation time of generated file to "14:33:22" (line 25):

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.ORpz6mGGUigbYIxSkGOSNn-Kbiz3DRlSV0NLta48AtE 
    *** INCORRECT HASH ***: FsPwtuzBVDh1LSc5t0nogZO57gdHYFKc081o_OMxcl4
