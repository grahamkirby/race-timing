package individual_race;

import common.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndividualRaceCategories {

    // Defines the order of iteration when allocating prizes.
    public static List<Category> getCategoriesInDecreasingGeneralityOrder(boolean open_category, int open_prizes, int category_prizes) {

        List<Category> categories = new ArrayList<>();

        if (open_category) {
            categories.add(new Category("Women Open", "WO", "Women", 0, open_prizes));
            categories.add(new Category("Men Open", "MO", "Men", 0, open_prizes));
        }

        categories.add(new Category("Women Senior", "FS", "Women", 20, category_prizes));
        categories.add(new Category("Men Senior", "MS", "Men", 20, category_prizes));
        categories.add(new Category("Women Junior", "FU20", "Women", 0, category_prizes));
        categories.add(new Category("Men Junior", "MU20", "Men", 0, category_prizes));

        for (int age_group = 40; age_group <= 60; age_group+= 10) {
            categories.add(new Category("Women " + age_group + "-" + (age_group+9), "F" + age_group,"Women", age_group, category_prizes));
            categories.add(new Category("Men " + age_group + "-" + (age_group+9), "M" + age_group,"Men", age_group, category_prizes));
        }

        categories.add(new Category("Women 70+", "F70+","Women", 70, category_prizes));
        categories.add(new Category("Men 70+", "M70+","Men", 70, category_prizes));

        return categories;
    }

    public static List<Category> getCategoriesInReportOrder(boolean open_category, int open_prizes, int category_prizes) {
        return getCategoriesInDecreasingGeneralityOrder(open_category, open_prizes, category_prizes);
    }

    public static boolean includes(final Category first_category, final Category second_category) {

        // Only the open categories include any other categories.
        return first_category.equals(second_category) ||
                (first_category.getLongName().equals("Women Open") && second_category.getGender().equals("Women")) ||
                (first_category.getLongName().equals("Men Open") && second_category.getGender().equals("Men"));
    }
}
