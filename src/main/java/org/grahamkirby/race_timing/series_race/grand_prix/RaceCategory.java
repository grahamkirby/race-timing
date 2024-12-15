package org.grahamkirby.race_timing.series_race.grand_prix;

import java.util.List;

public record RaceCategory(String category_title, int minimum_number_to_be_completed, List<Integer> race_numbers){}
