package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.time.Duration;
import java.util.Comparator;

public class UntimedIndividualRaceResult extends SingleRaceResult {

    public UntimedIndividualRaceResult(UntimedIndividualRace race, UntimedIndividualRaceEntry entry, Duration finish_time) {
        super(race, entry, finish_time);
    }

    protected String getIndividualRunnerClub() {
        return ((Runner)entry.participant).club;
    }

    @Override
    protected String getIndividualRunnerName() {
        return entry.participant.name;
    }

    @Override
    public Participant getParticipant() {
        return entry.participant;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((UntimedIndividualRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public CompletionStatus getCompletionStatus() {
        return CompletionStatus.COMPLETED;
    }

    @Override
    public boolean shouldDisplayPosition() {
        return true;
    }

    @Override
    public EntryCategory getCategory() {
        return getParticipant().category;
    }

    @Override
    public Duration duration() {
        return finish_time;
    }
}
