package org.lms.recommendation;

import org.lms.entity.Book;

import java.util.*;
import java.util.stream.Collectors;

public class BasicRecommendationStrategy implements RecommendationStrategy {
    @Override
    public List<Book> recommendBooks(String patronId, List<String> patronHistory, List<Book> inventory) {
        if(patronHistory.isEmpty()) {
            // Recommend popular books if no history
            return Collections.emptyList(); // Placeholder for popular books logic
        }
        Map<String, Long> authorFrequency = inventory.stream().filter(b -> patronHistory.contains(b.getIsbn()))
                .collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()));

        if(authorFrequency.isEmpty()) {
            return Collections.emptyList();
        }

        String favAuthor = authorFrequency.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        if(favAuthor == null) {
            return Collections.emptyList();
        }
        Set<String> already = new HashSet<>(patronHistory);
        return inventory.stream()
                .filter(b -> b.getAuthor().equals(favAuthor) && !already.contains(b.getIsbn()))
                .collect(Collectors.toList());
    }
}
