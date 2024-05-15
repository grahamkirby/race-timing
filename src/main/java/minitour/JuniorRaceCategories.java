package minitour;

import common.Categories;
import common.Category;

import java.util.ArrayList;
import java.util.List;

public class JuniorRaceCategories extends Categories {

    public JuniorRaceCategories(int category_prizes) {

        categories_in_decreasing_generality_order.add(new Category("Female Under 9", "FU9", "Female", 7, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Male Under 9", "MU9", "Male", 7, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Female Under 11", "FU11", "Female", 9, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Male Under 11", "MU11", "Male", 9, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Female Under 13", "FU13", "Female", 11, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Male Under 13", "MU13", "Male", 11, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Female Under 15", "FU15", "Female", 13, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Male Under 15", "MU15", "Male", 13, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Female Under 18", "FU18", "Female", 15, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Male Under 18", "MU18", "Male", 15, category_prizes));

        categories_in_report_order.addAll(categories_in_decreasing_generality_order);
    }

    public boolean includes(final Category first_category, final Category second_category) {

        // Only the open categories include any other categories.
        return first_category.equals(second_category);
    }
}
