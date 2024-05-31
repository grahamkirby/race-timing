package relay_race;

import common.Categories;
import common.Category;

public class RelayRaceCategories extends Categories {

    public RelayRaceCategories(int senior_prizes, int category_prizes) {

        categories_in_decreasing_generality_order.add(new Category("Open Senior", "OS", "Open", 15, senior_prizes));
        categories_in_decreasing_generality_order.add(new Category("Mixed Senior", "MS", "Mixed", 15, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Women Senior", "WS", "Women", 15, senior_prizes));

        categories_in_decreasing_generality_order.add(new Category("Open 40+", "O40", "Open", 40, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Mixed 40+", "M40", "Mixed", 40, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Women 40+", "W40", "Women", 40, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Open 50+", "O50", "Open", 50,category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Women 50+", "W50", "Women", 50, category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Open 60+", "O60", "Open", 60,category_prizes));
        categories_in_decreasing_generality_order.add(new Category("Women 60+", "W60", "Women", 60, category_prizes));

        categories_in_report_order.add(new Category("Women Senior", "WS", "Women", 15, senior_prizes));
        categories_in_report_order.add(new Category("Open Senior", "OS", "Open", 15, senior_prizes));
        categories_in_report_order.add(new Category("Women 40+", "W40", "Women", 40, category_prizes));
        categories_in_report_order.add(new Category("Open 40+", "O40", "Open", 40, category_prizes));
        categories_in_report_order.add(new Category("Women 50+", "W50", "Women", 50, category_prizes));
        categories_in_report_order.add(new Category("Open 50+", "O50", "Open", 50,category_prizes));
        categories_in_report_order.add(new Category("Women 60+", "W60", "Women", 60, category_prizes));
        categories_in_report_order.add(new Category("Open 60+", "O60", "Open", 60,category_prizes));
        categories_in_report_order.add(new Category("Mixed Senior", "MS", "Mixed", 15, category_prizes));
        categories_in_report_order.add(new Category("Mixed 40+", "M40", "Mixed", 40, category_prizes));
    }

    @Override
    public boolean includes(final Category first_category, final Category second_category) {

        return genderIncludes(first_category.getGender(), second_category.getGender()) && first_category.getMinimumAge() <= second_category.getMinimumAge();
    }

    private static boolean genderIncludes(final String first_gender, final String second_gender) {

        return first_gender.equals(second_gender) ||
                first_gender.equals("Open") && second_gender.equals("Women") ||
                first_gender.equals("Open") && second_gender.equals("Mixed");
    }
}
