package series_race;

import common.Category;
import individual_race.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeriesRacePrizes {

    final SeriesRace race;

    public SeriesRacePrizes(final SeriesRace race) {
        this.race = race;
    }

    public void allocatePrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            race.prize_winners.put(category, getPrizeWinners(category));
    }

    private List<Runner> getPrizeWinners(final Category category) {

        final List<Runner> prize_winners = new ArrayList<>();

        int position = 1;

        for (final SeriesRaceResult result : race.getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result.runner);
                position++;
            }
        }
        return prize_winners;
    }

    private boolean prizeWinner(final SeriesRaceResult result, final Category category) {

        boolean includes = race.categories.includes(category, result.runner.category);
        boolean b = alreadyWonPrize(result.runner);
        return result.completed() && includes && !b;
    }

    private boolean alreadyWonPrize(final Runner entry) {

        for (List<Runner> winners : race.prize_winners.values())
            if (winners.contains(entry)) return true;

        return false;
    }
}
