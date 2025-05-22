package org.grahamkirby.race_timing.single_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;

import java.time.Duration;

public abstract class SingleRaceResult extends RaceResult {

    public SingleRaceEntry entry;
    protected Duration finish_time;

    protected SingleRaceResult(Race race, SingleRaceEntry entry, Duration finish_time) {
        super(race);
        this.entry = entry;
        this.finish_time = finish_time;
    }

    public abstract Duration duration();
}
