package org.lms.recommendation;

import org.lms.entity.Book;

import java.util.List;

public interface RecommendationStrategy {
    List<Book> recommendBooks(String patronId, List<String> patronHistory, List<Book> inventory);
}
