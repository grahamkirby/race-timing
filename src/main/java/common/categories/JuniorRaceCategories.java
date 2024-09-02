package common.categories;

public class JuniorRaceCategories extends Categories {

    public JuniorRaceCategories(final int number_of_category_prizes) {

        runner_categories.add(new Category("Female Under 9", "FU9", "Female", 7, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 9", "MU9", "Male", 7, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 11", "FU11", "Female", 9, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 11", "MU11", "Male", 9, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 13", "FU13", "Female", 11, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 13", "MU13", "Male", 11, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 15", "FU15", "Female", 13, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 15", "MU15", "Male", 13, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 18", "FU18", "Female", 15, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 18", "MU18", "Male", 15, number_of_category_prizes));

        prize_categories_in_decreasing_generality_order.addAll(runner_categories);
        prize_categories_in_report_order.addAll(runner_categories);
    }

    public boolean includes(final Category first_category, final Category second_category) {

        // No category includes another.
        return first_category.equals(second_category);
    }
}
