# _race-timing_ is a Java application for calculating race results

[![Build and test](https://github.com/grahamkirby/race-timing/actions/workflows/maven-linux.yml/badge.svg)](https://github.com/grahamkirby/race-timing/actions/workflows/maven-linux.yml)
[![Build and test](https://github.com/grahamkirby/race-timing/actions/workflows/maven-windows.yml/badge.svg)](https://github.com/grahamkirby/race-timing/actions/workflows/maven-windows.yml)
[![Build and test](https://github.com/grahamkirby/race-timing/actions/workflows/maven-mac.yml/badge.svg)](https://github.com/grahamkirby/race-timing/actions/workflows/maven-mac.yml)

This software was originally written to process results for amateur athletics races organised by [Fife Athletic Club](//fifeac.org),
although it would probably work for other races. Reports of success or otherwise would be welcome, by email to the
author.

The software supports several types of race:

* individual races
* relay races
* series races

The focus is on simplicity, using files for input and output rather than a database. The input files used to
generate race results for each [Fife AC](//fifeac.org) race, and corresponding output files, are typically added
back into the repository to form an archive.

As far as possible, original source files (entries, raw timing results) are not changed during processing.
