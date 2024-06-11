package common;

public abstract class RaceResult implements Comparable<RaceResult> {

    protected final Race race;
    public String position_string;

    protected RaceResult(Race race) {
        this.race = race;
    }

    public int comparePerformanceTo(RaceResult other) {
        throw new UnsupportedOperationException();
    }
}
