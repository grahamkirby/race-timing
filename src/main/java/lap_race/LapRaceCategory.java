package lap_race;

public enum LapRaceCategory implements Category {

    // Declared in order of decreasing generality.
    // The order is significant in that it defines the order of iteration when allocating prizes.

    OPEN_SENIOR("Open Senior", "Open", 15, 3),
    MIXED_SENIOR("Mixed Senior", "Mixed", 15, 1),
    FEMALE_SENIOR("Women Senior", "Women", 15, 3),
    OPEN_40("Open 40+", "Open", 40, 1),
    MIXED_40("Mixed 40+", "Mixed", 40, 1),
    FEMALE_40("Women 40+", "Women", 40, 1),
    OPEN_50("Open 50+", "Open", 50,1),
    FEMALE_50("Women 50+", "Women", 50, 1),
    OPEN_60("Open 60+", "Open", 60,1),
    FEMALE_60("Women 60+", "Women", 60, 1);

    private final String category_name;
    private final String composition;
    private final int minimum_age;
    public final int number_of_prizes;

    LapRaceCategory(final String category_name, final String composition, final int minimum_age, final int number_of_prizes) {

        this.category_name = category_name;
        this.composition = composition;
        this.minimum_age = minimum_age;
        this.number_of_prizes = number_of_prizes;
    }

    @Override
    public boolean includes(final Category other_category) {

        LapRaceCategory other = (LapRaceCategory) other_category;
        return compositionIncludes(other.composition) && minimum_age <= other.minimum_age;
    }

    private boolean compositionIncludes(final String other_composition) {

        return composition.equals(other_composition) ||
                composition.equals("Open") && other_composition.equals("Women") ||
                composition.equals("Open") && other_composition.equals("Mixed");
    }

    public static Category parse(final String s) {

        for (Category category : values())
            if (category.shortName().equals(s)) return category;

        throw new RuntimeException("undefined category: " + s);
    }

    @Override
    public String shortName() {
        return category_name;
    }

    @Override
    public String longName() {
        return category_name;
    }

    @Override
    public int numberOfPrizes() {
        return number_of_prizes;
    }
}
