package lap_race;

public enum Category {

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

    Category(String category_name, String composition, int minimum_age, int number_of_prizes) {

        this.category_name = category_name;
        this.composition = composition;
        this.minimum_age = minimum_age;
        this.number_of_prizes = number_of_prizes;
    }

    public boolean includes(Category other_category) {

        return compositionIncludes(other_category.composition) && minimum_age <= other_category.minimum_age;
    }

    private boolean compositionIncludes(String other_composition) {

        return composition.equals(other_composition) ||
                composition.equals("Open") && other_composition.equals("Women") ||
                composition.equals("Open") && other_composition.equals("Mixed");
    }

    public static Category parse(final String s) {

        for (Category category : values()) {
            if (category.category_name.equals(s)) return category;
        }
        throw new RuntimeException("undefined category: " + s);
    }

    public String toString() {
        return category_name;
    }
}
