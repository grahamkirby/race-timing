package common;

import java.util.List;

public interface Category {

    boolean includes(final Category other_category);

    int numberOfPrizes();

    String shortName();

    String longName();
}
