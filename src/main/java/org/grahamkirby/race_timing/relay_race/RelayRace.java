/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;

public class RelayRace extends SingleRace {

    private record IndividualLegStart(int bib_number, int leg_number, Duration start_time) {}
    private record ResultWithLegIndex(RelayRaceResult result, int leg_index) {}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int DEFAULT_NUMBER_OF_SENIOR_PRIZES = 3;
    private static final int DEFAULT_NUMBER_OF_CATEGORY_PRIZES = 1;

    private static final String ZERO_TIME_STRING = "0:0:0";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected int number_of_legs;
    private int senior_prizes, category_prizes;
    private RelayRaceMissingData missing_data;

    // Records for each leg whether there was a mass start.
    private List<Boolean> mass_start_legs;

    // Times relative to start of leg 1 at which each mass start occurred.
    // For leg 2 onward, legs that didn't have a mass start are recorded with the time of the next actual
    // mass start. This allows e.g. for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
    private List<Duration> start_times_for_mass_starts;

    protected List<Boolean> paired_legs;
    private List<IndividualLegStart> individual_leg_starts;
    private Duration start_offset;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java RelayRace <config file path>");
        else {
            new RelayRace(Paths.get(args[0])).processResults();
        }
    }

    @Override
    public boolean allowEqualPositions() {

        // No dead heats for overall results, since an ordering is imposed at finish funnel for final leg runners.
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure() throws IOException {

        super.configure();
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
        calculateResults();
        printResults();
    }

    private void initialiseResults() {

        for (final RaceEntry entry : entries)
            overall_results.add(new RelayRaceResult((RelayRaceEntry) entry, number_of_legs, this));
    }

    private void calculateResults() {

        interpolateMissingTimes();
        guessMissingBibNumbers();
        fillLegFinishTimes();
        fillDNFs();
        fillLegResultDetails();
        sortOverallResults();
        allocatePrizes();
        addPaperRecordingComments();
    }

    private void printResults() throws IOException {

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printPrizes();
        printCombined();
        printCollatedTimes();
        printNotes();
    }

    @Override
    protected void configureInputData() throws IOException {

        super.configureInputData();
        ((RelayRaceInput)input).loadTimeAnnotations(raw_results);
    }

    @Override
    protected void fillDNF(final String dnf_string) {

        try {
            final ResultWithLegIndex result_with_leg = getResultWithLegIndex(dnf_string);
            result_with_leg.result.leg_results.get(result_with_leg.leg_index).DNF = true;
        }
        catch (Exception e) {
            throw new RuntimeException("illegal DNF time");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void readProperties() {

        number_of_legs = Integer.parseInt(getProperties().getProperty(KEY_NUMBER_OF_LEGS));
        start_offset = parseTime(getPropertyWithDefault(KEY_START_OFFSET, ZERO_TIME_STRING));
        senior_prizes = Integer.parseInt(getPropertyWithDefault(KEY_SENIOR_PRIZES, String.valueOf(DEFAULT_NUMBER_OF_SENIOR_PRIZES)));
        category_prizes = Integer.parseInt(getPropertyWithDefault(KEY_CATEGORY_PRIZES, String.valueOf(DEFAULT_NUMBER_OF_CATEGORY_PRIZES)));
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

    protected int getRecordedLegPosition(final int bib_number, final int leg_number) {

        int legs_completed = 0;

        for (int i = 0; i < raw_results.size(); i++) {

            final int result_bib_number = raw_results.get(i).getBibNumber();

            if (result_bib_number == bib_number) {
                legs_completed++;
                if (legs_completed == leg_number) return i + 1;
            }
        }

        return Integer.MAX_VALUE;
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

        // Example: MASS_START_ELAPSED_TIMES = 23:59:59,23:59:59,23:59:59,2:36:00

        final String default_string = (DUMMY_DURATION_STRING + ",").repeat(number_of_legs - 1) + DUMMY_DURATION_STRING;
        return getPropertyWithDefault(KEY_MASS_START_ELAPSED_TIMES, default_string);
    }

    private void setMassStartTimes(final String[] mass_start_elapsed_times_strings) {

        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            setMassStartTime(mass_start_elapsed_times_strings[leg_index], leg_index);
    }

    private void setMassStartTime(final String time_as_string, final int leg_index) {

        final Duration mass_start_time = parseTime(time_as_string);
        final Duration previous_mass_start_time = leg_index > 0 ? start_times_for_mass_starts.get(leg_index -1) : null;

        if (massStartTimesOutOfOrder(previous_mass_start_time, mass_start_time))
            throw new RuntimeException("illegal mass start time order");

        start_times_for_mass_starts.add(mass_start_time);
        mass_start_legs.add(!mass_start_time.equals(DUMMY_DURATION));
    }

    private boolean massStartTimesOutOfOrder(final Duration previous_mass_start_time, final Duration current_mass_start_time) {

        return previous_mass_start_time != null &&
                !previous_mass_start_time.equals(DUMMY_DURATION) &&
                previous_mass_start_time.compareTo(current_mass_start_time) > 0;
    }

    private void setEmptyMassStartTimes() {

        // For legs 2 and above, if there is no mass start time configured, use the next actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.

        for (int leg_index = number_of_legs - 2; leg_index > 0; leg_index--)

            if (start_times_for_mass_starts.get(leg_index).equals(DUMMY_DURATION))
                start_times_for_mass_starts.set(leg_index, start_times_for_mass_starts.get(leg_index+1));
    }

    private void configurePairedLegs() {

        final String paired_legs_string = getProperties().getProperty(KEY_PAIRED_LEGS);

        // Example: PAIRED_LEGS = 2,3

        paired_legs = new ArrayList<>();

        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            paired_legs.add(false);

        for (final String leg_number_as_string : paired_legs_string.split(","))
            paired_legs.set(Integer.parseInt(leg_number_as_string) - 1, true);
    }

    private void configureIndividualLegStarts() {

        final String individual_leg_starts_string = getPropertyWithDefault(KEY_INDIVIDUAL_LEG_STARTS, "");

        // bib number / leg number / start time
        // Example: INDIVIDUAL_LEG_STARTS = 2/1/0:10:00,26/3/2:41:20

        individual_leg_starts = new ArrayList<>();

        if (!individual_leg_starts_string.isBlank())
            for (final String s : individual_leg_starts_string.split(","))
                individual_leg_starts.add(getIndividualLegStart(s));
    }

    private IndividualLegStart getIndividualLegStart(final String individual_leg_starts_string) {

        final String[] split = individual_leg_starts_string.split("/");

        final int bib_number = Integer.parseInt(split[0]);
        final int leg_number = Integer.parseInt(split[1]);
        final Duration start_time = parseTime(split[2]);

        return new IndividualLegStart(bib_number, leg_number, start_time);
    }

    private void fillLegFinishTimes() {

        recordLegResults();
        sortLegResults();
    }

    private void recordLegResults() {

        for (final RawResult raw_result : raw_results)
            if (raw_result.getBibNumber() > -1)
                recordLegResult((RelayRaceRawResult)raw_result);
    }

    private void recordLegResult(final RelayRaceRawResult raw_result) {

        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult)overall_results.get(team_index);

        final int leg_index = findIndexOfNextUnfilledLegResult(result.leg_results);
        final LegResult leg_result = result.leg_results.get(leg_index);

        leg_result.finish_time = raw_result.getRecordedFinishTime().plus(start_offset);

        // Leg number will be zero in most cases, unless explicitly recorded in raw results.
        leg_result.leg_number = raw_result.getLegNumber();

        // Provisionally this leg is not DNF since a finish time was recorded.
        // However, it might still be set to DNF in fillDNFs() if the runner missed a checkpoint.
        leg_result.DNF = false;
    }

    private void sortLegResults() {

        for (final RaceResult result : overall_results)
            sortLegResults(result);
    }

    private void sortLegResults(final RaceResult result) {

        final List<LegResult> leg_results = ((RelayRaceResult)result).leg_results;

        // Sort by explicitly recorded leg number.
        leg_results.sort(Comparator.comparingInt(o -> o.leg_number));

        // Reset the leg numbers according to new positions in leg sequence.
        for (int leg_index = 1; leg_index <= leg_results.size(); leg_index++)
            leg_results.get(leg_index - 1).leg_number = leg_index;
    }

    private ResultWithLegIndex getResultWithLegIndex(final String bib_and_leg) {

        // String of form "bib-number/leg-number"

        final String[] elements = bib_and_leg.split("/");
        final int bib_number = Integer.parseInt(elements[0]);
        final int leg_number = Integer.parseInt(elements[1]);

        final RelayRaceResult result = (RelayRaceResult)overall_results.get(findIndexOfTeamWithBibNumber(bib_number));

        return new ResultWithLegIndex(result, leg_number - 1);
    }

    protected List<LegResult> getLegResults(final int leg_number) {

        final List<LegResult> leg_results = new ArrayList<>();

        for (final RaceResult overall_result : getOverallResults())
            leg_results.add(((RelayRaceResult)overall_result).leg_results.get(leg_number - 1));

        // Sort in order of increasing overall leg time, as defined in LegResult.compareTo().
        // Ordering for DNF results doesn't matter since they're omitted in output.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.
        // OutputCSV.printLegResults deals with dead heats.
        leg_results.sort(LegResult::compareTo);

        return leg_results;
    }

    protected void addMassStartAnnotation(final OutputStreamWriter writer, final LegResult leg_result, final int leg) throws IOException {

        // Adds e.g. "(M3)" after names of runners that started in leg 3 mass start.
        if (leg_result.in_mass_start) {

            // Find the next mass start.
            int mass_start_leg = leg;
            while (!(mass_start_legs.get(mass_start_leg-1)))
                mass_start_leg++;

            writer.append(" (M").append(String.valueOf(mass_start_leg)).append(")");
        }
    }

    protected Duration sumDurationsUpToLeg(final List<LegResult> leg_results, final int leg) {

        Duration total = Duration.ZERO;
        for (int i = 0; i < leg; i++)
            total = total.plus(leg_results.get(i).duration());
        return total;
    }

    private void fillLegResultDetails() {

        for (final RaceResult result : overall_results)
            fillLegResultDetails(result);
    }

    private void fillLegResultDetails(final RaceResult result) {

        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            fillLegResultDetails(((RelayRaceResult)result).leg_results, leg_index);
    }

    private void fillLegResultDetails(final List<LegResult> leg_results, final int leg_index) {

        final LegResult leg_result = leg_results.get(leg_index);

        final Duration individual_start_time = getIndividualStartTime(leg_result, leg_index);
        final Duration leg_mass_start_time = start_times_for_mass_starts.get(leg_index);
        final Duration previous_team_member_finish_time = leg_index > 0 ? leg_results.get(leg_index - 1).finish_time : null;

        leg_result.start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);

        // Record whether the runner started in a mass start.
        leg_result.in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);
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

    private int findIndexOfNextUnfilledLegResult(final List<LegResult> leg_results) {

        for (int i = 0; i < leg_results.size(); i++)
            if (leg_results.get(i).finish_time == null) return i;

        throw new RuntimeException("surplus result recorded for team: " + leg_results.getFirst().entry.bib_number);
    }

    private int findIndexOfTeamWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.size(); i++)
            if (((RelayRaceResult)overall_results.get(i)).entry.bib_number == bib_number) return i;

        throw new RuntimeException("unregistered team: " + bib_number);
    }

    private void addPaperRecordingComments() {

        for (int i = 0; i < raw_results.size(); i++) {

            final boolean last_electronically_recorded_result = i == ((RelayRaceInput)input).getNumberOfRawResults() - 1;

            if (last_electronically_recorded_result && ((RelayRaceInput)input).getNumberOfRawResults() < raw_results.size())
                raw_results.get(i).appendComment("Remaining times from paper recording sheet only.");
        }
    }

    private void printOverallResults() throws IOException {

        output_CSV.printOverallResults(false);
        output_HTML.printOverallResults(true);
    }

    private void printDetailedResults() throws IOException {

        output_CSV.printDetailedResults(false);
        output_HTML.printDetailedResults(true);
    }

    private void printLegResults() throws IOException {

        ((RelayRaceOutputCSV)output_CSV).printLegResults();
        ((RelayRaceOutputHTML)output_HTML).printLegResults(true);
    }

    private void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    private void printNotes() throws IOException {

        output_text.printNotes();
    }

    private void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    private void printCollatedTimes() throws IOException {

        ((RelayRaceOutputText)output_text).printCollatedResults();
    }
}
