## FASS

Java >= 1.8 needed.

A program that screen-scrapes data from FASS.se and inserts drug information
in a postgres database in a machine friendly form.

The crawler is single threaded.The extractor uses java 8 parallel streams to do the extraction
and saves synchronously to a single file.
All data is serialized/deserialized to XML files before being inserted to postgres.
Stream marshalling/unmarshalling is used and so the program executes
in constant memory and can handle arbitrarily large data sets.
