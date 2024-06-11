package series_race;

import common.Race;
import common.RaceInput;
import common.RaceOutput;
import common.Runner;
import individual_race.IndividualRace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class SeriesRace extends Race {

    public List<IndividualRace> races;
    public List<Runner> combined_runners;

    public int category_prizes;
    public int minimum_number_of_races;

    protected RaceInput input;
    protected RaceOutput output_CSV, output_HTML, output_text, output_PDF;

    public SeriesRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void processResults() throws IOException {

        initialiseResults();

        calculateResults();
        allocatePrizes();

        printOverallResults();
        printPrizes();
        printCombined();
    }

    @Override
    public void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();
    }

    public void initialiseResults() {

        combined_runners = getCombinedRunners(races);
    }

    protected void readProperties() {

        super.readProperties();
        category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(3)));
    }

    public abstract void configureHelpers();
    public abstract void configureCategories();
    public abstract void configureInputData() throws IOException;

    public abstract void calculateResults() throws IOException;
    public abstract void allocatePrizes() throws IOException;
    public abstract void printOverallResults() throws IOException;
    public abstract void printPrizes() throws IOException;
    public abstract void printCombined() throws IOException;
}
