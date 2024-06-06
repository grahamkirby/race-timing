package common;

public abstract class RaceResult implements Comparable<RaceResult> {

    protected final Race race;
    public String position_string;

    protected RaceResult(Race race) {
        this.race = race;
    }

    public abstract int compareTo2(RaceResult other);
}
