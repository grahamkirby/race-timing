package lap_race;

import common.Race;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class LapRace extends Race {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record IndividualLegStart(int bib_number, int leg_number, Duration start_time) {}

    static final String DUMMY_DURATION_STRING = "23:59:59";
    static final Duration DUMMY_DURATION = parseTime(DUMMY_DURATION_STRING);
    static final String ZERO_TIME_STRING = "0:0:0";
    static final Duration ZERO_TIME = parseTime(ZERO_TIME_STRING);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    Input input;
    Output output_CSV, output_HTML, output_text, output_PDF;
    Prizes prizes;

    Path working_directory_path;

    int number_of_legs;

    // String read from configuration file specifying all the runners who did have a finish
    // time recorded but were declared DNF due to missing checkpoints.
    String dnf_legs_string;

    String leg_times_swap_string;

    Team[] entries;
    RawResult[] raw_results;
    TeamResult[] overall_results;
    Map<Category, List<Team>> prize_winners = new HashMap<>();

    // Records for each leg whether there was a mass start.
    boolean[] mass_start_legs;

    // Times relative to start of leg 1 at which each mass start occurred.
    // For leg 2 onward, legs that didn't have a mass start are recorded with the time of the next actual
    // mass start. This allows e.g. for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
    Duration[] start_times_for_mass_starts;

    boolean[] paired_legs;
    IndividualLegStart[] individual_leg_starts;
    Duration start_offset;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public LapRace(final String config_file_path) throws IOException {

        super(config_file_path);
    }

    public LapRace(final Properties properties) throws IOException {

        super(properties);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            new LapRace(args[0]).processResults();
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
        printCombined();
    }

    @Override
    protected void configure() throws IOException {

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
        leg_times_swap_string = getPropertyWithDefault("LEG_TIME_SWAPS", null);
        start_offset = parseTime(getPropertyWithDefault("START_OFFSET", ZERO_TIME_STRING));
    }

    private void configureHelpers() {

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

        start_times_for_mass_starts = new Duration[number_of_legs];
        mass_start_legs = new boolean[number_of_legs];

        setMassStartTimes(getMassStartElapsedTimesString().split(","));

        // If there is no mass start configured for legs 2 and above, use the first actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.
        setEmptyMassStartTimes();
    }

    private String getMassStartElapsedTimesString() {

        final String default_string = (DUMMY_DURATION_STRING + ",").repeat(number_of_legs - 1) + DUMMY_DURATION_STRING;
        return getPropertyWithDefault("MASS_START_ELAPSED_TIMES", default_string);

        // Example: MASS_START_ELAPSED_TIMES = 23:59:59,23:59:59,23:59:59,2:36:00
    }

    private void setMassStartTimes(String[] mass_start_elapsed_times_strings) {

        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            setMassStartTime(mass_start_elapsed_times_strings[leg_index], leg_index);
    }

    private void setMassStartTime(final String time_as_string, final int leg_index) {

        final Duration mass_start_time = parseMassStartTime(time_as_string);
        final Duration previous_mass_start_time = leg_index > 0 ? start_times_for_mass_starts[leg_index -1] : null;

        if (massStartTimesOutOfOrder(previous_mass_start_time, mass_start_time))
            throw new RuntimeException("illegal mass start time order");

        start_times_for_mass_starts[leg_index] = mass_start_time;
        mass_start_legs[leg_index] = !mass_start_time.equals(DUMMY_DURATION);
    }

    private static Duration parseMassStartTime(final String time_as_string) {

        try {
            return parseTime(time_as_string);
        }
        catch (Exception e) {
            throw new RuntimeException("illegal mass start time: " + time_as_string);
        }
    }

    private boolean massStartTimesOutOfOrder(final Duration previous_mass_start_time, final Duration current_mass_start_time) {

        return previous_mass_start_time != null && !previous_mass_start_time.equals(DUMMY_DURATION) && previous_mass_start_time.compareTo(current_mass_start_time) > 0;
    }

    private void setEmptyMassStartTimes() {

        // For legs 2 and above, if there is no mass start time configured, use the next actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.

        for (int leg_index = number_of_legs - 2; leg_index > 0; leg_index--) {

            if (start_times_for_mass_starts[leg_index].equals(parseTime(DUMMY_DURATION_STRING)))
                start_times_for_mass_starts[leg_index] = start_times_for_mass_starts[leg_index+1];
        }
    }

    private void configurePairedLegs() {

        final String paired_legs_string = properties.getProperty("PAIRED_LEGS");

        // Example: PAIRED_LEGS = 2,3

        paired_legs = new boolean[number_of_legs];

        for (final String leg_number_as_string : paired_legs_string.split(",")) {
            paired_legs[Integer.parseInt(leg_number_as_string) - 1] = true;
        }
    }

    private void configureIndividualLegStarts() {

        final String individual_leg_starts_string = getPropertyWithDefault("INDIVIDUAL_LEG_STARTS", "");

        // bib number / leg number / start time
        // Example: INDIVIDUAL_LEG_STARTS = 2/1/0:10:00,26/3/2:41:20

        if (individual_leg_starts_string.isBlank())
            individual_leg_starts = new IndividualLegStart[0];
        else {
            final String[] individual_leg_starts_strings = individual_leg_starts_string.split(",");
            individual_leg_starts = new IndividualLegStart[individual_leg_starts_strings.length];

            for (int i = 0; i < individual_leg_starts_strings.length; i++)
                individual_leg_starts[i] = getIndividualLegStart(individual_leg_starts_strings[i]);
        }
    }

    private static IndividualLegStart getIndividualLegStart(String individual_leg_starts_strings) {

        final String[] split = individual_leg_starts_strings.split("/");
        final int bib_number = Integer.parseInt(split[0]);
        final int leg_number = Integer.parseInt(split[1]);
        final Duration start_time = parseTime(split[2]);

        return new IndividualLegStart(bib_number, leg_number, start_time);
    }

    private String getPropertyWithDefault(final String property_key, final String default_value) {

        final String value = properties.getProperty(property_key);
        return value == null || value.isBlank() ? default_value : value;
    }

    private void initialiseResults() {

        overall_results = new TeamResult[entries.length];

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = new TeamResult(entries[i], number_of_legs, this);
    }

    private void fillLegFinishTimes() {

        for (final RawResult raw_result : raw_results) {

            final int team_index = findIndexOfTeamWithBibNumber(raw_result.bib_number);
            final TeamResult result = overall_results[team_index];
            final LegResult[] leg_results = result.leg_results;

            final int leg_index = findIndexOfNextUnfilledLegResult(leg_results);

            leg_results[leg_index].finish_time = raw_result.recorded_finish_time.plus(start_offset);

            // Provisionally this leg is not DNF since a finish time was recorded.
            // However, it might still be set to DNF in fillDNFs() if the runner missed a checkpoint.
            leg_results[leg_index].DNF = false;
        }

        if (leg_times_swap_string != null) {
            for (final String leg_time_swap_string : leg_times_swap_string.split(",")) {

                final String[] swap = leg_time_swap_string.split("/");
                final int bib_number = Integer.parseInt(swap[0]);
                final int leg_number = Integer.parseInt(swap[1]);
                final int leg_index = leg_number - 1;

                final TeamResult result = overall_results[findIndexOfTeamWithBibNumber(bib_number)];

                final Duration temp = result.leg_results[leg_index - 1].finish_time;
                result.leg_results[leg_index - 1].finish_time = result.leg_results[leg_index].finish_time;
                result.leg_results[leg_index].finish_time = temp;
            }
        }
    }

    private void fillDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not visiting all
        // checkpoints.

        // DNF cases where there is no recorded leg result are captured by the
        // default value of LegResult.DNF being true.

        if (dnf_legs_string != null && !dnf_legs_string.isBlank()) {

            for (final String dnf_string : dnf_legs_string.split(",")) {

                try {
                    final String[] dnf = dnf_string.split("/");
                    final int bib_number = Integer.parseInt(dnf[0]);
                    final int leg_number = Integer.parseInt(dnf[1]);
                    final int leg_index = leg_number - 1;

                    final TeamResult result = overall_results[findIndexOfTeamWithBibNumber(bib_number)];
                    result.leg_results[leg_index].DNF = true;
                }
                catch (Exception e) {
                    throw new RuntimeException("illegal DNF time");
                }
            }
        }
    }

    private void fillLegStartTimes() {

        for (final TeamResult overall_result : overall_results)
            for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
                fillLegStartTime(overall_result.leg_results, leg_index);
    }

    private void fillLegStartTime(final LegResult[] leg_results, final int leg_index) {

        // Possible cases:

        // Individual start time recorded explicitly in the race configuration file (rare).
        // Zero if first leg.
        // Finish time of the previous leg runner.
        // Time of the relevant mass start if that was earlier that previous leg finish.
        // Null if no previous leg finish time was recorded: no time can be calculated for current leg so DNF.

        final Duration individual_start_time = getIndividualStartTime(leg_results, leg_index);
        final Duration leg_mass_start_time = start_times_for_mass_starts[leg_index];
        final Duration previous_runner_finish_time = leg_index > 0 ? leg_results[leg_index - 1].finish_time : null;

        leg_results[leg_index].start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_runner_finish_time, leg_index);

        // Record whether the runner started in a mass start.
        leg_results[leg_index].in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_runner_finish_time, leg_index);
    }

    private Duration getIndividualStartTime(LegResult[] leg_results, int leg_index) {

        for (IndividualLegStart individual_leg_start : individual_leg_starts)
            if (individual_leg_start.bib_number == leg_results[leg_index].team.bib_number && individual_leg_start.leg_number == leg_index + 1)
                return individual_leg_start.start_time;

        return null;
    }

    private Duration getLegStartTime(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // Individual leg time recorded for this runner.
        if (individual_start_time != null) return individual_start_time;

        // Leg 1 runners start at time zero if there's no individual time recorded.
        if (leg_index == 0) return ZERO_TIME;

        // No finish time recorded for previous runner, so we can't record a start time for this one.
        // This leg result will be set to DNF by default.
        if (previous_runner_finish_time == null) return null;

        // Use the earlier of the mass start time and the previous runner's finish time.
        return mass_start_time.compareTo(previous_runner_finish_time) < 0 ? mass_start_time : previous_runner_finish_time;
    }

    private boolean isInMassStart(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // Not in mass start if there is an individually recorded time, or it's the first leg.
        if (individual_start_time != null || leg_index == 0) return false;

        // No previously record leg time, so record this runner as starting in mass start if it's a mass start leg.
        if (previous_runner_finish_time == null) return mass_start_legs[leg_index];

        // Record this runner as starting in mass start if the previous runner finished after the relevant mass start.
        return mass_start_time.compareTo(previous_runner_finish_time) < 0;
    }

    private void calculateResults() {

        // Sort in order of increasing overall team time, as defined in OverallResult.compareTo().
        // DNF results are sorted in increasing order of bib number.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.
        Arrays.sort(overall_results);
    }

    Integer getRecordedLegPosition(final int bib_number, final int leg_number) {

        int legs_completed = 0;

        for (int i = 0; i < raw_results.length; i++) {
            if (raw_results[i].bib_number == bib_number) {
                legs_completed++;
                if (legs_completed == leg_number) return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    private int findIndexOfNextUnfilledLegResult(final LegResult[] leg_results) {

        for (int i = 0; i < leg_results.length; i++)
            if (leg_results[i].finish_time == null) return i;

        throw new RuntimeException("surplus result recorded for team: " + leg_results[0].team.bib_number);
    }

    int findIndexOfTeamWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.length; i++)
            if (overall_results[i].team.bib_number == bib_number) return i;

        throw new RuntimeException("unregistered team: " + bib_number);
    }
    private void allocatePrizes() {

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
        output_HTML.printPrizes();
    }

    private void printCombined() throws IOException {

        ((OutputHTML)output_HTML).printCombined();
    }

    static Duration parseTime(String element) {

        element = element.strip();

        try {
            final String[] parts = element.split(":");
            final String time_as_ISO = "PT" + hours(parts) + minutes(parts) + seconds(parts);

            return Duration.parse(time_as_ISO);
        }
        catch (Exception e) {
            throw new RuntimeException("illegal time: " + element);
        }
    }

    static String hours(final String[] parts) {
        return parts.length > 2 ? parts[0] + "H" : "";
    }

    static String minutes(final String[] parts) {
        return (parts.length > 2 ? parts[1] : parts[0]) + "M";
    }

    static String seconds(final String[] parts) {
        return (parts.length > 2 ? parts[2] : parts[1]) + "S";
    }
}