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
    private static final String NO_MASS_STARTS = "23:59:59,23:59:59,23:59:59,23:59:59";
    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    static Duration ZERO_TIME = RawResult.parseTime("0:0");

    public static final Duration DNF_DUMMY_LEG_TIME = RawResult.parseTime("23:59:59");

    // Read from configuration file.
    Path working_directory_path;
    String entries_filename;
    String raw_results_filename;
    String year;
    int number_of_legs;
    String race_name_for_results;
    String race_name_for_filenames;
    String dnf_legs_string;

    // Derived.
    String overall_results_filename;
    String detailed_results_filename;
    String prizes_filename;

    Path input_directory_path;
    Path entries_path;
    Path raw_results_path;

    Path output_directory_path;
    Path overall_results_path;
    Path detailed_results_path;
    Path prizes_path;

    Team[] entries;
    RawResult[] raw_results;
    OverallResult[] results;
    Set<Team> prize_winners = new HashSet<>();

    Duration[] start_times_for_mass_starts;  // Relative to start of leg 1.
    boolean[] paired_legs;

    public Results(String config_file_path) throws IOException {
        this(loadProperties(config_file_path));
    }

    public Results(Properties properties) {
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

        try (FileInputStream in = new FileInputStream(config_file_path)) {

            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    private void loadConfiguration(Properties properties) {

        readProperties(properties);
        configureMassStarts(properties);
        configurePairedLegs(properties);
        constructFilePaths();
    }

    private void readProperties(Properties properties) {

        working_directory_path = Paths.get(properties.getProperty("WORKING_DIRECTORY"));

        entries_filename = properties.getProperty("ENTRIES_FILENAME");
        raw_results_filename = properties.getProperty("RAW_RESULTS_FILENAME");
        year = properties.getProperty("YEAR");
        number_of_legs = Integer.parseInt(properties.getProperty("NUMBER_OF_LEGS"));
        race_name_for_results = properties.getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = properties.getProperty("RACE_NAME_FOR_FILENAMES");
        dnf_legs_string = properties.getProperty("DNF_LEGS");
    }

    private void configureMassStarts(Properties properties) {

        String mass_start_elapsed_times_string = properties.getProperty("MASS_START_ELAPSED_TIMES");

        if (mass_start_elapsed_times_string.isBlank())
            mass_start_elapsed_times_string = NO_MASS_STARTS;

        start_times_for_mass_starts = new Duration[number_of_legs];
        int leg = 0;
        for (String time_as_string : mass_start_elapsed_times_string.split(",")) {
            start_times_for_mass_starts[leg++] = RawResult.parseTime(time_as_string);
        }
    }

    private void configurePairedLegs(Properties properties) {

        String paired_legs_string = properties.getProperty("PAIRED_LEGS");
        paired_legs = new boolean[number_of_legs];
        for (String s : paired_legs_string.split(",")) {
            paired_legs[Integer.parseInt(s) - 1] = true;
        }
    }

    private void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year + ".csv";
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year + ".csv";
        prizes_filename = race_name_for_filenames + "_prizes_" + year + ".txt";

        input_directory_path = working_directory_path.resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);

        output_directory_path = working_directory_path.resolve("output");
        overall_results_path = output_directory_path.resolve(overall_results_filename);
        detailed_results_path = output_directory_path.resolve(detailed_results_filename);
        prizes_path = output_directory_path.resolve(prizes_filename);
    }

    public void processResults() throws IOException {

        loadEntries();
        loadRawResults();
        initialiseResults();

        fillLegFinishTimes();
        fillDNFs();
        fillLegStartTimes();
        calculateResults();

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printPrizes();
    }

    private void loadEntries() throws IOException {

        List<String> lines = Files.readAllLines(entries_path);
        entries = new Team[lines.size()];

        int i = 0;
        for (String line : lines) {
            String[] elements = line.split("\t");
            entries[i++] = new Team(elements);
        }
    }

    private void loadRawResults() throws IOException {

        List<String> lines = Files.readAllLines(raw_results_path);
        raw_results = new RawResult[lines.size()];

        for (int i = 0; i < raw_results.length; i++) {
            raw_results[i] = new RawResult(lines.get(i));
        }
    }

    private void initialiseResults() {

        results = new OverallResult[entries.length];

        for (int i = 0; i < results.length; i++) {
            results[i] = new OverallResult(entries[i], number_of_legs);
        }
    }

    private void fillLegFinishTimes() {

        for (RawResult raw_result : raw_results) {

            try {
                int team_index = findIndexOfTeamWithBibNumber(raw_result.bib_number);
                OverallResult result = results[team_index];
                LegResult[] leg_results = result.leg_results;

                int leg_index = findIndexOfNextLegResult(leg_results);
                leg_results[leg_index].finish_time = raw_result.recorded_finish_time;
                leg_results[leg_index].DNF = false;
            }
            catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage() + raw_result.bib_number);
            }
        }
    }

    private void fillDNFs() {

        if (!dnf_legs_string.isBlank()) {

            for (String dnf_string : dnf_legs_string.split(",")) {
                String[] dnf = dnf_string.split(":");
                int bib_number = Integer.parseInt(dnf[0]);
                int leg_number = Integer.parseInt(dnf[1]);

                int index = findIndexOfTeamWithBibNumber(bib_number);
                OverallResult result = results[index];
                result.leg_results[leg_number - 1].DNF = true;
            }
        }
    }

    private void fillLegStartTimes() {

        for (OverallResult result : results) {

            LegResult[] leg_results = result.leg_results;

            leg_results[0].start_time = ZERO_TIME;

            for (int i = 1; i < number_of_legs; i++) {

                Duration mass_start_time = start_times_for_mass_starts[i];
                if (leg_results[i-1].finish_time != null)
                    leg_results[i].start_time = earlierOf(leg_results[i-1].finish_time, mass_start_time);
            }
        }
    }

    private void calculateResults() {

        Arrays.sort(results);
    }

    public void printOverallResults() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(overall_results_path))) {

            writer.append(OVERALL_RESULTS_HEADER).append("Total\n");

            for (int i = 0; i < results.length; i++) {

                OverallResult result = results[i];

                if (!result.dnf()) writer.append(String.valueOf(i+1));
                writer.append(",").append(String.valueOf(results[i])).append("\n");
            }
        }
    }

    public void printDetailedResults() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_path))) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
        }
    }

    private void printDetailedResultsHeader(OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg = 1; leg <= number_of_legs; leg++) {
            writer.append("Runners ").append(String.valueOf(leg)).append(",Leg ").append(String.valueOf(leg)).append(",");
            if (leg < number_of_legs) writer.append("Split ").append(String.valueOf(leg)).append(",");
        }

        writer.append("Total\n");
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (OverallResult result : results) {

            Team team = result.team;

            int bib_number = team.bib_number;

            if (!result.dnf()) writer.append(String.valueOf(position++));
            writer.append(",");
            writer.append(String.valueOf(bib_number)).append(",");
            writer.append(team.name).append(",");
            writer.append(team.category.toString()).append(",");

            boolean previous_leg_dnf = false;

            for (int leg = 1; leg <= number_of_legs; leg++) {

                LegResult leg_result = result.leg_results[leg - 1];

                writer.append(team.runners[leg - 1]);
                writer.append(",");
                writer.append(leg_result.DNF ? DNF_STRING : OverallResult.format(leg_result.duration()));
                writer.append(",");
                writer.append(leg_result.DNF || previous_leg_dnf ? DNF_STRING : OverallResult.format(sumDurationsUpToLeg(result.leg_results, leg)));

                if (leg < number_of_legs) writer.append(",");

                if (leg_result.DNF) previous_leg_dnf = true;
            }

            writer.append("\n");
        }
    }

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= number_of_legs; leg++) {

            Path leg_results_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".csv");

            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(leg_results_path))) {

                printLegResultsHeader(leg, writer);

                LegResult[] leg_results = new LegResult[results.length];

                for (int i = 0; i < leg_results.length; i++) {
                    leg_results[i] = results[i].leg_results[leg-1];
                }

                Arrays.sort(leg_results);

                for (int i = 0; i < leg_results.length; i++) {
                    LegResult leg_result = leg_results[i];
                    if (!leg_result.DNF) {
                        writer.append(String.valueOf(i+1));
                        writer.append(",").append(leg_result.toString()).append("\n");
                    }
                }
            }
        }
    }

    private void printLegResultsHeader(int leg, OutputStreamWriter writer) throws IOException {

        writer.append("Pos,Runner");
        if (paired_legs[leg - 1]) writer.append("s");
        writer.append(",Time\n");
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
        OverallResult[] prize_results = new OverallResult[number_of_prizes];

        for (OverallResult result : results) {

            if (!result.dnf() && categoryMatch(category, position, result) && !prize_winners.contains(result.team) && position <= number_of_prizes) {

                prize_results[position - 1] = result;

                prize_winners.add(result.team);
                position++;
            }
        }

        for (int i = 0; i < number_of_prizes; i++) {

            OverallResult prize_result = prize_results[i];
            if (prize_result != null)
                writer.append(String.valueOf(i + 1)).append(",").append(String.valueOf(prize_result)).append("\n");
        }

        writer.append("\n\n");
    }

    private Duration earlierOf(Duration duration1, Duration duration2) {
        return duration1.compareTo(duration2) <= 0 ? duration1 : duration2;
    }

    private int findIndexOfNextLegResult(LegResult[] leg_results) {

        for (int i = 0; i < leg_results.length; i++) {
            if (leg_results[i].finish_time == null) return i;
        }
        throw new RuntimeException("surplus result recorded for team: ");
    }

    private int findIndexOfTeamWithBibNumber(int bib_number) {

        for (int i = 0; i < results.length; i++) {
            if (results[i].team.bib_number == bib_number) return i;
        }
        throw new RuntimeException("unregistered team: ");
    }

    private Duration sumDurationsUpToLeg(LegResult[] leg_results, int leg) {

        Duration total = leg_results[0].duration();
        for (int i = 1; i < leg; i++)
            total = total.plus(leg_results[i].duration());
        return total;
    }

    private boolean categoryMatch(final Category category, final int position, final OverallResult result) {

        // Only check for an older category being included in this category when looking for first prize,
        // since the first in an older category always gets that prize rather than a lower senior prize.

        // This won't work in the case where the top two teams are women or mixed...

        return category == result.team.category || (position == 1 && category.includes(result.team.category));
    }

    private void printDashes(final int n, final OutputStreamWriter writer) throws IOException {

        for (int i = 0; i < n; i++) writer.append("-");
        writer.append("\n");
    }
}


// code for dead heats
//        int number_of_results = overall_results.size();
//
//        for (int i = 0; i < number_of_results; i++) {
//
//            OverallResult result = overall_results.get(i);
//            int j = i;
//
//            while (j + 1 < number_of_results && result.overall_time.equals(overall_results.get(j + 1).overall_time)) j++;
//            if (j > i) {
//                for (int k = i; k <= j; k++)
//                    overall_results.get(k).position_string = i + 1 + "=";
//                i = j;
//            }
//            else
//                result.position_string = String.valueOf(i + 1);
//        }