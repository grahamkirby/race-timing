package common;

import series_race.SeriesRace;

import java.util.List;

public abstract class RacePrizes {

    protected SeriesRace race;

    public RacePrizes(final SeriesRace race) {
        this.race = race;
    }

    public void allocatePrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            race.prize_winners.put(category, getPrizeWinners(category));
    }

    protected boolean notYetWonPrize(final Runner entry) {

        for (List<Runner> winners : race.prize_winners.values())
            if (winners.contains(entry)) return false;

        return true;
    }

    protected abstract List<Runner> getPrizeWinners(final Category category);

}
