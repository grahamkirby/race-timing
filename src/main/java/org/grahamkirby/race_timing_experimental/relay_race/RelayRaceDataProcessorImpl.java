/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.relay_race;

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceData;
import org.grahamkirby.race_timing_experimental.common.RaceDataProcessor;
import org.grahamkirby.race_timing_experimental.common.RaceEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Normalisation.KEY_ENTRY_COLUMN_MAP;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.Race.COMMENT_SYMBOL;
import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;
import static org.grahamkirby.race_timing_experimental.common.CommonDataProcessor.*;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

public class RelayRaceDataProcessorImpl implements RaceDataProcessor {

    private Race race;
    private final Map<RawResult, Integer> explicitly_recorded_leg_numbers = new HashMap<>();

    @Override
    public void setRace(Race race) {

        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceData getRaceData() {

        final Path entries_path = (Path) race.getConfig().get(KEY_ENTRIES_PATH);
        final Path electronic_results_path = (Path) race.getConfig().get(KEY_RAW_RESULTS_PATH);
        final Path paper_results_path = (Path) race.getConfig().get(KEY_PAPER_RESULTS_PATH);

        try {
            validateDataFiles(entries_path, electronic_results_path, paper_results_path);

            final List<RaceEntry> entries = loadEntries(entries_path);

            final List<RawResult> electronically_recorded_raw_results = loadRawResults(electronic_results_path);
            final List<RawResult> paper_recorded_raw_results = loadRawResults(paper_results_path);
            final List<RawResult> combined_raw_results = append(electronically_recorded_raw_results, paper_recorded_raw_results);

            loadTimeAnnotations(combined_raw_results);
            validateData(entries, entries_path, combined_raw_results, electronic_results_path, paper_results_path);

            return new RelayRaceDataImpl(entries, combined_raw_results, explicitly_recorded_leg_numbers, electronically_recorded_raw_results.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateDataFiles(final Path entries_path, final Path electronic_results_path, final Path paper_results_path) throws IOException {

        validateEntriesNumberOfElements(entries_path, (int) race.getConfig().get(KEY_NUMBER_OF_LEGS) + 3, (String) race.getConfig().get(KEY_ENTRY_COLUMN_MAP));
        validateEntryCategories(entries_path);
        validateBibNumbersUnique(entries_path);

        validateRawResults(electronic_results_path);
        validateRawResults(paper_results_path);

        validateNumberOfLegResults(electronic_results_path, paper_results_path);
        validateRawResultsOrdering(electronic_results_path);
        validateRawResultsOrdering(paper_results_path);
    }

    private void validateData(final List<RaceEntry> entries, final Path entries_path, final List<RawResult> combined_raw_results, final Path electronic_results_path, Path paper_results_path) {

        validateEntriesUnique(entries, entries_path);
        validateRecordedBibNumbersAreRegistered(entries, combined_raw_results, electronic_results_path, paper_results_path);
    }

    private List<RawResult> append(final List<RawResult> list1, final List<RawResult> list2) {

        final List<RawResult> result = new ArrayList<>(list1);
        result.addAll(list2);
        return result;
    }

    protected List<RawResult> loadRawResults(final Path results_path) throws IOException {

        return readAllLines(results_path).stream().
            map(SingleRaceInput::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(this::makeRawResult).
            toList();
    }

    private RawResult makeRawResult(final String line) {

        final RawResult result = new RawResult(line);

        final int leg_number = getExplicitLegNumber(line);
        if (leg_number > 0) explicitly_recorded_leg_numbers.put(result, leg_number);

        return result;
    }

    private int getExplicitLegNumber(final String line) {

        final String[] elements = line.split("\t");
        return elements.length > 2 ? Integer.parseInt(elements[2]) : 0;
    }

    private void validateRecordedBibNumbersAreRegistered(final List<RaceEntry> entries, final List<RawResult> raw_results, final Path electronic_results_path, final Path paper_results_path) {

        final Set<Integer> entry_bib_numbers = entries.stream().
            map(entry -> entry.bib_number).
            collect(Collectors.toSet());

        for (final RawResult raw_result : raw_results) {
            final int result_bib_number = raw_result.getBibNumber();
            if (result_bib_number != UNKNOWN_BIB_NUMBER && !entry_bib_numbers.contains(result_bib_number)) {
                String message = STR."invalid bib number '\{result_bib_number}' in file '\{electronic_results_path.getFileName()}'";
                if (paper_results_path != null) message += STR." or '\{paper_results_path.getFileName()}'";
                throw new RuntimeException(message);
            }
        }
    }

    private void validateEntryCategories(Path entries_path) {

        try {
            final AtomicInteger counter = new AtomicInteger(0);

            Files.readAllLines(entries_path).stream().
                map(SingleRaceInput::stripEntryComment).
                filter(Predicate.not(String::isBlank)).
                forEach(line -> {
                    try {
                        counter.incrementAndGet();
                        makeRelayRaceEntry(Arrays.stream(line.split("\t")).toList(), race);

                    } catch (final RuntimeException e) {
                        throw new RuntimeException(STR."invalid entry '\{e.getMessage()}' at line \{counter.get()} in file '\{entries_path.getFileName()}'", e);
                    }
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }

    private void validateEntriesUnique(final List<RaceEntry> entries, Path entries_path) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1.participant != entry2.participant && entry1.participant.equals(entry2.participant))
                    throw new RuntimeException(STR."duplicate entry '\{entry1}' in file '\{entries_path.getFileName()}'");
    }

    private void validateNumberOfLegResults(final Path raw_results_path, final Path paper_results_path) {

        try {
            final Map<String, Integer> bib_counts = new HashMap<>();

            countLegResults(bib_counts, raw_results_path);
            countLegResults(bib_counts, paper_results_path);

            for (final Map.Entry<String, Integer> entry : bib_counts.entrySet())
                if (entry.getValue() > (int) race.getConfig().get(KEY_NUMBER_OF_LEGS)) {
                    String message = STR."surplus result for team '\{entry.getKey()}' in file '\{raw_results_path.getFileName()}'";
                    if (paper_results_path != null)
                        message += STR." or '\{paper_results_path.getFileName()}'";
                    throw new RuntimeException(message);
                }
        } catch (final IOException e) {
            throw new RuntimeException("unexpected IO exception", e);
        }
    }

    private void countLegResults(final Map<String, Integer> bib_counts, final Path results_path) throws IOException {

        if (results_path != null)
            for (final String line : Files.readAllLines(results_path))
                // TODO rationalise with other comment handling. Use stripComment.
                if (!line.startsWith(COMMENT_SYMBOL) && !line.isBlank()) {

                    final String bib_number = line.split("\t")[0];
                    if (!bib_number.equals("?"))
                        bib_counts.put(bib_number, bib_counts.getOrDefault(bib_number, 0) + 1);
                }
    }

    void loadTimeAnnotations(final List<? extends RawResult> raw_results) throws IOException {

        final Path annotations_path = (Path) race.getConfig().get(KEY_ANNOTATIONS_PATH);

        if (annotations_path != null) {

            final List<String> lines = Files.readAllLines(annotations_path);

            // Skip header line.
            for (int line_index = 1; line_index < lines.size(); line_index++) {

                final String[] elements = lines.get(line_index).split("\t");

                // May add insertion option later.
                if (elements[0].equals("Update"))
                    updateResult(raw_results, elements);
            }
        }
    }

    private static void updateResult(final List<? extends RawResult> raw_results, final String[] elements) {

        final int position = Integer.parseInt(elements[1]);
        final RawResult raw_result = raw_results.get(position - 1);

        if (elements[2].equals("?")) raw_result.setBibNumber(UNKNOWN_BIB_NUMBER);
        else if (!elements[2].isEmpty()) raw_result.setBibNumber(Integer.parseInt(elements[2]));

        if (elements[3].equals("?")) raw_result.setRecordedFinishTime(null);
        else if (!elements[3].isEmpty()) raw_result.setRecordedFinishTime(parseTime(elements[3]));

        if (!elements[4].isEmpty()) raw_result.appendComment(elements[4]);
    }

    List<RaceEntry> loadEntries(Path entries_path) throws IOException {

        return Files.readAllLines(entries_path).stream().
            map(SingleRaceInput::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> makeRelayRaceEntry(Arrays.stream(line.split("\t")).toList(), race)).
            toList();
    }

    private static final int BIB_NUMBER_INDEX = 0;
    private static final int TEAM_NAME_INDEX = 1;
    private static final int CATEGORY_INDEX = 2;
    private static final int FIRST_RUNNER_NAME_INDEX = 3;

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    RaceEntry makeRelayRaceEntry(final List<String> elements, final Race race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        if (elements.size() != FIRST_RUNNER_NAME_INDEX + ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs())
            throw new RuntimeException("Invalid number of elements: " + String.join(" ", elements));

        int bib_number = Integer.parseInt(elements.get(BIB_NUMBER_INDEX));
        try {
            final String name = elements.get(TEAM_NAME_INDEX);
            final EntryCategory category = race.getCategoryDetails().lookupEntryCategory(elements.get(CATEGORY_INDEX));

            final List<String> runners = elements.subList(FIRST_RUNNER_NAME_INDEX, elements.size()).stream().map(s -> race.getNormalisation().cleanRunnerName(s)).toList();

            Participant participant = new Team(name, category, runners);

            return new RaceEntry(participant, bib_number, race);

        } catch (final RuntimeException e) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }
}
