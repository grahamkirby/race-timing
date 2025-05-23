/*
 * Copyright 2025 Graham Kirby:
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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Race.COMMENT_SYMBOL;

public abstract class TimedRaceInput extends SingleRaceInput {

    protected TimedRaceInput(final Race race) {
        super(race);
    }

    @Override
    public void validateInputFiles() {

        super.validateInputFiles();

        checkConfig();
        checkResultsContainValidBibNumbers();
    }

    protected abstract void checkConfig();

    @Override
    protected int getNumberOfEntryColumns() {
        return 4;
    }

    List<TimedRaceEntry> loadEntries() throws IOException {

        if (entries_path == null) return new ArrayList<>();

        final List<TimedRaceEntry> entries = Files.readAllLines(race.getPath(entries_path)).stream().
            filter(Predicate.not(String::isBlank)).
            filter(s -> !s.startsWith(COMMENT_SYMBOL)).
            map(line -> (TimedRaceEntry)makeRaceEntry(Arrays.stream(line.split("\t")).toList())).
            toList();

        assertNoDuplicateEntries(entries);

        return entries;
    }

    private void assertNoDuplicateEntries(final Iterable<? extends SingleRaceEntry> entries) {

        for (final SingleRaceEntry entry1 : entries)
            for (final SingleRaceEntry entry2 : entries)
                if (entry1 != entry2 && entry1.equals(entry2))
                    throw new RuntimeException(STR."duplicate entry '\{entry1}' in file '\{Paths.get(entries_path).getFileName()}'");
    }
}
