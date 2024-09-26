## Individual races ##

This package supports conventional individual races.

It has been used for the following [Fife AC](https://fifeac.org) races:

* [Balmullo Trail Race](https://www.fifeac.org/events/fife-ac-events/balmullo-trail-race.html)
* [Hill of Tarvit](https://www.fifeac.org/events/fife-ac-events/hill-of-tarvit.html)
* [St Andrews 5K/5M](https://www.fifeac.org/events/fife-ac-events/st-andrews-5k.html)
* [Strathkinness - Blebocraigs](https://www.fifeac.org/events/fife-ac-events/strathkinness-blebocraigs.html)

## Configurable options ##

A particular individual race has two related but distinct sets of categories:
* Runner categories
* Prize categories

In many cases these are equivalent, but there may be differences. For example, the races listed
above (from the [Midweek Series](https://www.fifeac.org/events/fife-ac-events/midweek-series.html)) use the following:
* Runner categories:
    * FU20, MU20: Female Under 20, Male Under 20
    * FS, MS: Female Senior, Male Senior (age 20-39)
    * F40, M40: Female Vet 40, Male Vet 40 (age 40-49)
    * F50, M50: Female Vet 50, Male Vet 50 (age 50-59)
    * F60, M60: Female Vet 60, Male Vet 60 (age 60-69)
    * F70+, M70+: Female Vet 70, Male Vet 70 (age 70+)
* Prize categories: WO, MO, FU20, MU20, F40, M40, F50, M50, F60, M60, F70, M70

Category options can be configured via the race configuration file:

* Senior or junior race, determining the runner categories (default: senior)
* Whether or not there should be Open prize categories (default: true)
* Whether or not there should be Senior prize categories (default: false)
* The numbers of prizes to be awarded in Open, Senior and veteran age categories

## Processing results for an individual race ##

1. Create a new copy of the directory [```src/main/resources/individual_race/sample_config```](https://github.com/grahamkirby/race-timing/tree/main/src/main/resources/individual_race/sample_config).
2. In the new file ```input/config.txt```, update properties as required:
   2. ENTRIES_FILENAME: name of the file containing the race entries
   3. RAW_RESULTS_FILENAME: name of the file containing the recorded times
   4. YEAR: year of race
   7. RACE_NAME_FOR_RESULTS: race title
   8. RACE_NAME_FOR_FILENAMES: filename root for output files
   1. WORKING_DIRECTORY: the location of the new directory
   5. NUMBER_OF_LAPS: 4 in current configuration of race
   6. PAIRED_LEGS: numbers of the legs that are run in pairs
   9. MASS_START_ELAPSED_TIMES: mass start time for each leg
   10. DNF_LEGS: legs with recorded times but runners missed a checkpoint
   11. INDIVIDUAL_LEG_STARTS: any runners exceptionally starting at different times
3. See example file [```config.txt```](src/main/resources/relay_race/devils_burdens/sample_config/input/config.txt)
for examples of required formats.
4. Copy the files containing race entries and raw results to the ```input``` sub-directory of the
working directory.
    * example format for race entries: [```entries.txt```](src/main/resources/relay_race/devils_burdens/sample_config/input/entries.txt)
    * example format for raw results: [```rawtimes.txt```](src/main/resources/relay_race/devils_burdens/sample_config/input/rawtimes.txt)
4. Run the class [```LapRace```](src/main/java/relay_race/LapRace.java),
passing the path of the new configuration file as command line parameter (surrounded by double
quotes, no escaping of spaces).
5. The results files will be created in the ```output``` sub-directory in CSV and HTML
formats:
    * overall results
    * detailed results including individual leg times
    * ranked times for each leg
    * prize list