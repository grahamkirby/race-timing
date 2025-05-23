package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Runner;

import java.time.Duration;

class TimedIndividualRaceResult extends TimedRaceResult {

    TimedIndividualRaceResult(final TimedRace race, final TimedIndividualRaceEntry entry, final Duration finish_time) {
        super(race, entry, finish_time);
    }

    @Override
    public Participant getParticipant() {
        return entry.participant;
    }

    protected String getIndividualRunnerClub() {
        return ((Runner) entry.participant).club;
    }
}
