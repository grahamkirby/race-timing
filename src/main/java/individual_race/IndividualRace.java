package individual_race;

import common.Category;
import common.Race;
import common.RawResult;

import java.io.IOException;
import java.util.*;

public class IndividualRace extends Race {

    // TODO variable number of prizes per category; open prizes in addition to gender categories; non-binary category; optional team prizes, by cumulative positions and times

    IndividualRaceInput input;
    IndividualRaceOutput output_CSV, output_HTML, output_text, output_PDF;
    IndividualRacePrizes prizes;

    Runner[] entries;
    Result[] overall_results;
    Map<Category, List<Runner>> prize_winners = new HashMap<>();

    static Map<String, String> normalised_club_names = new HashMap<>();

    static {
        normalised_club_names.put("", "Unatt.");
        normalised_club_names.put("Unattached", "Unatt.");
        normalised_club_names.put("None", "Unatt.");
        normalised_club_names.put("Fife Athletic Club", "Fife AC");
        normalised_club_names.put("Dundee HH", "Dundee Hawkhill Harriers");
        normalised_club_names.put("Leven Las Vegas", "Leven Las Vegas RC");
        normalised_club_names.put("Haddies", "Anster Haddies");
        normalised_club_names.put("Dundee Hawkhill", "Dundee Hawkhill Harriers");
        normalised_club_names.put("DRR", "Dundee Road Runners");
        normalised_club_names.put("Perth RR", "Perth Road Runners");
        normalised_club_names.put("DHH", "Dundee Hawkhill Harriers");
        normalised_club_names.put("Carnegie H", "Carnegie Harriers");
        normalised_club_names.put("Dundee RR", "Dundee Road Runners");
    }

    public IndividualRace(final String config_file_path) throws IOException {
        super(config_file_path);
    }

    public IndividualRace(final Properties properties) throws IOException {
        super(properties);
    }

    public static String normaliseClubName(final String club) {

        return normalised_club_names.getOrDefault(club, club);
    }

    @Override
    protected void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureInputData();
    }

    @Override
    public void processResults() throws IOException {

        initialiseResults();

        fillFinishTimes();
        fillDNFs();
        calculateResults();
        allocatePrizes();

        printOverallResults();
        printPrizes();
        printCombined();
    }

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

        overall_results = new Result[entries.length];

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = new Result(entries[i], this);
    }

    private void fillFinishTimes() {

        for (final RawResult raw_result : raw_results) {

            final int runner_index = findIndexOfRunnerWithBibNumber(raw_result.getBibNumber());
            final Result result = overall_results[runner_index];

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
                    final Result result = getResultWithIndex(dnf_string);
                    result.DNF = true;
                }
                catch (Exception e) {
                    throw new RuntimeException("illegal DNF time");
                }
            }
        }
    }

    private Result getResultWithIndex(final String bib) {

        final int bib_number = Integer.parseInt(bib);

        return overall_results[findIndexOfRunnerWithBibNumber(bib_number)];
    }

    int findIndexOfRunnerWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.length; i++)
            if (overall_results[i].runner.bib_number == bib_number) return i;

        throw new RuntimeException("unregistered team: " + bib_number);
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

        ((IndividualRaceOutputHTML)output_HTML).printCombined();
    }
}
