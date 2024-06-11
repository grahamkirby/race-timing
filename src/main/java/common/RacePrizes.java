package common;

import series_race.SeriesRace;
import series_race.SeriesRaceResult;

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

        for (List<RaceResult> winners : race.prize_winners.values())
            for (RaceResult res : winners) {
                if (((SeriesRaceResult) res).runner.equals(entry)) return false;
            }

        return true;
    }

    protected abstract List<RaceResult> getPrizeWinners(final Category category);
}
