package com.bookstore;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<Book, Integer> items = new HashMap<>();

    // Add book with quantity
    public void addBook(Book book, int quantity) {
        items.put(book, items.getOrDefault(book, 0) + quantity);
        System.out.println("🛒 Added to cart: " + book + " (x" + quantity + ")");
    }

    // Remove book
    public void removeBook(Book book) {
        if (items.containsKey(book)) {
            items.remove(book);
            System.out.println("❌ Removed from cart: " + book.getTitle());
        } else {
            System.out.println("⚠️ Book not in cart: " + book.getTitle());
        }
    }

    // Calculate total price
    public double calculateTotal() {
        return items.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();
    }

    // Get items
    public Map<Book, Integer> getItems() {
        return items;
    }

    // Clear cart
    public void clear() {
        items.clear();
    }
}
