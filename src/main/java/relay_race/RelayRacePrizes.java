package relay_race;

import common.Race;
import common.RacePrizes;
import common.RaceResult;
import common.categories.Category;

import java.util.ArrayList;
import java.util.List;

public class RelayRacePrizes extends RacePrizes {

    public RelayRacePrizes(final Race race) {

        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void allocatePrizes() {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.
        allocateFirstPrizes();

        // Now consider other prizes (only available in senior categories).
        allocateMinorPrizes();
    }

    private void allocateFirstPrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder()) {

            for (final RaceResult result : race.getOverallResults()) {

                if (prizeWinner(result, category)) {
                    race.prize_winners.put(category, new ArrayList<>(List.of(result)));
                    break;
                }
            }
        }
    }

    private void allocateMinorPrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            allocateMinorPrizes(category);
    }

    private void allocateMinorPrizes(final Category category) {

        int position = 2;

        for (final RaceResult result : race.getOverallResults()) {

            if (position > category.numberOfPrizes()) return;

            if (prizeWinner(result, category)) {
                race.prize_winners.get(category).add(result);
                position++;
            }
        }
    }
}
