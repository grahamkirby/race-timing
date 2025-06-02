package org.grahamkirby.race_timing.single_race;

import org.grahamkirby.race_timing.common.Participant;

import java.util.Objects;

public abstract class SingleRaceEntry {

    public Participant participant;
    public int bib_number;

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final SingleRaceEntry other_entry &&
            participant.equals(other_entry.participant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participant);
    }
}
