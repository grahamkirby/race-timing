package lap_race;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class Results {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  1. If a time was recorded for any leg that should be treated as a DNF, e.g. if the runners  //
    //     reported that they missed a checkpoint, insert an additional dummy entry into the times  //
    //     file, following the recorded time, with the relevant bib number and time 00:00           //
    //                                                                                              //
    //  2. Edit the details in the following section as appropriate                                 //

    // format for mass start times
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String DNF_STRING = "DNF";
    public static final String DNS_STRING = "DNS";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    static Duration ZERO_TIME = RawResult.parseTime("0:0");

    private static final String COMMENT_PREFIX = "//";
    public static final Duration DNF_DUMMY_LAP_TIME = RawResult.parseTime("23:59:59");

    // Read from configuration file.
    Path working_directory_path;
    String entries_filename;
    String raw_results_filename;
    String year;
    int number_of_laps;
    String race_name_for_results;
    String race_name_for_filenames;
    String overall_results_header;

    // Derived.
    String overall_results_filename;
    String detailed_results_filename;
    String lap_results_filename;
    String prizes_filename;

    Path input_directory_path;
    Path entries_path;
    Path raw_results_path;

    Path output_directory_path;
    Path overall_results_path;
    Path detailed_results_path;
    Path lap_results_path;
    Path prizes_path;

    Map<Integer, Team> entries = new HashMap<>();
    List<RawResult> raw_results = new ArrayList<>();
    List<List<LapResult>> lap_results = new ArrayList<>();
    List<OverallResult> overall_results = new ArrayList<>();
    Set<Integer> prizes = new HashSet<>();

    List<Duration> mass_start_elapsed_times = new ArrayList<>();
    List<String> dnf_legs = new ArrayList<>();

    public Results(String config_file_path) throws IOException {

        this(loadProperties(config_file_path));
    }

    public Results(Properties properties) throws IOException {

        loadConfiguration(properties);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else
            new Results(args[0]).processResults();
    }

    private static Properties loadProperties(String config_file_path) throws IOException {

        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(config_file_path)) {
            properties.load(in);
        }
        return properties;
    }

    private void loadConfiguration(Properties properties) {

        working_directory_path = Paths.get(properties.getProperty("WORKING_DIRECTORY"));

        entries_filename = properties.getProperty("ENTRIES_FILENAME");
        raw_results_filename = properties.getProperty("RAW_RESULTS_FILENAME");
        year = properties.getProperty("YEAR");
        number_of_laps = Integer.parseInt(properties.getProperty("NUMBER_OF_LAPS"));
        race_name_for_results = properties.getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = properties.getProperty("RACE_NAME_FOR_FILENAMES");
        overall_results_header = properties.getProperty("OVERALL_RESULTS_HEADER");

        for (String time_as_string : properties.getProperty("MASS_START_ELAPSED_TIMES").split(",")) {
            mass_start_elapsed_times.add(RawResult.parseTime(time_as_string));
        }

        Collections.addAll(dnf_legs, properties.getProperty("DNF_LEGS").split(","));

        overall_results_filename = race_name_for_filenames + "_overall_" + year + ".csv";
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year + ".csv";
        lap_results_filename = race_name_for_filenames + "_leg_times_" + year + ".csv";
        prizes_filename = race_name_for_filenames + "_prizes_" + year + ".txt";

        input_directory_path = working_directory_path.resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);

        output_directory_path = working_directory_path.resolve("output");
        overall_results_path = output_directory_path.resolve(overall_results_filename);
        detailed_results_path = output_directory_path.resolve(detailed_results_filename);
        lap_results_path = output_directory_path.resolve(lap_results_filename);
        prizes_path = output_directory_path.resolve(prizes_filename);
    }

    public void processResults() throws IOException {

        // why not in processResults?
        loadEntries();
        loadRawResults();

        calculateLapResults(); // Need to calculate lap results as they're used in following calculations.
        calculateOverallResults();

        printOverallResults();
        printDetailedResults();
        printLapResults();
        printPrizes();
    }

    public void printEntries() {

        for (Map.Entry<Integer, Team> entry : entries.entrySet()) {
            System.out.println("team " + entry.getKey() + ": " + entry.getValue());
        }
    }

    public void printRawResults() {

        for (RawResult result : raw_results) {
            System.out.println(result);
        }
    }

    public void printOverallResults() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(overall_results_path))) {

            printOverallResultsHeader(writer);
            printOverallResults(writer);
            printDNFs(false, writer);
            printDNSs(writer);
        }
    }

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        int position = 1;
        for (OverallResult overall_result : overall_results) {
            writer.append(String.valueOf(position)).append(",").append(String.valueOf(overall_result));
            writer.append("\n");
            position++;
        }
    }

    public void printDetailedResults() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_path))) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
            printDNFs(true, writer);
            printDNSs(writer);
        }
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (OverallResult overall_result : overall_results) {

            int bib_number = overall_result.bib_number;

            writer.append(String.valueOf(position)).append(",");
            writer.append(String.valueOf(bib_number)).append(",");
            writer.append(overall_result.team_name).append(",");
            writer.append(overall_result.team_category.toString()).append(",");

            Team team = entries.get(bib_number);

            for (int lap = 1; lap <= number_of_laps; lap++) {

                writer.append(team.runners[lap - 1]).append(",");

                LapResult lap_result = findLapResult(bib_number, lap - 1);

                writer.append(OverallResult.format(lap_result.lap_time)).append(",");
                writer.append(OverallResult.format(lap_result.adjusted_split_time));

                if (lap < number_of_laps) writer.append(",");
            }

            writer.append("\n");
            position++;
        }
    }

    public void printLapResults() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(lap_results_path))) {

            int max_completions_in_any_lap = 0;
            for (int lap = 1; lap <= number_of_laps; lap++) {
                int completions_for_lap = countValidLapResults(lap_results.get(lap - 1));
                if (max_completions_in_any_lap < completions_for_lap) max_completions_in_any_lap = completions_for_lap;
            }

            printLapResultsHeader(writer);

            for (int pos = 1; pos <= max_completions_in_any_lap; pos++) {

                writer.append(String.valueOf(pos));

                for (int lap = 1; lap <= number_of_laps; lap++) {

                    final List<LapResult> results_for_lap = lap_results.get(lap - 1);

                    if (results_for_lap.size() >= pos) {
                        LapResult lap_result = results_for_lap.get(pos - 1);
                        if (!lap_result.DNF) {
                            Team team = entries.get(lap_result.bib_number);

                            writer.append(",").append(team.runners[lap - 1]).append(",").append(OverallResult.format(lap_result.lap_time));
                        } else {
                            writer.append(",,");
                        }
                    } else {
                        writer.append(",,");
                    }
                }

                writer.append("\n");
            }
        }
    }

    private int countValidLapResults(final List<LapResult> lap_results) {

        int count = 0;
        for (LapResult lap_result : lap_results) {
            if (!lap_result.DNF) count++;
        }
        return count;
    }

    public void printPrizes() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================\n\n");

            for (Category category : Arrays.asList(
                    Category.FEMALE_SENIOR, Category.OPEN_SENIOR,
                    Category.FEMALE_40, Category.OPEN_40,
                    Category.FEMALE_50, Category.OPEN_50,
                    Category.FEMALE_60, Category.OPEN_60,
                    Category.MIXED_SENIOR, Category.MIXED_40)) {

                printPrizes(category, writer);
            }
        }
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        int number_of_prizes = category.getNumberOfPrizes();

        writer.append("Category: ").append(String.valueOf(category)).append("\n");
        printDashes(category.toString().length() + 10, writer);
        writer.append("\n");

        int position = 1;
        for (OverallResult overall_result : overall_results) {

            if (categoryMatch(category, position, overall_result) && !prizes.contains(overall_result.bib_number) && position <= number_of_prizes) {
                writer.append(String.valueOf(position)).append(",").append(String.valueOf(overall_result)).append("\n");
                prizes.add(overall_result.bib_number);
                position++;
            }
        }

        writer.append("\n\n");
    }

    private boolean categoryMatch(final Category category, final int position, final OverallResult overall_result) {

        // Only check for an older category being included in this category when looking for first prize,
        // since the first in an older category always gets that prize rather than a lower senior prize.

        // This won't work in the case where the top two teams are women or mixed...

        return category == overall_result.team_category || (position == 1 && category.includes(overall_result.team_category));
    }

    private void printDashes(final int n, final OutputStreamWriter writer) throws IOException {

        for (int i = 0; i < n; i++) writer.append("-");
        writer.append("\n");
    }

    private void loadEntries() throws IOException {

        for (String line : Files.readAllLines(entries_path)) {
            String[] elements = line.split("\t");
            entries.put(Integer.parseInt(elements[0]), new Team(elements));
        }
    }

    private void loadRawResults() throws IOException {

        for (String line : Files.readAllLines(raw_results_path)) {
            if (!line.startsWith(COMMENT_PREFIX)) {
                raw_results.add(new RawResult(line));
            }
        }
    }

    private void printDNFs(boolean include_lap_details, OutputStreamWriter writer) throws IOException {

        for (Map.Entry<Integer, Team> entry : entries.entrySet()) {

            final int bib_number = entry.getKey();
            final int laps_completed = countLapsCompleted(bib_number);
            if (laps_completed > 0 && laps_completed < number_of_laps) {

                Team team = entry.getValue();
                writer.append(",").append(String.valueOf(bib_number)).append(",").append(team.getName()).append(",").append(String.valueOf(team.getCategory())).append(",");

                if (include_lap_details) {

                    boolean an_earlier_lap_was_DNF = false;

                    for (int lap = 1; lap <= number_of_laps; lap++) {

                        writer.append(team.runners[lap - 1]).append(",");

                        try {
                            final LapResult lap_result = findLapResult(bib_number, lap - 1);

                            if (!lap_result.DNF) {
                                writer.append(OverallResult.format(lap_result.lap_time)).append(",");
                                writer.append(an_earlier_lap_was_DNF ? DNF_STRING : OverallResult.format(lap_result.adjusted_split_time));
                            } else {
                                writer.append(DNF_STRING).append(",").append(DNF_STRING);
                                an_earlier_lap_was_DNF = true;
                            }
                        } catch (RuntimeException e) {
                            writer.append(DNF_STRING).append(",").append(DNF_STRING);
                        }

                        if (lap < number_of_laps) writer.append(",");
                    }
                    writer.append("\n");
                } else {
                    writer.append("DNF\n");
                }
            }
        }
    }

    private void printDNSs(OutputStreamWriter writer) throws IOException {

        for (Map.Entry<Integer, Team> entry : entries.entrySet()) {

            final int bib_number = entry.getKey();
            final int laps_completed = countLapsCompleted(bib_number);

            if (laps_completed == 0) {
                Team team = entry.getValue();
                writer.append(",").append(String.valueOf(bib_number)).append(",").append(team.getName()).append(",").append(String.valueOf(team.getCategory())).append(",").append(DNS_STRING).append("\n");
            }
        }
    }

    private int countLapsCompleted(final int bib_number) {

        int count = 0;

        for (int lap_index = 0; lap_index < number_of_laps; lap_index++) {
            try {
                final LapResult lap_result = findLapResult(bib_number, lap_index);
                if (!lap_result.DNF) count++;

            } catch (RuntimeException e) {
                // Ignore - no result for this lap.
            }
        }

        return count;
    }

    private void printOverallResultsHeader(OutputStreamWriter writer) throws IOException {

        writer.append(overall_results_header).append("Total\n");
    }

    private void printDetailedResultsHeader(OutputStreamWriter writer) throws IOException {

        writer.append(overall_results_header);

        for (int lap = 1; lap <= number_of_laps; lap++) {
            writer.append("Runners ").append(String.valueOf(lap)).append(",Leg ").append(String.valueOf(lap)).append(",");
            if (lap < number_of_laps) writer.append("Split ").append(String.valueOf(lap)).append(",");
        }

        writer.append("Total\n");
    }

    private void printLapResultsHeader(OutputStreamWriter writer) throws IOException {

        writer.append("Pos");

        for (int lap = 1; lap <= number_of_laps; lap++) {
            writer.append(",Runners ").append(String.valueOf(lap)).append(",Leg ").append(String.valueOf(lap));
        }

        writer.append("\n");
    }

    private void calculateLapResults() {

        for (int i = 0; i < number_of_laps; i++) {
            lap_results.add(new ArrayList<>());
        }

        for (RawResult raw_result : raw_results) {

            int lap_index = findEarliestLapWithoutNumberRecorded(raw_result.bib_number);

//            if (raw_result.recorded_elapsed_time.isZero()) {
//
//                // Mark current lap as DNF.
//                LapResult lap_result = findLapResult(raw_result.bib_number, lap_index - 1);
//                lap_result.DNF = true;
//                lap_result.lap_time = DNF_DUMMY_LAP_TIME;
//
//            } else {

                final List<LapResult> this_lap_results = lap_results.get(lap_index);

                final Duration adjusted_split_time = calculateAdjustedSplitTime(raw_result, lap_index);
                final Duration amount_this_lap_finish_later_than_next_lap_mass_start = calculateAmountThisLapFinishLaterThanNextLapMassStart(raw_result, lap_index);
                final Duration lap_time = calculateLapTime(raw_result, lap_index);

                final LapResult lap_result = new LapResult(
                        lap_index + 1,
                        raw_result.bib_number,
                        raw_result.recorded_elapsed_time,
                        adjusted_split_time,
                        amount_this_lap_finish_later_than_next_lap_mass_start,
                        lap_time,
                        entries);

                this_lap_results.add(lap_result);
//            }
        }

        for (String dnf_leg : dnf_legs) {

            String[] split = dnf_leg.split(":");
            int bib_number = Integer.parseInt(split[0]);
            int leg_number = Integer.parseInt(split[1]);

            LapResult lap_result = findLapResult(bib_number, leg_number - 1);
            lap_result.DNF = true;
            lap_result.lap_time = DNF_DUMMY_LAP_TIME;
        }

        // Sort the results within each lap.
        for (int i = 0; i < number_of_laps; i++) {
            lap_results.get(i).sort(Comparator.comparing(o -> o.lap_time));
        }
    }

    private void calculateOverallResults() {

        for (int bib_number : entries.keySet()) {

            try {
                Duration overall_time = ZERO_TIME;

                for (int lap_index = 0; lap_index < number_of_laps; lap_index++) {

                    final LapResult lap_result = findLapResult(bib_number, lap_index);
                    if (lap_result.DNF) throw new RuntimeException();
                    overall_time = overall_time.plus(lap_result.lap_time);
                }

                OverallResult result = new OverallResult(bib_number, overall_time, entries);
                overall_results.add(result);

            } catch (RuntimeException e) {
                // Ignore - missing lap result so don't record in overall results.
            }
        }

        overall_results.sort(Comparator.comparing(o -> o.overall_time));
    }

    private Duration calculateAmountThisLapFinishLaterThanNextLapMassStart(final RawResult raw_result, final int lap_index) {

        if (lap_index == number_of_laps - 1) return ZERO_TIME;

        Duration difference = raw_result.recorded_elapsed_time.minus(mass_start_elapsed_times.get(lap_index + 1));

        return difference.isNegative() ? ZERO_TIME : difference;
    }

    private Duration calculateAdjustedSplitTime(final RawResult raw_result, final int lap_index) {

        Duration adjusted_split_time = raw_result.recorded_elapsed_time;

        // Add on mass start adjustments from all previous laps.
        for (int i = 0; i < lap_index; i++) {
            LapResult earlier_lap_result = findLapResult(raw_result.bib_number, i);
            adjusted_split_time = adjusted_split_time.plus(earlier_lap_result.amount_this_lap_finish_later_than_next_lap_mass_start);
        }

        return adjusted_split_time;
    }

    private Duration calculateLapTime(final RawResult raw_result, final int lap_index) {

        Duration previous_lap_recorded_split_time = ZERO_TIME;

        if (lap_index > 0) {

            LapResult previous_lap_result = findLapResult(raw_result.bib_number, lap_index - 1);
            previous_lap_recorded_split_time = previous_lap_result.recorded_split_time;
        }

        Duration lap_time = raw_result.recorded_elapsed_time.minus(previous_lap_recorded_split_time);

        // Add on mass start adjustment from previous lap.
        if (lap_index > 0) {
            LapResult earlier_lap_result = findLapResult(raw_result.bib_number, lap_index - 1);
            lap_time = lap_time.plus(earlier_lap_result.amount_this_lap_finish_later_than_next_lap_mass_start);
        }

        return lap_time;
    }

    private LapResult findLapResult(final int bib_number, int lap_index) {

        final List<LapResult> this_lap_results = lap_results.get(lap_index);
        for (LapResult lap_result : this_lap_results) {
            if (lap_result.bib_number == bib_number) return lap_result;
        }
        throw new RuntimeException("couldn't find lap result for bib: " + bib_number + " in lap: " + (lap_index + 1));
    }

    // Find the earliest lap that doesn't yet have this bib number recorded.
    private int findEarliestLapWithoutNumberRecorded(final int bib_number) {

        for (int lap_index = 0; lap_index < number_of_laps; lap_index++) {
            if (!bibNumberRecorded(bib_number, lap_index)) return lap_index;
        }
        throw new RuntimeException("bib number recorded too many times: " + bib_number);
    }

    private boolean bibNumberRecorded(final int bib_number, final int lap_index) {

        for (LapResult lap_result : lap_results.get(lap_index)) {
            if (lap_result.bib_number == bib_number) return true;
        }
        return false;
    }
}
