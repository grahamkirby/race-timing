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

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            race.prize_winners.put(category, getPrizeWinners(category));
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

        return !result.DNF && race.categories.includes(category, result.entry.runner.category) && !alreadyWonPrize(result.entry);
    }

    private boolean alreadyWonPrize(final IndividualRaceEntry entry) {

        for (List<IndividualRaceEntry> winners : race.prize_winners.values())
            if (winners.contains(entry)) return true;

        return false;
    }
}
