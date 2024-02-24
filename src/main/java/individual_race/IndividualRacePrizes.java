package individual_race;

import common.Category;

import java.util.ArrayList;
import java.util.List;

public class IndividualRacePrizes {

    final IndividualRace results;

    public IndividualRacePrizes(final IndividualRace results) {
        this.results = results;
    }

    public void allocatePrizes() {

        for (final Category category : IndividualRaceCategory.values())
            results.prize_winners.put(category, getPrizeWinners(category));
    }

    private List<Runner> getPrizeWinners(final Category category) {

        final List<Runner> prize_winners = new ArrayList<>();

        int position = 1;

        for (final Result result : results.overall_results) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result.runner);
                position++;
            }
        }
        return prize_winners;
    }

    private boolean prizeWinner(final Result result, final Category category) {

        return !result.dnf() && category.includes(result.runner.category) && !alreadyWonPrize(result.runner);
    }

    private boolean alreadyWonPrize(final Runner runner) {

        for (List<Runner> winners : results.prize_winners.values())
            if (winners.contains(runner)) return true;

        return false;
    }
}
