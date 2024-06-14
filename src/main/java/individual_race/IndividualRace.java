package individual_race;

import common.RaceEntry;
import common.RacePrizes;
import common.RawResult;
import common.categories.Category;
import common.categories.JuniorRaceCategories;
import common.categories.SeniorRaceCategories;
import single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IndividualRace extends SingleRace {

    // TODO non-binary category; optional team prizes, by cumulative positions and times

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean senior_race;
    public boolean open_category;
    public int open_prizes, category_prizes;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java IndividualRace <config file path>");
        else {
            new IndividualRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();
    }

    @Override
    public void processResults() throws IOException {

        processResults(true);
    }

    protected int getDefaultOpenPrizes() {
        return 3;
    }

    protected int getDefaultCategoryPrizes() {
        return 1;
    }

    public void processResults(boolean output_results) throws IOException {

        initialiseResults();

        fillFinishTimes();
        fillDNFs();
        calculateResults();
        allocatePrizes();

        if (output_results) {
            printOverallResults();
            printPrizes();
            printCombined();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        senior_race = Boolean.parseBoolean(getPropertyWithDefault("SENIOR_RACE", "true"));
        open_category = Boolean.parseBoolean(getPropertyWithDefault("OPEN_CATEGORY", "true"));
        open_prizes = Integer.parseInt(getPropertyWithDefault("OPEN_PRIZES", String.valueOf(getDefaultOpenPrizes())));
        category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(getDefaultCategoryPrizes())));
    }

    private void configureHelpers() {

        input = new IndividualRaceInput(this);

        output_CSV = new IndividualRaceOutputCSV(this);
        output_HTML = new IndividualRaceOutputHTML(this);
        output_text = new IndividualRaceOutputText(this);
        output_PDF = new IndividualRaceOutputPDF(this);

        prizes = new RacePrizes(this);
    }

    private void configureCategories() {

        categories = senior_race ? new SeniorRaceCategories(open_category, open_prizes, category_prizes) : new JuniorRaceCategories(category_prizes);
    }

    private void initialiseResults() {

//        overall_results = new ArrayList<>();

        for (int i = 0; i < raw_results.size(); i++)
            overall_results.add(new IndividualRaceResult(this));
    }

    private void fillFinishTimes() {

        for (int results_index = 0; results_index < raw_results.size(); results_index++) {

            final RawResult raw_result = raw_results.get(results_index);
            final IndividualRaceResult result = (IndividualRaceResult)overall_results.get(results_index);

            result.entry = findEntryWithBibNumber(raw_result.getBibNumber());
            result.finish_time = raw_result.getRecordedFinishTime();

            // Provisionally this leg is not DNF since a finish time was recorded.
            // However, it might still be set to DNF in fillDNFs() if the runner didn't complete the course.
            result.DNF = false;
        }
    }

    private void fillDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runner reported not completing the course.

        // DNF cases where there is no recorded leg result are captured by the
        // default value of Result.DNF being true.

        if (dnf_string != null && !dnf_string.isBlank()) {

            for (final String bib_number : dnf_string.split(",")) {

                try {
                    getResultWithBibNumber(Integer.parseInt(bib_number)).DNF = true;
                }
                catch (Exception e) {
                    throw new RuntimeException("illegal DNF time");
                }
            }
        }
    }

    public IndividualRaceResult getResultWithBibNumber(final int bib_number) {

        return (IndividualRaceResult)overall_results.get(findResultsIndexOfRunnerWithBibNumber(bib_number));
    }

    int findResultsIndexOfRunnerWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.size(); i++)
            if (((IndividualRaceResult)overall_results.get(i)).entry.bib_number == bib_number)
                return i;

        throw new RuntimeException("unregistered bib number: " + bib_number);
    }

    public int getRecordedPosition(final int bib_number) {

        for (int i = 0; i < raw_results.size(); i++) {
            if (raw_results.get(i).getBibNumber() == bib_number) {
                return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    private IndividualRaceEntry findEntryWithBibNumber(final int bib_number) {

        for (RaceEntry entry : entries)
            if (entry.bib_number == bib_number)
                return (IndividualRaceEntry)entry;

        throw new RuntimeException("unregistered bib number: " + bib_number);
    }

    public Category findCategory(int bib_number) {

        return findEntryWithBibNumber(bib_number).runner.category;
    }

    private void calculateResults() {

        // Sort in order of recorded time.
        // DNF results are sorted in increasing order of bib number.
        // Where two runners have the same recorded time, the order in which they were recorded is preserved.
        overall_results.sort(IndividualRaceResult::compare);
    }

    private void allocatePrizes() {

        prizes.allocatePrizes();
    }

    public void printOverallResults() throws IOException {

        output_CSV.printOverallResults();
        output_HTML.printOverallResults();
    }

    private void printPrizes() throws IOException {

        output_text.printPrizes();
        output_PDF.printPrizes();
        output_HTML.printPrizes();
    }

    private void printCombined() throws IOException {

        output_HTML.printCombined();
    }
}
