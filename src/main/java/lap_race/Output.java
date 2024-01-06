package lap_race;

import java.io.IOException;

public abstract class Output {

    protected final Results results;

    public Output(final Results results) {
        this.results = results;
    }

    public abstract void printOverallResults() throws IOException;
    public abstract void printDetailedResults() throws IOException;

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= results.number_of_legs; leg++)
            printLegResults(leg);
    }

    abstract void printLegResults(final int leg) throws IOException;

    public abstract void printPrizes() throws IOException;
}
