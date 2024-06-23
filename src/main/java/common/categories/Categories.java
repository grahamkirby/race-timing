package common.categories;

import java.util.ArrayList;
import java.util.List;

public abstract class Categories {

    protected List<Category> categories_in_decreasing_generality_order = new ArrayList<>();
    protected List<Category> categories_in_report_order = new ArrayList<>();

    // Defines the order of iteration when allocating prizes.
    public List<Category> getCategoriesInDecreasingGeneralityOrder() {

        return categories_in_decreasing_generality_order;
    }

    public List<Category> getCategoriesInReportOrder() {

        return categories_in_report_order;
    }

    public Category getCategory(final String category_short_name) {

        for (final Category category : categories_in_decreasing_generality_order)
            if (category.getShortName().equals(category_short_name)) return category;

        throw new RuntimeException("unknown category: " + category_short_name);
    }

    public abstract boolean includes(Category first_category, Category second_category);
}
