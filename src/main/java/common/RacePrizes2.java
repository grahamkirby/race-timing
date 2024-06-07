package common;

import series_race.SeriesRace;
import series_race.SeriesRaceResult;

public abstract class RacePrizes2 {

    protected SeriesRace race;

    public RacePrizes2(final SeriesRace race) {
        this.race = race;
    }

    public void allocatePrizes() {

        for (final Category category : race.categories.getCategoriesInDecreasingGeneralityOrder())
            race.prize_winners.put(category, getPrizeWinners(category));
    }

    protected boolean notYetWonPrize(final Runner entry) {

        for (RaceResult[] winners : race.prize_winners.values())
            for (RaceResult res : winners) {
                if (((SeriesRaceResult) res).runner.equals(entry)) return false;
            }

        return true;
    }

    protected abstract RaceResult[] getPrizeWinners(final Category category);
}
