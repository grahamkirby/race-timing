package individual_race;

import common.Category;
import common.Race;
import common.RaceEntry;
import common.Runner;

import java.util.HashMap;
import java.util.Map;

public class IndividualRaceEntry extends RaceEntry {

    private static final Map<String, String> NORMALISED_CLUB_NAMES = new HashMap<>();

    static {
        NORMALISED_CLUB_NAMES.put("", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Unattached", "Unatt.");
        NORMALISED_CLUB_NAMES.put("U/A", "Unatt.");
        NORMALISED_CLUB_NAMES.put("None", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Fife Athletic Club", "Fife AC");
        NORMALISED_CLUB_NAMES.put("Dundee HH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas Running Club", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Haddies", "Anster Haddies");
        NORMALISED_CLUB_NAMES.put("Dundee Hawkhill", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("DRR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Perth RR", "Perth Road Runners");
        NORMALISED_CLUB_NAMES.put("Kinross RR", "Kinross Road Runners");
        NORMALISED_CLUB_NAMES.put("Falkland TR", "Falkland Trail Runners");
        NORMALISED_CLUB_NAMES.put("PH Racing Club", "PH Racing");
        NORMALISED_CLUB_NAMES.put("DHH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Carnegie H", "Carnegie Harriers");
        NORMALISED_CLUB_NAMES.put("Dundee RR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Recreational Running", "Recreational Runners");
    }

    public final Runner runner;

    public IndividualRaceEntry(final String[] elements, final Race race) {

        // Expected format: "1" "John Smith"	"Fife AC"	"MS"

        try {

            if (elements.length != 4)
                throw new RuntimeException("illegal composition for runner: " + elements[0]);

            bib_number = Integer.parseInt(elements[0]);

            final String name = cleanName(elements[1]);
            final String club = normaliseClubName(cleanName(elements[2]));
            final Category category = race.lookupCategory(elements[3]);

            runner = new Runner(name, club, category);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for runner: " + bib_number);
        }
    }

    private String cleanName(String name) {

        while (name.contains("  ")) name = name.replaceAll(" {2}", " ");
        return name.strip();
    }

    private static String normaliseClubName(final String club) {

        return NORMALISED_CLUB_NAMES.getOrDefault(club, club);
    }

    @Override
    public String toString() {
        return runner.name + ", " + runner.club;
    }
}
