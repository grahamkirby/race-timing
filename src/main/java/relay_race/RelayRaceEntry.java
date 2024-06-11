package relay_race;

import common.Category;

import java.util.Arrays;

public class RelayRaceEntry {

    public int bib_number;
    public Team team;

    public RelayRaceEntry(final String[] elements, RelayRace race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        if (elements.length != race.number_of_legs + 3)
            throw new RuntimeException("illegal composition for team: " + elements[0]);

        bib_number = Integer.parseInt(elements[0]);
        try {
            final String name = elements[1];
            final Category category = race.lookupCategory(elements[2]);
            final String[] runners = Arrays.copyOfRange(elements, 3, elements.length);

            team = new Team(name, category, runners);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for team: " + bib_number);
        }
    }
}
