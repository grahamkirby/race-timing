package relay_race;

import common.*;
import single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RelayRace extends SingleRace {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record IndividualLegStart(int bib_number, int leg_number, Duration start_time) {}
    private record ResultWithLegIndex(RelayRaceResult result, int leg_index) {}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private RelayRaceMissingData missing_data;
    protected int number_of_legs;
    private int senior_prizes, category_prizes;

    // Records for each leg whether there was a mass start.
    List<Boolean> mass_start_legs;

    // Times relative to start of leg 1 at which each mass start occurred.
    // For leg 2 onward, legs that didn't have a mass start are recorded with the time of the next actual
    // mass start. This allows e.g. for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
    private List<Duration> start_times_for_mass_starts;

    List<Boolean> paired_legs;
    private List<IndividualLegStart> individual_leg_starts;
    private Duration start_offset;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java LapRace <config file path>");
        else {
            new RelayRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();

        configureMassStarts();
        configurePairedLegs();
        configureIndividualLegStarts();
    }

    @Override
    public void processResults() throws IOException {

        initialiseResults();

        interpolateMissingTimes();
        guessMissingBibNumbers();
        fillLegFinishTimes();
        fillDNFs();
        fillLegStartTimes();
        sortOverallResults();
        allocatePrizes();

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printPrizes();
        printCombined();
        printCollatedTimes();
    }

    protected int getDefaultSeniorPrizes() {
        return 3;
    }

    protected int getDefaultCategoryPrizes() {
        return 1;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        super.readProperties();

        number_of_legs = Integer.parseInt(properties.getProperty("NUMBER_OF_LEGS"));
        start_offset = parseTime(getPropertyWithDefault("START_OFFSET", ZERO_TIME_STRING));
        senior_prizes = Integer.parseInt(getPropertyWithDefault("SENIOR_PRIZES", String.valueOf(getDefaultSeniorPrizes())));
        category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(getDefaultCategoryPrizes())));
    }

    private void configureHelpers() {

        input = new RelayRaceInput(this);

        output_CSV = new RelayRaceOutputCSV(this);
        output_HTML = new RelayRaceOutputHTML(this);
        output_text = new RelayRaceOutputText(this);
        output_PDF = new RelayRaceOutputPDF(this);

        missing_data = new RelayRaceMissingData(this);
        prizes = new RelayRacePrizes(this);
    }

    protected void configureCategories() {

        categories = new RelayRaceCategories(senior_prizes, category_prizes);
    }

    protected void configureInputData() throws IOException {

        super.configureInputData();
        ((RelayRaceInput)input).loadTimeAnnotations(raw_results);
    }

    private void interpolateMissingTimes() {

        missing_data.interpolateMissingTimes();
    }

    private void guessMissingBibNumbers() {

        missing_data.guessMissingBibNumbers();
    }

    private void configureMassStarts() {

        start_times_for_mass_starts = new ArrayList<>();
        mass_start_legs = new ArrayList<>();

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

    private void setMassStartTimes(final String[] mass_start_elapsed_times_strings) {

        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            setMassStartTime(mass_start_elapsed_times_strings[leg_index], leg_index);
    }

    private void setMassStartTime(final String time_as_string, final int leg_index) {

        final Duration mass_start_time = parseMassStartTime(time_as_string);
        final Duration previous_mass_start_time = leg_index > 0 ? start_times_for_mass_starts.get(leg_index -1) : null;

        if (massStartTimesOutOfOrder(previous_mass_start_time, mass_start_time))
            throw new RuntimeException("illegal mass start time order");

        start_times_for_mass_starts.add(mass_start_time);
        mass_start_legs.add(!mass_start_time.equals(DUMMY_DURATION));
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

            if (start_times_for_mass_starts.get(leg_index).equals(DUMMY_DURATION))
                start_times_for_mass_starts.set(leg_index, start_times_for_mass_starts.get(leg_index+1));
        }
    }

    private void configurePairedLegs() {

        final String paired_legs_string = properties.getProperty("PAIRED_LEGS");

        // Example: PAIRED_LEGS = 2,3

        paired_legs = new ArrayList<>();
        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            paired_legs.add(false);

        for (final String leg_number_as_string : paired_legs_string.split(",")) {
            paired_legs.set(Integer.parseInt(leg_number_as_string) - 1, true);
        }
    }

    private void configureIndividualLegStarts() {

        final String individual_leg_starts_string = getPropertyWithDefault("INDIVIDUAL_LEG_STARTS", "");

        // bib number / leg number / start time
        // Example: INDIVIDUAL_LEG_STARTS = 2/1/0:10:00,26/3/2:41:20

        individual_leg_starts = new ArrayList<>();

        if (!individual_leg_starts_string.isBlank()) {

            final String[] individual_leg_starts_strings = individual_leg_starts_string.split(",");

            for (String s : individual_leg_starts_strings)
                individual_leg_starts.add(getIndividualLegStart(s));
        }
    }

    private static IndividualLegStart getIndividualLegStart(final String individual_leg_starts_strings) {

        final String[] split = individual_leg_starts_strings.split("/");
        final int bib_number = Integer.parseInt(split[0]);
        final int leg_number = Integer.parseInt(split[1]);
        final Duration start_time = parseTime(split[2]);

        return new IndividualLegStart(bib_number, leg_number, start_time);
    }

    private void initialiseResults() {

        overall_results = new ArrayList<>();

        for (RaceEntry entry : entries)
            overall_results.add(new RelayRaceResult((RelayRaceEntry) entry, number_of_legs, this));
    }

    private void fillLegFinishTimes() {

        recordLegResults();
        sortLegResults();
    }

    private void recordLegResults() {

        for (final RawResult raw_result : raw_results)
            if (raw_result.getBibNumber() != null)
                recordLegResult(raw_result);
    }

    private void recordLegResult(final RawResult raw_result) {

        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult)overall_results.get(team_index);
        final List<LegResult> leg_results = result.leg_results;

        final int leg_index = findIndexOfNextUnfilledLegResult(leg_results);

        leg_results.get(leg_index).finish_time = raw_result.getRecordedFinishTime().plus(start_offset);

        // Leg number will be zero in most cases, unless explicitly recorded in raw results.
        leg_results.get(leg_index).leg_number = raw_result.getLegNumber();

        // Provisionally this leg is not DNF since a finish time was recorded.
        // However, it might still be set to DNF in fillDNFs() if the runner missed a checkpoint.
        leg_results.get(leg_index).DNF = false;
    }

    private void sortLegResults() {

        for (final RaceResult team_result : overall_results) {

            final List<LegResult> leg_results = ((RelayRaceResult)team_result).leg_results;

            // Sort by explicitly recorded leg number.
            leg_results.sort(Comparator.comparingInt(o -> o.leg_number));

            for (int leg_index = 0; leg_index < leg_results.size(); leg_index++)
                leg_results.get(leg_index).leg_number = leg_index+1;
        }
    }

    private ResultWithLegIndex getResultWithLegIndex(final String bib_and_leg) {

        // String of form "bib-number/leg-number"

        final String[] elements = bib_and_leg.split("/");
        final int bib_number = Integer.parseInt(elements[0]);
        final int leg_number = Integer.parseInt(elements[1]);
        final int leg_index = leg_number - 1;

        final RaceResult result = overall_results.get(findIndexOfTeamWithBibNumber(bib_number));

        return new ResultWithLegIndex((RelayRaceResult)result, leg_index);
    }

    private void fillDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not visiting all
        // checkpoints.

        // DNF cases where there is no recorded leg result are captured by the
        // default value of LegResult.DNF being true.

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                fillDNF(individual_dnf_string);
    }

    private void fillDNF(final String dnf_string) {

        try {
            final ResultWithLegIndex result_with_leg = getResultWithLegIndex(dnf_string);

            result_with_leg.result.leg_results.get(result_with_leg.leg_index).DNF = true;
        }
        catch (Exception e) {
            throw new RuntimeException("illegal DNF time");
        }
    }

    private void fillLegStartTimes() {

        for (final RaceResult overall_result : overall_results)
            for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
                fillLegStartTime(((RelayRaceResult)overall_result).leg_results, leg_index);
    }

    private void fillLegStartTime(final List<LegResult> leg_results, final int leg_index) {

        // Possible cases:

        // Individual start time recorded explicitly in the race configuration file (rare).
        // Zero if first leg.
        // Finish time of the previous leg runner.
        // Time of the relevant mass start if that was earlier that previous leg finish.
        // Null if no previous leg finish time was recorded: no time can be calculated for current leg so DNF.

        final Duration individual_start_time = getIndividualStartTime(leg_results.get(leg_index), leg_index);
        final Duration leg_mass_start_time = start_times_for_mass_starts.get(leg_index);
        final Duration previous_team_member_finish_time = leg_index > 0 ? leg_results.get(leg_index - 1).finish_time : null;

        leg_results.get(leg_index).start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);

        // Record whether the runner started in a mass start.
        leg_results.get(leg_index).in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);
    }

    private Duration getIndividualStartTime(final LegResult leg_result, final int leg_index) {

        for (final IndividualLegStart individual_leg_start : individual_leg_starts)
            if (individual_leg_start.bib_number == leg_result.entry.bib_number && individual_leg_start.leg_number == leg_index + 1)
                return individual_leg_start.start_time;

        return null;
    }

    private Duration getLegStartTime(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_team_member_finish_time, final int leg_index) {

        // Individual leg time recorded for this runner.
        if (individual_start_time != null) return individual_start_time;

        // Leg 1 runners start at time zero if there's no individual time recorded.
        if (leg_index == 0) return Duration.ZERO;

        // No finish time recorded for previous runner, so we can't record a start time for this one.
        // This leg result will be set to DNF by default.
        if (previous_team_member_finish_time == null) return null;

        // Use the earlier of the mass start time and the previous runner's finish time.
        return mass_start_time.compareTo(previous_team_member_finish_time) < 0 ? mass_start_time : previous_team_member_finish_time;
    }

    private boolean isInMassStart(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // Not in mass start if there is an individually recorded time, or it's the first leg.
        if (individual_start_time != null || leg_index == 0) return false;

        // No previously record leg time, so record this runner as starting in mass start if it's a mass start leg.
        if (previous_runner_finish_time == null) return mass_start_legs.get(leg_index);

        // Record this runner as starting in mass start if the previous runner finished after the relevant mass start.
        return mass_start_time.compareTo(previous_runner_finish_time) < 0;
    }

    private void sortOverallResults() {

        // Sort in order of increasing overall team time, as defined in OverallResult.compareTo().
        // DNF results are sorted in increasing order of bib number.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.
        overall_results.sort(RelayRaceResult::compare);
    }

    int getRecordedLegPosition(final int bib_number, final int leg_number) {

        int legs_completed = 0;

        for (int i = 0; i < raw_results.size(); i++) {
            if (raw_results.get(i).getBibNumber() != null && raw_results.get(i).getBibNumber() == bib_number) {
                legs_completed++;
                if (legs_completed == leg_number) return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    private int findIndexOfNextUnfilledLegResult(final List<LegResult> leg_results) {

        for (int i = 0; i < leg_results.size(); i++)
            if (leg_results.get(i).finish_time == null) return i;

        throw new RuntimeException("surplus result recorded for team: " + leg_results.get(0).entry.bib_number);
    }

    int findIndexOfTeamWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.size(); i++)
            if (((RelayRaceResult)overall_results.get(i)).entry.bib_number == bib_number) return i;

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

        ((RelayRaceOutput)output_CSV).printLegResults();
        ((RelayRaceOutput)output_HTML).printLegResults();
    }

    private void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    private void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    private void printCollatedTimes() throws IOException {

        ((RelayRaceOutputText)output_text).printCollatedResults();
    }
}
