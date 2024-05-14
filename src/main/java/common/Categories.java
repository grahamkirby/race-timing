package common;

import java.util.List;

public interface Categories {

    // Defines the order of iteration when allocating prizes.
    List<Category> getCategoriesInDecreasingGeneralityOrder();

    List<Category> getCategoriesInReportOrder();

    Category getCategory(String category_short_name);

    boolean includes(Category first_category, Category second_category);
}
