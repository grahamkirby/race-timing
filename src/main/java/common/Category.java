package common;

public final class Category {

    private final String long_name;
    private final String short_name;
    private final String gender;

    private final int minimum_age;
    private final int number_of_prizes;

    public Category(final String long_name, final String short_name, final String gender, final int minimum_age, final int number_of_prizes) {

        this.long_name = long_name;
        this.short_name = short_name;
        this.gender = gender;
        this.minimum_age = minimum_age;
        this.number_of_prizes = number_of_prizes;
    }

    public String getLongName() {
        return long_name;
    }

    public String getShortName() {
        return short_name;
    }

    public String getGender() {
        return gender;
    }

    public int getMinimumAge() {
        return minimum_age;
    }

    public int numberOfPrizes() {
        return number_of_prizes;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Category other && short_name.equals(other.short_name);
    }

    @Override
    public int hashCode() {
        return short_name.hashCode();
    }
}
