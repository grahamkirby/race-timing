package relay_race;

import common.Categories;
import common.Category;

import java.util.Arrays;

public class RelayRaceCategories extends Categories {

    public RelayRaceCategories(final int senior_prizes, final int category_prizes) {

        final Category OS = new Category("Open Senior", "OS", "Open", 15, senior_prizes);
        final Category MS = new Category("Mixed Senior", "MS", "Mixed", 15, category_prizes);
        final Category WS = new Category("Women Senior", "WS", "Women", 15, senior_prizes);
        final Category O40 = new Category("Open 40+", "O40", "Open", 40, category_prizes);
        final Category M40 = new Category("Mixed 40+", "M40", "Mixed", 40, category_prizes);
        final Category W40 = new Category("Women 40+", "W40", "Women", 40, category_prizes);
        final Category O50 = new Category("Open 50+", "O50", "Open", 50, category_prizes);
        final Category W50 = new Category("Women 50+", "W50", "Women", 50, category_prizes);
        final Category O60 = new Category("Open 60+", "O60", "Open", 60, category_prizes);
        final Category W60 = new Category("Women 60+", "W60", "Women", 60, category_prizes);

        categories_in_decreasing_generality_order.addAll(Arrays.asList(OS, MS, WS, O40, M40, W40, O50, W50, O60, W60));
        categories_in_report_order.addAll(Arrays.asList(WS, OS, W40, O40, W50, O50, W60, O60, MS, M40));
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
