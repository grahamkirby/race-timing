package common;

import common.categories.Category;

public class Runner {

    public String name;
    public String club;
    public Category category;

    public Runner(String name, String club, Category category) {
        this.name = name;
        this.club = club;
        this.category = category;
    }

    @Override
    public boolean equals(Object other) {

        return other instanceof Runner other_runner &&
                name.equals(other_runner.name) &&
                club.equals(other_runner.club);
    }
}
