package common;

import common.categories.Category;

public class Runner {

    public final String name;
    public String club;
    public final Category category;

    public Runner(final String name, final String club, final Category category) {
        this.name = name;
        this.club = club;
        this.category = category;
    }

    @Override
    public boolean equals(final Object other) {

        return other instanceof Runner other_runner &&
                name.equals(other_runner.name) &&
                club.equals(other_runner.club);
    }
}
