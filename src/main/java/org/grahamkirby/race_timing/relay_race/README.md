## The Devil's Burdens race ##

The [Devil's Burdens](https://www.fifeac.org/events/fife-ac-events/devils-burdens.html) is a team relay 
hill race usually held in January or February in the Fife Lomond Hills.

* Four legs, some individual and some run in pairs.
* Mass starts for legs 3 and 4 if necessary.
* Prize categories:
  * Open Senior - no restriction on team composition. 1st, 2nd and 3rd prizes.
  * Open 40+ - any gender mix, all runners must be 40 or older on the day. 1st prize only.
  * Open 50+ - any gender mix, all runners must be 50 or older on the day. 1st prize only.
  * Open 60+ - any gender mix, all runners must be 60 or older on the day. 1st prize only.
  * Women Senior - six women, no age restrictions. 1st, 2nd and 3rd prizes.
  * Women 40+ - six women, all runners must be 40 or older on the day. 1st prize only.
  * Women 50+ - six women, all runners must be 50 or older on the day. 1st prize only.
  * Women 60+ - six women, all runners must be 60 or older on the day. 1st prize only.
  * Mixed Senior - minimum of three women, no age restrictions. 1st prize only.
  * Mixed 40+ - minimum of three women, all runners must be 40 or older on the day. 1st prize only.
* No more than one prize awarded to a team. If a team is eligible for more than one prize they
are awarded the prize in the higher position, and then the younger age category.

## Running the software ##

1. Create a new copy of the directory [```src/main/resources/relay_race/devils_burdens/sample_config```](https://github.com/grahamkirby/race-timing/tree/main/src/main/resources/lap_race/devils_burdens/sample_config).
2. In the file ```input/config.txt```, update properties as required:
   1. WORKING_DIRECTORY: the location of the new directory
   2. ENTRIES_PATH: name of the file containing the race entries
   3. RAW_RESULTS_PATH: name of the file containing the recorded times
   4. YEAR: year of race
   5. NUMBER_OF_LAPS: 4 in current configuration of race
   6. PAIRED_LEGS: numbers of the legs that are run in pairs
   7. RACE_NAME_FOR_RESULTS: race title
   8. RACE_NAME_FOR_FILENAMES: filename root for output files
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