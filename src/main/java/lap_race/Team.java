package lap_race;

import java.util.Arrays;

public class Team {

    String name;
    int bib_number;
    Category category;
    String[] runners;

    public Team(String[] elements) {

        bib_number = Integer.parseInt(elements[0]);
        name = elements[1];
        category = Category.parse(elements[2]);
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

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }
}
