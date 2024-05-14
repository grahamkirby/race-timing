package individual_race;

import common.Categories;
import common.Category;
import common.Race;
import common.RawResult;
import minitour.JuniorRaceCategories;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class IndividualRace extends Race {

    // TODO non-binary category; optional team prizes, by cumulative positions and times

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    IndividualRaceInput input;
    IndividualRaceOutput output_CSV, output_HTML, output_text, output_PDF;
    IndividualRacePrizes prizes;

    IndividualRaceEntry[] entries;
    private IndividualRaceResult[] overall_results;
    Map<Category, List<IndividualRaceEntry>> prize_winners = new HashMap<>();

    private boolean senior_race;
    public boolean open_category;
    public int open_prizes, category_prizes;

    static Map<String, String> normalised_club_names = new HashMap<>();

    static {
        normalised_club_names.put("", "Unatt.");
        normalised_club_names.put("Unattached", "Unatt.");
        normalised_club_names.put("U/A", "Unatt.");
        normalised_club_names.put("None", "Unatt.");
        normalised_club_names.put("Fife Athletic Club", "Fife AC");
        normalised_club_names.put("Dundee HH", "Dundee Hawkhill Harriers");
        normalised_club_names.put("Leven Las Vegas", "Leven Las Vegas RC");
        normalised_club_names.put("Leven Las Vegas Running Club", "Leven Las Vegas RC");
        normalised_club_names.put("Haddies", "Anster Haddies");
        normalised_club_names.put("Dundee Hawkhill", "Dundee Hawkhill Harriers");
        normalised_club_names.put("DRR", "Dundee Road Runners");
        normalised_club_names.put("Perth RR", "Perth Road Runners");
        normalised_club_names.put("Kinross RR", "Kinross Road Runners");
        normalised_club_names.put("Falkland TR", "Falkland Trail Runners");
        normalised_club_names.put("PH Racing Club", "PH Racing");
        normalised_club_names.put("DHH", "Dundee Hawkhill Harriers");
        normalised_club_names.put("Carnegie H", "Carnegie Harriers");
        normalised_club_names.put("Dundee RR", "Dundee Road Runners");
        normalised_club_names.put("Recreational Running", "Recreational Runners");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

//    public IndividualRace(final Path config_file_path, Categories categories) throws IOException {
//        super(config_file_path);
//        this.categories = categories;
//    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            IndividualRace individualRace = new IndividualRace(Paths.get(args[0]));
            individualRace.configure();
            individualRace.processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure() throws IOException {

        readProperties();

        configureCategories();
        configureHelpers();
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

        super.readProperties();

        senior_race = Boolean.parseBoolean(getPropertyWithDefault("SENIOR_RACE", "true"));
        open_category = Boolean.parseBoolean(getPropertyWithDefault("OPEN_CATEGORY", "true"));
        open_prizes = Integer.parseInt(getPropertyWithDefault("OPEN_PRIZES", String.valueOf(getDefaultOpenPrizes())));
        category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(getDefaultCategoryPrizes())));
    }

    private void configureCategories() {

        categories = senior_race ? new SeniorRaceCategories(open_category, open_prizes, category_prizes) : new JuniorRaceCategories(category_prizes);
    }
//
//    public void setCategories(Categories categories) {
//        this.categories = categories;
//    }

    private void configureHelpers() {

        input = new IndividualRaceInput(this);

        output_CSV = new IndividualRaceOutputCSV(this);
        output_HTML = new IndividualRaceOutputHTML(this);
        output_text = new IndividualRaceOutputText(this);
        output_PDF = new IndividualRaceOutputPDF(this);

        prizes = new IndividualRacePrizes(this);
    }

    private void configureInputData() throws IOException {

        raw_results = input.loadRawResults();
        entries = input.loadEntries();
    }

    private void initialiseResults() {

        overall_results = new IndividualRaceResult[raw_results.length];

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = new IndividualRaceResult(entries[i], this);
    }

    private void fillFinishTimes() {

        for (int results_index = 0; results_index < raw_results.length; results_index++) {

            final RawResult raw_result = raw_results[results_index];
            final IndividualRaceResult result = overall_results[results_index];

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

            for (final String dnf_string : dnf_string.split(",")) {

                try {
                    final IndividualRaceResult result = getResultWithIndex(dnf_string);
                    result.DNF = true;
                }
                catch (Exception e) {
                    throw new RuntimeException("illegal DNF time");
                }
            }
        }
    }

    private IndividualRaceResult getResultWithIndex(final String bib) {

        final int bib_number = Integer.parseInt(bib);

        return overall_results[findIndexOfRunnerWithBibNumber(bib_number)];
    }

    int findIndexOfRunnerWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.length; i++)
            if (overall_results[i].entry.bib_number == bib_number) return i;

        throw new RuntimeException("unregistered bib number: " + bib_number);
    }

    IndividualRaceEntry findEntryWithBibNumber(final int bib_number) {

        for (IndividualRaceEntry entry : entries) if (entry.bib_number == bib_number) return entry;

        throw new RuntimeException("unregistered bib number: " + bib_number);
    }

    private void calculateResults() {

        // Sort in order of recorded time.
        // DNF results are sorted in increasing order of bib number.
        // Where two runners have the same recorded time, the order in which they were recorded is preserved.
        Arrays.sort(overall_results);
    }

    int getRecordedPosition(final int bib_number) {

        for (int i = 0; i < raw_results.length; i++) {
            if (raw_results[i].getBibNumber() == bib_number) {
                return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    public static String normaliseClubName(final String club) {

        return normalised_club_names.getOrDefault(club, club);
    }

    private void allocatePrizes() {

        prizes.allocatePrizes();
    }

    private void printOverallResults() throws IOException {

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

    public IndividualRaceResult[] getOverallResults() {
        return overall_results;
    }
}
