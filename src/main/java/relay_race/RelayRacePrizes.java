package relay_race;

import common.Category;
import common.RaceResult;

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

            for (final RelayRaceResult result : race.overall_results) {
                if (prizeWinner(result, category)) {
                    race.prize_winners.put(category, new RaceResult[]{result});
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

        for (final RelayRaceResult result : race.overall_results) {

            if (position > category.numberOfPrizes()) return;

            if (prizeWinner(result, category)) {
                race.prize_winners.put(category, appendToArray(race.prize_winners.get(category), result));
                position++;
            }
        }
    }

    private static RaceResult[] appendToArray(final RaceResult[] existing_results, final RaceResult result) {

        final RaceResult[] new_results = new RaceResult[existing_results.length + 1];
        System.arraycopy(existing_results, 0, new_results, 0, existing_results.length);
        new_results[new_results.length - 1] = result;
        return new_results;
    }

    private boolean prizeWinner(final RelayRaceResult result, final Category category) {

        return !result.dnf() && race.categories.includes(category, result.entry.team.category) && !alreadyWonPrize(result.entry.team);
    }

    private boolean alreadyWonPrize(final Team team) {

        for (RaceResult[] winners : race.prize_winners.values())

            for (RaceResult result : winners)
                if (((RelayRaceResult)result).entry.team.equals(team))
                    return true;

        return false;
    }
}
