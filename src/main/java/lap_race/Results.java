package lap_race;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class Results {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  1. Create new copy of directory src/main/resources/lap_race/devils_burdens/sample_config.   //
    //                                                                                              //
    //  2. Edit WORKING_DIRECTORY property to location of new directory.                            //
    //                                                                                              //
    //  3. Update year and mass start times.                                                        //
    //                                                                                              //
    //  4. Run Results, passing path of config file as command line parameter.                      //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record IndividualLegStart(int bib_number, int leg_number, Duration start_time) {}

    public static final String DUMMY_DURATION_STRING = "23:59:59";
    public static final Duration DUMMY_DURATION = RawResult.parseTime(DUMMY_DURATION_STRING);

    static final Duration ZERO_TIME = RawResult.parseTime("0:0");

    //////////////////////////////////////////////////////////////////////////////////////////////////

    Properties properties;

    Input input;
    Output output_CSV, output_HTML, output_text, output_PDF;
    Prizes prizes;

    Path working_directory_path;

    int number_of_legs;
    String dnf_legs_string;

    Team[] entries;
    RawResult[] raw_results;
    OverallResult[] overall_results;
    final Map<Category, List<Team>> prize_winners = new HashMap<>();

    Duration[] start_times_for_mass_starts;  // Relative to start of leg 1.

    // Subtly different from start_times_for_mass_starts: there may be a mass start time recorded for a leg even though
    // it's not a leg with a mass start, to allow for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
    boolean[] mass_start_legs;

    boolean[] paired_legs;

    IndividualLegStart[] individual_leg_starts;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Results(final String config_file_path) throws IOException {
        this(readProperties(config_file_path));
    }

    public Results(final Properties properties) throws IOException {

        this.properties = properties;
        configure();
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            new Results(args[0]).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void processResults() throws IOException {

        initialiseResults();

        fillLegFinishTimes();
        fillDNFs();
        fillLegStartTimes();
        calculateResults();
        allocatePrizes();

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printPrizes();
    }

    private static Properties readProperties(final String config_file_path) throws IOException {

        try (final FileInputStream in = new FileInputStream(config_file_path)) {

            final Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    private void configure() throws IOException {

        readProperties();
        configureHelpers();
        configureInputData();
        configureMassStarts();
        configurePairedLegs();
        configureIndividualLegStarts();
    }

    private void readProperties() {

        working_directory_path = Paths.get(properties.getProperty("WORKING_DIRECTORY"));

        number_of_legs = Integer.parseInt(properties.getProperty("NUMBER_OF_LEGS"));
        dnf_legs_string = properties.getProperty("DNF_LEGS");
    }

    private void configureHelpers() throws IOException {

        input = new Input(this);

        output_CSV = new OutputCSV(this);
        output_HTML = new OutputHTML(this);
        output_text = new OutputText(this);
        output_PDF = new OutputPDF(this);

        prizes = new Prizes(this);
    }

    private void configureInputData() throws IOException {

        entries = input.loadEntries();
        raw_results = input.loadRawResults();
    }

    private void configureMassStarts() {

        String mass_start_elapsed_times_string = properties.getProperty("MASS_START_ELAPSED_TIMES");

        if (mass_start_elapsed_times_string.isBlank())
            mass_start_elapsed_times_string = (DUMMY_DURATION_STRING + ",").repeat(number_of_legs - 1) + DUMMY_DURATION_STRING;

        start_times_for_mass_starts = new Duration[number_of_legs];
        mass_start_legs = new boolean[number_of_legs];

        int leg_index = 0;
        Duration previous_mass_start_time = null;

        for (String time_as_string : mass_start_elapsed_times_string.split(",")) {
            Duration mass_start_time;
            try {
                mass_start_time = RawResult.parseTime(time_as_string);
            }
            catch (Exception e) {
                throw new RuntimeException("illegal mass start time: " + time_as_string);
            }
            start_times_for_mass_starts[leg_index] = mass_start_time;
            mass_start_legs[leg_index] = !mass_start_time.equals(DUMMY_DURATION);
            if (previous_mass_start_time != null && !(previous_mass_start_time.equals(DUMMY_DURATION)) && previous_mass_start_time.compareTo(start_times_for_mass_starts[leg_index]) > 0)
                throw new RuntimeException("illegal mass start time order");
            previous_mass_start_time = start_times_for_mass_starts[leg_index];
            leg_index++;
        }

        // If there is no mass start configured for leg 2,
        // use the leg 3 mass start time for leg 2 too.
        // This covers the case where the leg 1 runner finishes after a mass start.
        if (start_times_for_mass_starts[1].equals(RawResult.parseTime(DUMMY_DURATION_STRING))) {
            start_times_for_mass_starts[1] = start_times_for_mass_starts[2];
        }
    }

    private void configurePairedLegs() {

        final String paired_legs_string = properties.getProperty("PAIRED_LEGS");
        paired_legs = new boolean[number_of_legs];

        for (String s : paired_legs_string.split(",")) {
            paired_legs[Integer.parseInt(s) - 1] = true;
        }
    }

    private void configureIndividualLegStarts() {

        String individual_leg_starts_string = properties.getProperty("INDIVIDUAL_LEG_STARTS");
        if (individual_leg_starts_string == null)
            individual_leg_starts = new IndividualLegStart[0];
        else {
            String[] strings = individual_leg_starts_string.split(",");
            individual_leg_starts = new IndividualLegStart[strings.length];

            for (int i = 0; i < strings.length; i++) {
                String[] split = strings[i].split("/");
                final int bib_number = Integer.parseInt(split[0]);
                final int leg_number = Integer.parseInt(split[1]);
                final Duration start_time = RawResult.parseTime(split[2]);
                individual_leg_starts[i] = new IndividualLegStart(bib_number, leg_number, start_time);
            }
        }
    }

    private void initialiseResults() {

        overall_results = new OverallResult[entries.length];

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = new OverallResult(entries[i], number_of_legs, this);
    }

    private void fillLegFinishTimes() {

        for (final RawResult raw_result : raw_results) {

            try {
                final int team_index = findIndexOfTeamWithBibNumber(raw_result.bib_number);
                final OverallResult result = overall_results[team_index];
                final LegResult[] leg_results = result.leg_results;

                final int leg_index = findIndexOfNextUnfilledLegResult(leg_results);
                leg_results[leg_index].finish_time = raw_result.recorded_finish_time;
                leg_results[leg_index].DNF = false;
            }
            catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage() + raw_result.bib_number);
            }
        }
    }

    private void fillDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not visiting all
        // checkpoints.

        // DNF cases where there is no recorded leg result are captured by the
        // default value of LegResult.DNF being true.

        if (!dnf_legs_string.isBlank()) {

            for (final String dnf_string : dnf_legs_string.split(",")) {

                try {
                    final String[] dnf = dnf_string.split("/");
                    final int bib_number = Integer.parseInt(dnf[0]);
                    final int leg_number = Integer.parseInt(dnf[1]);
                    final int leg_index = leg_number - 1;

                    final OverallResult result = overall_results[findIndexOfTeamWithBibNumber(bib_number)];
                    result.leg_results[leg_index].DNF = true;
                }
                catch (Exception e) {
                    throw new RuntimeException("illegal DNF time");
                }
            }
        }
    }

    private void fillLegStartTimes() {

        for (final OverallResult result : overall_results) {

            final LegResult[] leg_results = result.leg_results;
            leg_results[0].start_time = ZERO_TIME;   // All times are relative to start of leg 1.

            for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
                fillLegStartTime(leg_results, leg_index);
        }
    }

    private void fillLegStartTime(final LegResult[] leg_results, final int leg_index) {

        // If there isn't a recorded finish time for the previous leg then we can't set
        // a start time for this one. Both legs will be DNF.

        // Even if the previous leg does have a recorded finish time it still might be
        // DNF, if the runners reported missing a checkpoint, but it can still be used
        // to set this leg's start time.

        // If the previous leg finish was after the mass start for this leg, allow for
        // this leg runner(s) starting in the mass start rather than when the previous
        // leg runner(s) finished.

        // First leg always starts at zero, but there might be an individual start time
        // recorded for a leg 1 runner.


        // Look for an individual start time.
        Duration individual_start_time = null;

        for (IndividualLegStart individual_leg_start : individual_leg_starts) {
            if (individual_leg_start.bib_number == leg_results[leg_index].team.bib_number && individual_leg_start.leg_number == leg_index + 1)
                individual_start_time = individual_leg_start.start_time;
        }

        if (individual_start_time != null) {
            leg_results[leg_index].start_time = individual_start_time;
        }
        else {
            if (leg_index > 0) {

                final Duration mass_start_time = start_times_for_mass_starts[leg_index];
                final int previous_leg_index = leg_index - 1;

                if (leg_results[previous_leg_index].finish_time == null) {

                    // Leg result will already be set to DNF.
                    leg_results[leg_index].in_mass_start = mass_start_legs[leg_index];
                } else {
                    leg_results[leg_index].start_time = earlierOf(mass_start_time, leg_results[previous_leg_index].finish_time);
                    leg_results[leg_index].in_mass_start = mass_start_time.compareTo(leg_results[previous_leg_index].finish_time) < 0;
                }
            }
        }
    }

    private void calculateResults() {

        Arrays.sort(overall_results);
    }

    private void allocatePrizes() throws IOException {

        prizes.allocatePrizes();
    }

    private void printOverallResults() throws IOException {

        output_CSV.printOverallResults();
        output_HTML.printOverallResults();
    }

    private void printDetailedResults() throws IOException {

        output_CSV.printDetailedResults();
        output_HTML.printDetailedResults();
    }

    private void printLegResults() throws IOException {

        output_CSV.printLegResults();
        output_HTML.printLegResults();
    }

    private void printPrizes() throws IOException {

        output_text.printPrizes();
        output_PDF.printPrizes();
    }

    LegResult[] getLegResults(final int leg) {

        final LegResult[] leg_results = new LegResult[overall_results.length];

        for (int i = 0; i < leg_results.length; i++)
            leg_results[i] = overall_results[i].leg_results[leg-1];

        Arrays.sort(leg_results);
        return leg_results;
    }

    Integer getRecordedLegPosition(final int bib_number, final int leg_number) {

        int count = 0;

        for (int i = 0; i < raw_results.length; i++) {
            if (raw_results[i].bib_number == bib_number) {
                count++;
                if (count == leg_number) return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    private Duration earlierOf(final Duration duration1, final Duration duration2) {

        return duration1.compareTo(duration2) <= 0 ? duration1 : duration2;
    }

    private int findIndexOfNextUnfilledLegResult(final LegResult[] leg_results) {

        for (int i = 0; i < leg_results.length; i++)
            if (leg_results[i].finish_time == null) return i;

        throw new RuntimeException("surplus result recorded for team: ");
    }

    int findIndexOfTeamWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.length; i++)
            if (overall_results[i].team.bib_number == bib_number) return i;

        throw new RuntimeException("unregistered team: ");
    }
}
