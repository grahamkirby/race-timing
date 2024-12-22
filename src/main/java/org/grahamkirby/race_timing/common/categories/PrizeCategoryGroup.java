package org.grahamkirby.race_timing.common.categories;

import java.util.List;

/**
 * Represents a grouping of prize categories, used in some cases to structure results output.
 * For example, the 'Under 9' group in a junior race may include prize categories
 * 'Female Under 9' and 'Male Under 9'. The results may be split into different groups where
 * runners of different ages complete different courses.
 * <br />
 * Values are read from a configuration file such as
 * {@link /src/main/resources/configuration/categories_prize_individual_junior.csv}.
 */
public record PrizeCategoryGroup(String group_title, List<PrizeCategory> categories) {
}
