package series_race;

import common.Race;
import individual_race.IndividualRace;

import java.io.IOException;
import java.util.*;

public class SeriesRace extends Race {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    IndividualRace[] races;

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


    }

    @Override
    public void processResults() throws IOException {

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


}
