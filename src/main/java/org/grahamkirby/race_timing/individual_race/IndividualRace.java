/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.NormalisationProcessor.*;
import static org.grahamkirby.race_timing.common.RaceConfigValidator.*;
import static org.grahamkirby.race_timing.common.RaceEntry.*;

public class IndividualRace implements SingleRaceInternal {

    private static final int NUMBER_OF_ENTRY_COLUMNS = 4;
    private static final int DUMMY_BIB_NUMBER = 0;

    private List<RaceEntry> entries;
    private List<RawResult> raw_results;
    private List<RaceResult> overall_results;

    private Map<Integer, Duration> separately_recorded_finish_times;

    // Even if there is more than one dead heat, all the bib numbers involved are loaded into one set,
    // since they can be distinguished by finish time.
    private Set<Integer> dead_heats;

    private final Config config;
    private CategoriesProcessor categories_processor;
    private NormalisationProcessor normalisation;
    private final NotesProcessor notes;

    private RaceResultsProcessor results_processor;
    private RaceOutput results_output;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRace(final Config config) {

        this.config = config;
        notes = new NotesProcessor();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceResults processResults() {

        try {
            loadRaceData();
            loadSeparatelyRecordedResults();
            loadDeadHeats();

            results_processor.calculateResults();
            return results_processor;
        }
        catch (final Exception e) {
            notes.appendToNotes(e.getMessage());
            return null;
        }
    }

    @Override
    public void outputResults(final RaceResults results) throws IOException {

        if (!results.getOverallResults().isEmpty())
            results_output.outputResults(results);
    }

    @Override
    public void outputNotes() throws IOException {

        results_output.printNotes(notes);
    }

    @Override
    public void outputRacerList() throws IOException {

        final OutputStream stream = results_output.getOutputStream("racers", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            for (final String line : makeRacerList(entries))
                writer.append(line + LINE_SEPARATOR);
        }
    }

    @Override
    public boolean configIsValid() {
        return results_processor != null && categories_processor != null && normalisation != null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialise() throws IOException {

        categories_processor = new CategoriesProcessor(config);
        normalisation = new NormalisationProcessor(config);
        results_output = new IndividualRaceOutput(config);
        results_processor = new IndividualRaceResultsProcessor(this);
    }

    @Override
    public void setOutput(final RaceOutput output) {
        this.results_output = output;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CategoriesProcessor getCategoriesProcessor() {
        return categories_processor;
    }

    @Override
    public RaceResultsProcessor getResultsProcessor() {
        return results_processor;
    }

    @Override
    public NormalisationProcessor getNormalisationProcessor() {

        return normalisation;
    }

    @Override
    public NotesProcessor getNotesProcessor() {
        return notes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<RaceEntry> getEntries() {
        return entries;
    }

    @Override
    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Map<Integer, Duration> getSeparatelyRecordedFinishTimes() {
        return separately_recorded_finish_times;
    }

    protected Set<Integer> getDeadHeats() {
        return dead_heats;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private List<String> makeRacerList(final List<RaceEntry> entries) {

        return entries.stream().
            map(this::makeRacerListEntry).
            toList();
    }

    private String makeRacerListEntry(final RaceEntry entry) {

        return entry.getBibNumber() + "\t" +
            getLastNameOfRunner(entry.getParticipant().getName()) + "\t" +
            getFirstNameOfRunner(entry.getParticipant().getName()) + "\t" +
            ((Runner) entry.getParticipant()).getClub() + "\t" +
            entry.getParticipant().getCategory().getShortName().charAt(0) + "\t\t" +
            entry.getParticipant().getCategory().getShortName();
    }

    private void loadRaceData() {

        final Path entries_path = config.getPath(KEY_ENTRIES_PATH);
        final Path raw_results_path = config.getPath(KEY_RAW_RESULTS_PATH);
        final Path overall_results_path = config.getPath(KEY_OVERALL_RESULTS_PATH);

        try {
            validateEntryDataFiles(entries_path);

            entries = loadEntries(entries_path);
            validateEntryData(entries, entries_path);

            if (raw_results_path != null) {
                validateResultsDataFiles(raw_results_path, overall_results_path);
                raw_results = loadRawResults(raw_results_path);
                overall_results = List.of();

                validateResultsData(entries, raw_results_path);
            }
            else if (overall_results_path != null) {
                validateResultsDataFiles(raw_results_path, overall_results_path);
                raw_results = List.of();
                overall_results = loadOverallResults(overall_results_path);
            }
            else {
                raw_results = List.of();
                overall_results = List.of();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<RaceEntry> loadEntries(final Path entries_path) throws IOException {

        return readAllLines(entries_path).stream().
            map(NormalisationProcessor::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> new RaceEntry(Arrays.stream(line.split("\t")).toList(), this)).
            toList();
    }

    private List<RawResult> loadRawResults(final Path raw_results_path) throws IOException {

        return readAllLines(raw_results_path).stream().
            map(NormalisationProcessor::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(RawResult::new).
            toList();
    }

    private List<RaceResult> loadOverallResults(final Path overall_results_path) {

        try {
            return readAllLines(overall_results_path).stream().
                map(NormalisationProcessor::stripComment).
                filter(Predicate.not(String::isBlank)).
                map(this::makeRaceResult).
                toList();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private RaceResult makeRaceResult(final String line) {

        final List<String> elements = makeMutableCopy(Arrays.stream(line.split("\t")).toList());

        elements.addFirst(String.valueOf(DUMMY_BIB_NUMBER));

        final RaceEntry entry = new RaceEntry(elements, this);
        final Duration finish_time = parseTime(elements.getLast());

        return new IndividualRaceResult(entry, finish_time, this);
    }

    private void validateEntryCategory(final String line) {

        final List<String> elements = Arrays.stream(line.split("\t")).toList();
        final NormalisationProcessor normalisation = getNormalisationProcessor();
        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            getCategoriesProcessor().getEntryCategory(category_name);

        } catch (final RuntimeException e) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    private void validateEntryDataFiles(final Path entries_path) throws IOException {

        validateEntriesNumberOfElements(entries_path, NUMBER_OF_ENTRY_COLUMNS, config.getString(KEY_ENTRY_COLUMN_MAP));
        validateEntryCategories(entries_path, this::validateEntryCategory);
        validateBibNumbers(entries_path);
    }

    private void validateResultsDataFiles(final Path raw_results_path, final Path overall_results_path) throws IOException {

        validateRawResults(raw_results_path);
        validateBibNumbers(raw_results_path);
        validateRawResultsOrdering(raw_results_path);

        // Number of columns in directly recorded results is same as for entries file: no bib number, but does have finish time.
        validateEntriesNumberOfElements(overall_results_path, NUMBER_OF_ENTRY_COLUMNS, null);
    }

    private void validateEntryData(final List<RaceEntry> entries, final Path entries_path) throws IOException {

        validateEntriesUnique(entries, entries_path);
    }

    private void validateResultsData(final List<RaceEntry> entries, final Path raw_results_path) throws IOException {

        validateRecordedBibNumbersAreRegistered(entries, raw_results_path);
    }

    private void validateEntriesUnique(final List<RaceEntry> entries, final Path entries_path) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1.getParticipant() != entry2.getParticipant() && entry1.getParticipant().equals(entry2.getParticipant()))
                    throw new RuntimeException("duplicate entry '" + entry1 + "' in file '" + entries_path.getFileName() + "'");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadSeparatelyRecordedResults() {

        separately_recorded_finish_times = new HashMap<>();

        final Consumer<Object> process_separately_recorded_results = value -> {

            for (final String s : ((String) value).split(",", -1)) {

                // Example: SEPARATELY_RECORDED_RESULTS = 126/8:09

                final String[] split = s.split("/", -1);
                final int bib_number = Integer.parseInt(split[0]);
                final Duration finish_time = parseTime(split[1]);

                separately_recorded_finish_times.put(bib_number, finish_time);
            }
        };

        config.processConfigIfPresent(KEY_SEPARATELY_RECORDED_RESULTS, process_separately_recorded_results);
    }

    private void loadDeadHeats() {

        dead_heats = new HashSet<>();

        final Consumer<Object> process_dead_heats = value -> {

            // Example: DEAD_HEATS = 10/29/4
            dead_heats = Arrays.stream(((String) value).split("/", -1)).map(Integer::parseInt).collect(Collectors.toSet());
        };

        config.processConfigIfPresent(KEY_DEAD_HEATS, process_dead_heats);
    }
}
