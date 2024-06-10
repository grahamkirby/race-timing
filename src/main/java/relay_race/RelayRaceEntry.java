package relay_race;

import common.Category;
import common.Race;

import java.util.Arrays;

public class RelayRaceEntry {

    public int bib_number;
    public Team team;

    public RelayRaceEntry(final String[] elements, Race race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        bib_number = Integer.parseInt(elements[0]);
        try {
            String name = elements[1];
            Category category = race.lookupCategory(elements[2]);
            String[] runners = Arrays.copyOfRange(elements, 3, elements.length);

            team = new Team(name, category, runners);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for team: " + bib_number);
        }
    }
}
