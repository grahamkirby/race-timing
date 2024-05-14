package individual_race;

import common.Categories;
import common.Category;

import java.util.ArrayList;
import java.util.List;

public class SeniorRaceCategories implements Categories {

    List<Category> categories_in_decreasing_generality_order = new ArrayList<>();

    public SeniorRaceCategories(boolean open_category, int open_prizes, int category_prizes) {

        if (open_category) {
            categories_in_decreasing_generality_order.add(new Category("Women Open", "WO", "Women", 0, open_prizes));
            categories_in_decreasing_generality_order.add(new Category("Men Open", "MO", "Men", 0, open_prizes));
        }

        categories_in_decreasing_generality_order.add(new Category("Women Senior", "FS", "Women", 20, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Men Senior", "MS", "Men", 20, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Women Junior", "FU20", "Women", 0, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Men Junior", "MU20", "Men", 0, category_prizes));

        for (int age_group = 40; age_group <= 60; age_group+= 10) {
            categories_in_decreasing_generality_order.add(new Category("Women " + age_group + "-" + (age_group+9), "F" + age_group,"Women", age_group, category_prizes));
            categories_in_decreasing_generality_order.add(new Category("Men " + age_group + "-" + (age_group+9), "M" + age_group,"Men", age_group, category_prizes));
        }

        categories_in_decreasing_generality_order.add(new Category("Women 70+", "F70+","Women", 70, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Men 70+", "M70+","Men", 70, category_prizes));

    }

    // Defines the order of iteration when allocating prizes.
    @Override
    public List<Category> getCategoriesInDecreasingGeneralityOrder() {
        return categories_in_decreasing_generality_order;
    }

    @Override
    public List<Category> getCategoriesInReportOrder() {
        return categories_in_decreasing_generality_order;
    }

    @Override
    public boolean includes(final Category first_category, final Category second_category) {

        // Only the open categories include any other categories.
        return first_category.equals(second_category) ||
                (first_category.getLongName().equals("Women Open") && second_category.getGender().equals("Women")) ||
                (first_category.getLongName().equals("Men Open") && second_category.getGender().equals("Men"));
    }
}
