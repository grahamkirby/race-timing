package individual_race;

import common.Category;

import java.util.Arrays;
import java.util.List;

public enum IndividualRaceCategory implements Category {

    // Declared in order of decreasing generality.
    // The order is significant in that it defines the order of iteration when allocating prizes.

    OPEN_SENIOR("Open Senior", "OS", "Open", 15, 3),
    MEN_SENIOR("Men Senior", "MS", "Men", 20, 3),
    WOMEN_SENIOR("Women Senior", "FS", "Women", 20, 3),
    MEN_UNDER_20("Men Junior", "MU20", "Men", 15, 1),
    WOMEN_UNDER_20("Women Junior", "FU20", "Women", 15, 1),
    MEN_40("Men 40-49", "M40","Men", 40, 1),
    WOMEN_40("Women 40-49", "F40","Women", 40, 1),
    MEN_50("Men 50-59", "M50","Men", 50, 1),
    WOMEN_50("Women 50-59", "F50","Women", 50, 1),
    MEN_60("Men 60-69", "M60","Men", 60, 1),
    WOMEN_60("Women 60-69", "F60","Women", 60, 1),
    MEN_70("Men 70+", "M70","Men", 70, 1),
    WOMEN_70("Women 70+", "F70","Women", 70, 1);

    private final String category_long_name;
    private final String category_short_name;
    private final String gender;
    private final int minimum_age;
    public final int number_of_prizes;

    IndividualRaceCategory(final String category_long_name, final String category_short_name, final String gender, final int minimum_age, final int number_of_prizes) {

        this.category_long_name = category_long_name;
        this.category_short_name = category_short_name;
        this.gender = gender;
        this.minimum_age = minimum_age;
        this.number_of_prizes = number_of_prizes;
    }

    @Override
    public boolean includes(final Category other_category) {

        // Only the open category includes any other categories.
        return this == other_category || this == OPEN_SENIOR;
    }

    public static final List<Category> getCategoriesInReportOrder() {
        return Arrays.asList(
                OPEN_SENIOR,
                WOMEN_SENIOR,
                MEN_SENIOR,
                WOMEN_UNDER_20,
                MEN_UNDER_20,
                WOMEN_40,
                MEN_40,
                WOMEN_50,
                MEN_50,
                WOMEN_60,
                MEN_60,
                WOMEN_70,
                MEN_70);
    }

    public static Category parse(final String s) {

        for (Category category : values())
            if (category.shortName().equals(s)) return category;

        throw new RuntimeException("undefined category: " + s);
    }

    @Override
    public String shortName() {
        return category_short_name;
    }

    @Override
    public String longName() {
        return category_long_name;
    }

    @Override
    public int numberOfPrizes() {
        return number_of_prizes;
    }
}
