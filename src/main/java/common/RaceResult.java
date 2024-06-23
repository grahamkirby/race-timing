package common;

import common.categories.Category;

public abstract class RaceResult implements Comparable<RaceResult> {

    public final Race race;
    public String position_string;

    protected RaceResult(Race race) {
        this.race = race;
    }

    public int comparePerformanceTo(final RaceResult other) {
        throw new UnsupportedOperationException();
    }

    public abstract boolean sameEntrant(final RaceResult other);

    public abstract boolean completed();
    public abstract Category getCategory();
}
