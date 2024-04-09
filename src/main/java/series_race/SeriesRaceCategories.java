package series_race;

import common.Category;
import individual_race.IndividualRaceCategories;

import java.util.List;

public class SeriesRaceCategories {

    public static List<Category> getCategoriesInDecreasingGeneralityOrder(boolean open_category, int open_prizes, int category_prizes) {
        return IndividualRaceCategories.getCategoriesInDecreasingGeneralityOrder(open_category, open_prizes, category_prizes);
    }

    public static List<Category> getCategoriesInReportOrder(boolean open_category, int open_prizes, int category_prizes) {
        return IndividualRaceCategories.getCategoriesInReportOrder(open_category, open_prizes, category_prizes);
    }

    public static boolean includes(final Category first_category, final Category second_category) {

        // Only the open categories include any other categories.
        return first_category.equals(second_category) ||
                (first_category.getLongName().equals("Women Open") && second_category.getGender().equals("Women")) ||
                (first_category.getLongName().equals("Men Open") && second_category.getGender().equals("Men"));
    }
}
