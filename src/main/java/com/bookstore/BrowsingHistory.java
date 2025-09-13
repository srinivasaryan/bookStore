package com.bookstore;

import java.util.LinkedList;

public class BrowsingHistory {
    private LinkedList<Book> history = new LinkedList<>();
    private static final int MAX_HISTORY = 5; // ✅ keep only the last 5 viewed books

    // Add a book to history
    public void add(Book book) {
        if (book == null) return;

        // Remove existing to avoid duplicates
        history.remove(book);

        // Add at the beginning (most recent first)
        history.addFirst(book);

        // Ensure history size does not exceed MAX_HISTORY
        if (history.size() > MAX_HISTORY) {
            history.removeLast(); // remove oldest
        }
    }

    // Display browsing history
    public void display() {
        if (history.isEmpty()) {
            System.out.println("📭 No browsing history yet.");
            return;
        }
        System.out.println("📜 Recently Browsed Books (last " + MAX_HISTORY + "):");
        int i = 1;
        for (Book book : history) {
            System.out.println(i++ + ". " + book);
        }
    }
}
