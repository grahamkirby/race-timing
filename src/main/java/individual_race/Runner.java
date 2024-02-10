package individual_race;

import common.Category;
import lap_race.LapRaceCategory;

import java.util.Arrays;

public class Runner {

    final int bib_number;
    final String name;
    final Category category;
    final String club;

    public Runner(final String[] elements) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        bib_number = Integer.parseInt(elements[0]);
        name = elements[1];
        try {
            category = LapRaceCategory.parse(elements[2]);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for runner: " + bib_number);
        }
        club = elements[3];
    }
}
