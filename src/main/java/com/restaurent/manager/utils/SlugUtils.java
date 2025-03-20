package com.restaurent.manager.utils;

import java.text.Normalizer;

public class SlugUtils {
    public static String toSlug(String input) {
        // Normalize the string to decompose combined characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Remove diacritics (accents)
        String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Replace non-alphanumeric characters with hyphens
        String slug = withoutDiacritics.replaceAll("[^\\p{Alnum}]+", "-");

        // Convert to lowercase and trim hyphens from ends
        slug = slug.toLowerCase().replaceAll("^-|-$", "");

        return slug;
    }
}
