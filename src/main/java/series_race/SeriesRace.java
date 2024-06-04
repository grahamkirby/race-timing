package series_race;

import common.*;
import individual_race.IndividualRace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SeriesRace extends Race {

    public IndividualRace[] races;
    public Runner[] combined_runners;
    public Map<Category, List<Runner>> prize_winners = new HashMap<>();

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
