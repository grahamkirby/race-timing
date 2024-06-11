package relay_race;

import common.Category;
import common.RaceResult;

import java.util.ArrayList;
import java.util.List;

public class RelayRacePrizes {

    final RelayRace race;

    public RelayRacePrizes(final RelayRace race) {

        this.race = race;
    }

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
                    List<RaceResult> result1 = new ArrayList<>();
                    result1.add(result);
                    race.prize_winners.put(category, result1);
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

    private boolean prizeWinner(final RaceResult result, final Category category) {

        return !((RelayRaceResult)result).dnf() && race.categories.includes(category, ((RelayRaceResult)result).entry.team.category) && !alreadyWonPrize(((RelayRaceResult)result).entry.team);
    }

    private boolean alreadyWonPrize(final Team team) {

        for (final List<RaceResult> winners : race.prize_winners.values())

            for (final RaceResult result : winners)
                if (((RelayRaceResult)result).entry.team.equals(team))
                    return true;

        return false;
    }
}
