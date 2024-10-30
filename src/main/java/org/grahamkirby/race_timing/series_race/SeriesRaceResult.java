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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;

public abstract class SeriesRaceResult extends RaceResult {

    // TODO rationalise with IndividualRaceResult - consistency wrt entry class
    public final Runner runner;

    public SeriesRaceResult(final Runner runner, final SeriesRace race) {

        super(race);
        this.runner = runner;
    }

    @Override
    public boolean completed() {

        return numberCompleted() >= ((SeriesRace)race).minimum_number_of_races;
    }

    @Override
    public EntryCategory getCategory() {
        return runner.category;
    }

    @Override
    public boolean sameEntrant(final RaceResult other) {
        return runner.equals(((SeriesRaceResult) other).runner);
    }

    protected int numberCompleted() {

        int count = 0;

        for (final IndividualRace individual_race : ((SeriesRace)race).races) {
            if (individual_race != null)
                for (final RaceResult result : individual_race.getOverallResults())
                    if (((IndividualRaceResult)result).entry.runner.equals(runner)) count++;
        }

        return count;
    }

    protected int compareRunnerNameTo(final SeriesRaceResult o) {

        final int last_name_comparison = race.normalisation.getLastName(runner.name).compareTo(race.normalisation.getLastName(o.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : race.normalisation.getFirstName(runner.name).compareTo(race.normalisation.getFirstName(o.runner.name));
    }

    public boolean shouldDisplayPosition() {

        final SeriesRace series_race = (SeriesRace) race;
        final int number_of_races_taken_place = series_race.getNumberOfRacesTakenPlace();

        return number_of_races_taken_place < series_race.getRaces().size() || completed();
    }
}
