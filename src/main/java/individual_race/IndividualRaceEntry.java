package individual_race;

import common.Category;
import common.Race;

public class IndividualRaceEntry {

    int bib_number = 0;
    public final Runner runner;

    public IndividualRaceEntry(final String[] elements, Race race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"
        // Expected format: "1" "John Smith"	"Fife AC"	"MS"

        String name, club;
        Category category;

        try {
            bib_number = Integer.parseInt(elements[0]);
            name = elements[1];
            club = elements[2];
            category = race.lookupCategory(elements[3]);

            runner = new Runner(name, club, category);
        }
        catch (NumberFormatException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for runner: " + bib_number);
        }
    }
}
