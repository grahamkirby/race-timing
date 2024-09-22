package common;

import common.categories.Category;

public abstract class RaceResult implements Comparable<RaceResult> {

    public final Race race;
    public String position_string;

    protected RaceResult(Race race) {
        this.race = race;
    }

    public int compareCompletionTo(final RaceResult o) {

        if (completed() && !o.completed()) return -1;
        if (!completed() && o.completed()) return 1;
        return 0;
    }

    protected static String getFirstName(final String name) {
        return name.split(" ")[0];
    }

    protected static String getLastName(final String name) {

        final String[] names = name.split(" ");
        return names[names.length - 1];
    }

    public int comparePerformanceTo(final RaceResult other) {
        throw new UnsupportedOperationException();
    }

    public abstract boolean sameEntrant(final RaceResult other);

    public abstract boolean completed();
    public abstract Category getCategory();
}
