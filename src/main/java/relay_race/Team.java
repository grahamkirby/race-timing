package relay_race;

import common.Category;
import common.Race;

import java.util.Arrays;

public class Team {

//    final int bib_number;
    final String name;
    final Category category;
    final String[] runners;

    public Team(String name, Category category, String[] runners) {
        this.name = name;
        this.category = category;
        this.runners = runners;
    }

    public Team(final String[] elements, Race race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

//        bib_number = Integer.parseInt(elements[0]);
        name = elements[1];
        try {
            category = race.lookupCategory(elements[2]);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for team: " );
        }
        runners = Arrays.copyOfRange(elements, 3, elements.length);
    }
}
