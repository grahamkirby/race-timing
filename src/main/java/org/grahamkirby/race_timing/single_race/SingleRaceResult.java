package org.grahamkirby.race_timing.single_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;

import java.time.Duration;

public abstract class SingleRaceResult extends RaceResult {

    public SingleRaceEntry entry;
    public Duration finish_time;
    public boolean dnf;

    protected SingleRaceResult(final Race race, final SingleRaceEntry entry, final Duration finish_time) {
        super(race);
        this.entry = entry;
        this.finish_time = finish_time;
    }

    public abstract Duration duration();

    @Override
    public boolean canComplete() {
        return !dnf;
    }
}
