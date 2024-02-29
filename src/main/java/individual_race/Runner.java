package individual_race;

import common.Category;
import lap_race.LapRaceCategory;

import java.time.Duration;
import java.util.Arrays;

public record Runner(String name, String club, Category category) {

    @Override
    public boolean equals(Object other) {

        return other instanceof Runner && name.equals(((Runner) other).name) && club.equals(((Runner) other).club);
    }
}
