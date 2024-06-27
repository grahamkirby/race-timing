package common;

import common.categories.Category;

import java.util.ArrayList;
import java.util.List;

public class RacePrizes {

    protected Race race;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RacePrizes(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void allocatePrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            race.prize_winners.put(category, getPrizeWinners(category));
    }

    protected boolean notYetWonPrize(final RaceResult potential_winner) {

        for (final List<RaceResult> winners : race.prize_winners.values())
            for (final RaceResult result : winners)
                if (result.sameEntrant(potential_winner))
                    return false;

        return true;
    }

    protected boolean prizeWinner(final RaceResult result, final Category category) {

        return result.completed() && race.categories.includes(category, result.getCategory()) && notYetWonPrize(result);
    }

    private List<RaceResult> getPrizeWinners(final Category category) {

        final List<RaceResult> prize_winners = new ArrayList<>();

        int position = 1;

        for (final RaceResult result : race.getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result);
                position++;
            }
        }
        return prize_winners;
    }
}
