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
import java.util.function.Predicate;

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

    public void completeConfiguration() {

    }

    private Path paper_results_path, annotations_path;
    private int number_of_raw_results;
    Map<RawResult, Integer> explicitly_recorded_leg_numbers;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceData getRaceData() {

        Path raw_results_path = (Path) race.getConfig().get(KEY_RAW_RESULTS_PATH);
        Path entries_path = (Path) race.getConfig().get(KEY_ENTRIES_PATH);

        try {
            return new RelayRaceDataImpl(loadRawResults(raw_results_path), loadEntries(entries_path), explicitly_recorded_leg_numbers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RawResult> loadRawResults(final Path raw_results_path) throws IOException {

        // Need to copy into a mutable list.
        final List<RawResult> raw_results = new ArrayList<>(loadRawResults2(raw_results_path));
        number_of_raw_results = raw_results.size();

        if (paper_results_path != null)
            raw_results.addAll(loadRawResults(paper_results_path));

        return raw_results;
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

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }
}
