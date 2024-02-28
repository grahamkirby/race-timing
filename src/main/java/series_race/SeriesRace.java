package series_race;

import common.Race;
import individual_race.*;
import lap_race.LapRaceInput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SeriesRace extends Race {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    SeriesRaceInput input;

    IndividualRace[] races;
    SeriesRaceResult[] overall_results;

    int minimum_number_of_races;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRace(String config_file_path) throws IOException {
        super(config_file_path);
    }

    public SeriesRace(final Properties properties) throws IOException {
        super(properties);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            new SeriesRace(args[0]).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureInputData();
    }

    @Override
    public void processResults() throws IOException {

        initialiseResults();

        calculateResults();
        allocatePrizes();

        printOverallResults();
        printPrizes();
        printCombined();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        super.readProperties();
        minimum_number_of_races = Integer.parseInt(properties.getProperty("MINIMUM_NUMBER_OF_RACES"));
    }

    private void configureHelpers() {

        input = new SeriesRaceInput(this);

    }

    private void configureInputData() throws IOException {

        races = input.loadSeriesRaces();
    }

    private void initialiseResults() {

        // scores =
    }

    private void calculateResults() {

        // Check dead heats.

        // Arrays.sort(overall_results);

    }

    private void allocatePrizes() {


    }

    private void printOverallResults() throws IOException {


    }

    private void printPrizes() throws IOException {


    }

    private void printCombined() throws IOException {


    }
}
