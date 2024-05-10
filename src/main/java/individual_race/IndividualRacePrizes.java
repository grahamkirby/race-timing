package individual_race;

import common.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndividualRacePrizes {

    final IndividualRace race;

    public IndividualRacePrizes(final IndividualRace race) {
        this.race = race;
    }

    public void allocatePrizes() {

//        List<Category> categories = IndividualRaceCategories.getCategoriesInDecreasingGeneralityOrder(race.open_category, race.open_prizes, race.category_prizes);

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder()) {
            List<IndividualRaceEntry> prizeWinners = getPrizeWinners(category);
            race.prize_winners.put(category, prizeWinners);
        }
    }

    private List<IndividualRaceEntry> getPrizeWinners(final Category category) {

        final List<IndividualRaceEntry> prize_winners = new ArrayList<>();

        int position = 1;

        for (final IndividualRaceResult result : race.getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result.entry);
                position++;
            }
        }
        return prize_winners;
    }

    private boolean prizeWinner(final IndividualRaceResult result, final Category category) {

        return !result.dnf() && race.categories.includes(category, result.entry.runner.category) && !alreadyWonPrize(result.entry);
    }

    private boolean alreadyWonPrize(final IndividualRaceEntry entry) {

        for (List<IndividualRaceEntry> winners : race.prize_winners.values())
            if (winners.contains(entry)) return true;

        return false;
    }
}
