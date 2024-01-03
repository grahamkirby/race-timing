package lap_race;

import java.util.Arrays;

public class Team {

    final int bib_number;
    final String name;
    final Category category;
    final String[] runners;

    public Team(String[] elements) {

        // Expected format: "1", "Anster Haddies Ladies B", "Women Senior", "Tracy Knox", "Pamela Cruickshanks & Debz Hay", "Lynne Herd & Jacs McDonald", "Rosie Knox"

        bib_number = Integer.parseInt(elements[0]);
        name = elements[1];
        try {
            category = Category.parse(elements[2]);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for team: " + bib_number);
        }
        runners = Arrays.copyOfRange(elements, 3, elements.length);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        for (String runner : runners)  {
            if (!builder.isEmpty()) builder.append("\t");
            builder.append(runner);
        }
        return name + ", " + category + ": " + builder;
    }
}
