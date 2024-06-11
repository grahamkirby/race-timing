package individual_race;

import common.Category;
import common.RaceResult;

import java.util.ArrayList;
import java.util.List;

public class IndividualRacePrizes {

    // TODO rationalise with RelayRacePrizes

    final IndividualRace race;

    public IndividualRacePrizes(final IndividualRace race) {
        this.race = race;
    }

    public void allocatePrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            race.prize_winners.put(category, getPrizeWinners(category));
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

    private boolean prizeWinner(final RaceResult result, final Category category) {

        return !((IndividualRaceResult)result).DNF && race.categories.includes(category, ((IndividualRaceResult)result).entry.runner.category) && !alreadyWonPrize(((IndividualRaceResult)result).entry);
    }

    private boolean alreadyWonPrize(final IndividualRaceEntry entry) {

        for (final List<RaceResult> winners : race.prize_winners.values())
            for (final RaceResult result : winners)
                if (((IndividualRaceResult)result).entry.equals(entry))
                    return true;

        return false;
    }
}
