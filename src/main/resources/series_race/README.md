## Series races ##

This package supports series races composed of multiple individual races, in particular the following [Fife AC](//fifeac.org) series:

* [Midweek Series](https://www.fifeac.org/events/fife-ac-events/midweek-series.html)
* [Minitour of Fife](https://www.fifeac.org/events/fife-ac-events/minitour-of-fife.html)

## Processing results for a series race ##

1. Create a new copy of the directory [```src/main/resources/individual_race/sample_config```](https://github.com/grahamkirby/race-timing/tree/main/src/main/resources/individual_race/sample_config).
2. In the new file ```input/config.txt```, update properties as required:
   2. ENTRIES_PATH: name of the file containing the race entries
   3. RAW_RESULTS_PATH: name of the file containing the recorded times
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