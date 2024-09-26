package common.categories;

import java.util.ArrayList;
import java.util.List;

public abstract class Categories {

    protected final List<Category> runner_categories = new ArrayList<>();
    protected final List<Category> prize_categories_in_decreasing_generality_order = new ArrayList<>();
    protected final List<Category> prize_categories_in_report_order = new ArrayList<>();

    // Order not important.
    public List<Category> getRunnerCategories() {
        return runner_categories;
    }

    // Defines the order of iteration when allocating prizes.
    public List<Category> getPrizeCategoriesInDecreasingGeneralityOrder() {
        return prize_categories_in_decreasing_generality_order;
    }

    // Defines the order of iteration when reporting prizes.
    public List<Category> getPrizeCategoriesInReportOrder() {
        return prize_categories_in_report_order;
    }

    public Category getCategory(final String category_short_name) {

        for (final Category category : runner_categories)
            if (category.getShortName().equals(category_short_name)) return category;

        throw new RuntimeException("unknown category: " + category_short_name);
    }

    public abstract boolean includes(Category first_category, Category second_category);
}
