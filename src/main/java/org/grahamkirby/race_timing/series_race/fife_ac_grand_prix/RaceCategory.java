package org.grahamkirby.race_timing.series_race.fife_ac_grand_prix;

import java.util.List;

public record RaceCategory(String category_title, int minimum_number_to_be_completed, List<Integer> race_numbers){}
