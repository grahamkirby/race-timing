# _race-timing_ is a Java application for calculating race results

[![Build and test](https://github.com/grahamkirby/race-timing/actions/workflows/maven.yml/badge.svg)](https://github.com/grahamkirby/race-timing/actions/workflows/maven.yml)

This software was originally written to process results for amateur athletics races organised by [Fife Athletic Club](//fifeac.org),
although it would probably work for other races. Reports of success or otherwise would be welcome, by email to the
author.

The software supports several types of race:

* [individual races](/src/main/resources/individual_race/README.md)
* [relay races](/src/main/resources/relay_race/README.md)
* [series races](/src/main/resources/series_race/README.md)

The focus is on simplicity, using files for input and output rather than a database. The input files used to
generate race results for each [Fife AC](//fifeac.org) race, and corresponding output files, are typically added
back into the repository to form an archive.

As far as possible, original source files (entries, raw timing results) are not changed during processing.