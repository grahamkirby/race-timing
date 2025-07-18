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
import org.grahamkirby.race_timing_experimental.common.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Normalisation.KEY_ENTRY_COLUMN_MAP;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

public class RelayRaceDataProcessorImpl implements RaceDataProcessor {

    private Race race;

    public RelayRaceDataProcessorImpl() {

        explicitly_recorded_leg_numbers = new HashMap<>();
    }

    @Override
    public void setRace(Race race) {
        this.race = race;

        paper_results_path = (Path) race.getConfig().get(KEY_PAPER_RESULTS_PATH);
        annotations_path = (Path) race.getConfig().get(KEY_ANNOTATIONS_PATH);
    }

    private Path paper_results_path, annotations_path;
    private Map<RawResult, Integer> explicitly_recorded_leg_numbers;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceData getRaceData() {

        Path entries_path = (Path) race.getConfig().get(KEY_ENTRIES_PATH);
        Path raw_results_path = (Path) race.getConfig().get(KEY_RAW_RESULTS_PATH);

        try {

            validateEntriesNumberOfElements(entries_path);
            validateEntryCategories(entries_path);
            validateBibNumbersUnique(entries_path);

            List<RaceEntry> entries = loadEntries(entries_path);
            List<RawResult> raw_results = loadRawResults(raw_results_path);

            validateEntriesUnique(entries, entries_path);

            return new RelayRaceDataImpl(entries, raw_results, explicitly_recorded_leg_numbers, number_of_raw_results);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void validateBibNumbersUnique(final Path entries_path) {

        try {
            final Set<String> seen = new HashSet<>();

            Files.readAllLines(entries_path).stream().
                map(RelayRaceDataProcessorImpl::getBibNumber).
                filter(bib_number -> !seen.add(bib_number)).
                forEach(bib_number -> {throw new RuntimeException(STR."duplicate bib number '\{bib_number}' in file '\{entries_path.getFileName()}'");});

        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }

    protected int getNumberOfEntryColumns() {
        return (int)race.getConfig().get(KEY_NUMBER_OF_LEGS) + 3;
    }
    private void validateEntriesNumberOfElements(final Path entries_path) {

        final String entry_column_map_string = (String) race.getConfig().get(KEY_ENTRY_COLUMN_MAP);
        final int number_of_columns = entry_column_map_string == null ? getNumberOfEntryColumns() : entry_column_map_string.split("[,\\-]").length;

        try {
            final AtomicInteger counter = new AtomicInteger(0);

            Files.readAllLines(entries_path).stream().
                map(SingleRaceInput::stripEntryComment).
                filter(Predicate.not(String::isBlank)).
                forEach(line -> {
                    counter.incrementAndGet();
                    if (line.split("\t").length != number_of_columns)
                        throw new RuntimeException(STR."invalid entry '\{line}' at line \{counter.get()} in file '\{entries_path.getFileName()}'");
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."unexpected invalid file: '\{entries_path}'");
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

    private int number_of_raw_results;
    int getNumberOfRawResults() {
        return number_of_raw_results;
    }
    public List<RawResult> loadRawResults(final Path raw_results_path) throws IOException {

        // Need to copy into a mutable list.
        final List<RawResult> raw_results = new ArrayList<>(loadRawResults2(raw_results_path));
        number_of_raw_results = raw_results.size();

        if (paper_results_path != null)
            raw_results.addAll(loadRawResults2(paper_results_path));

        loadTimeAnnotations(raw_results);
        return raw_results;
    }

    void loadTimeAnnotations(final List<? extends RawResult> raw_results) throws IOException {

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

    protected List<RawResult> loadRawResults2(final Path raw_results_path) throws IOException {

        return Files.readAllLines(raw_results_path).stream().
            map(SingleRaceInput::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(this::makeRawResult).
            toList();
    }

    protected RawResult makeRawResult(final String line) {

        RawResult result = new RawResult(line);

        final String[] elements = line.split("\t");
        if (elements.length > 2) {
            int leg_number = Integer.parseInt(elements[2]);
            explicitly_recorded_leg_numbers.put(result, leg_number);
        }

        return result;
    }

    private static String getBibNumber(final String line){
        return line.split("\t")[0];
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
