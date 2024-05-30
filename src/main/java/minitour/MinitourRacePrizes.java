package minitour;

import common.Category;
import common.Runner;

import java.util.ArrayList;
import java.util.List;

public class MinitourRacePrizes {

    final MinitourRace race;

    public MinitourRacePrizes(final MinitourRace race) {
        this.race = race;
    }

    public void allocatePrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            race.prize_winners.put(category, getPrizeWinners(category));
    }

    private List<Runner> getPrizeWinners(final Category category) {

        final List<Runner> prize_winners = new ArrayList<>();

        int position = 1;

        for (final MinitourRaceResult result : race.getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result.runner);
                position++;
            }
        }
        return prize_winners;
    }

    private boolean prizeWinner(final MinitourRaceResult result, final Category category) {

        return result.completed() && race.categories.includes(category, result.runner.category) && !alreadyWonPrize(result.runner);
    }

    private boolean alreadyWonPrize(final Runner entry) {

        for (final List<Runner> winners : race.prize_winners.values())
            if (winners.contains(entry)) return true;

        return false;
    }
}
