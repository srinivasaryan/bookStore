package com.bookstore;

import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("📚 Welcome to the Bookstore!");

        // Seed catalog (your existing list)
        List<Book> storeBooks = Arrays.asList(
                new Book("Introduction to Algorithms", "Thomas H. Cormen", 55.00),
                new Book("Design Patterns", "Erich Gamma", 39.99),
                new Book("Refactoring", "Martin Fowler", 45.00),
                new Book("Effective Java", "Joshua Bloch", 42.50),
                new Book("The Pragmatic Programmer", "Andrew Hunt", 35.75),
                new Book("Head First Java", "Kathy Sierra", 27.40),
                new Book("Java Concurrency in Practice", "Brian Goetz", 49.99)
        );

        BookService bookService = new BookService();
        bookService.seedBooks(storeBooks);

        Cart cart = new Cart();
        CheckoutService checkoutService = new CheckoutService();
        BrowsingHistory browsingHistory = new BrowsingHistory(); // ✅ Day 7 feature

        // User details with validation
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim().toLowerCase();
        while (username.isEmpty()) {
            System.out.print("⚠️ Username cannot be empty. Please enter again: ");
            username = scanner.nextLine().trim().toLowerCase();
        }

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        while (email.isEmpty()) {
            System.out.print("⚠️ Email cannot be empty. Please enter again: ");
            email = scanner.nextLine().trim();
        }

        User user = new User(username, email);
        System.out.println("👤 Shopping as: " + user);

        // Recommendations init
        RecommendationService recService = new RecommendationService();
        try {
            recService.ensureTable();
            recService.seedIfEmpty(username);
            recService.trimToTop5(username); // keep only top-5 per user
        } catch (Exception e) {
            System.out.println("⚠️ Could not initialize recommendations: " + e.getMessage());
        }

        while (true) {
            System.out.println("\n========== BOOKSTORE ==========");
            System.out.println("Actions:");
            System.out.println("1. Add Book ");
            System.out.println("2. Remove Book from Cart");
            System.out.println("3. View Cart & Total");
            System.out.println("4. Checkout Now");
            System.out.println("5. Exit");
            System.out.println("6. View Available Books (from DB)");
            System.out.println("7. Search Books (title/author from DB)");
            System.out.println("8. Delete one order by ID");
            System.out.println("9. Clear all orders");
            System.out.println("10. View Top-5 Recommended Books");
            System.out.println("11. View Recently Browsed Books");
            System.out.println("================================");
            System.out.print("Enter your choice: ");

            int choice = readIntOrMinusOne();

            switch (choice) {
                case 1: { // Guided add → continue? → optional remove → checkout
                    do {
                        addBookFlow(bookService, cart, browsingHistory);
                    } while (askYesNo("Do you want to continue shopping? (yes/no): "));

                    if (!cart.getItems().isEmpty()) {
                        if (askYesNo("Do you want to remove anything before checkout? (yes/no): ")) {
                            removeBookByTitleOrAuthorWithQuantity(cart);
                        }
                        checkoutService.checkout(user, cart);
                    } else {
                        System.out.println("🛒 Cart is empty. Nothing to checkout.");
                    }
                    break;
                }

                case 2: { // Manual remove
                    if (cart.getItems().isEmpty()) {
                        System.out.println("🛒 Cart is empty.");
                    } else {
                        removeBookByTitleOrAuthorWithQuantity(cart);
                    }
                    break;
                }

                case 3: { // View cart
                    printCart(cart);
                    break;
                }

                case 4: { // Checkout now
                    if (cart.getItems().isEmpty()) {
                        System.out.println("🛒 Cart is empty.");
                    } else {
                        checkoutService.checkout(user, cart);
                    }
                    break;
                }

                case 5: { // Exit
                    System.out.println("👋 Goodbye!");
                    return;
                }

                case 6: { // View all books
                    List<Book> booksFromDb = bookService.getAllBooks();
                    if (booksFromDb.isEmpty()) {
                        System.out.println("⚠️ No books in store.");
                    } else {
                        System.out.println("📖 Books in Store:");
                        for (int i = 0; i < booksFromDb.size(); i++) {
                            System.out.println((i + 1) + ". " + booksFromDb.get(i));
                        }

                        System.out.print("Enter number to mark as viewed (or 0 to skip): ");
                        int choiceView = readIntOrMinusOne();
                        if (choiceView >= 1 && choiceView <= booksFromDb.size()) {
                            Book viewed = booksFromDb.get(choiceView - 1);
                            browsingHistory.add(viewed); // ✅ Add to history
                            System.out.println("👀 Viewed: " + viewed);
                        }
                    }
                    break;
                }

                case 7: { // Search
                    System.out.print("Enter keyword (title/author): ");
                    String keyword = scanner.nextLine().trim();
                    List<Book> matches = bookService.searchBooksAsBooks(keyword);
                    if (matches.isEmpty()) {
                        System.out.println("No books found.");
                        break;
                    }
                    System.out.println("🔎 Search Results:");
                    for (int i = 0; i < matches.size(); i++) {
                        System.out.println((i + 1) + ". " + matches.get(i));
                    }

                    System.out.print("Enter number to mark as viewed (or 0 to skip): ");
                    int choiceView = readIntOrMinusOne();
                    if (choiceView >= 1 && choiceView <= matches.size()) {
                        Book viewed = matches.get(choiceView - 1);
                        browsingHistory.add(viewed); // ✅ Add to history
                        System.out.println("👀 Viewed: " + viewed);
                    }
                    break;
                }

                case 8: { // Delete order
                    while (true) {
                        System.out.print("Enter orderId to delete (or type 'cancel' to go back): ");
                        String orderId = scanner.nextLine().trim();
                        if (orderId.equalsIgnoreCase("cancel")) {
                            System.out.println("Cancelled.");
                            break;
                        }
                        if (orderId.isEmpty()) {
                            System.out.println("⚠️ orderId cannot be empty. Please try again or type 'cancel'.");
                            continue;
                        }
                        DBUtils.deleteOrderById(orderId);
                        break;
                    }
                    break;
                }

                case 9: { // Clear all orders
                    if (askYesNo("Are you sure you want to delete ALL orders? (yes/no): ")) {
                        DBUtils.clearAllOrders();
                    } else {
                        System.out.println("Cancelled.");
                    }
                    break;
                }

                case 10: { // Recommendations
                    List<RecommendedBook> recs = recService.top5(username);
                    if (recs.isEmpty()) {
                        System.out.println("😕 No recommendations yet for " + username);
                    } else {
                        System.out.println("=== Top 5 Recommended Books for " + username + " ===");
                        int i = 1;
                        for (RecommendedBook r : recs) {
                            System.out.printf("%d) %s by %s  [score %.2f]%n   reason: %s%n",
                                    i++, r.getTitle(), r.getAuthor(), r.getScore(), r.getReason());
                        }
                    }
                    break;
                }

                case 11: { // ✅ Browsing History
                    browsingHistory.display();
                    break;
                }

                default:
                    System.out.println("⚠️ Invalid choice. Try again.");
            }
        }
    }

    /* ---------- helpers ---------- */

    private static void addBookFlow(BookService bookService, Cart cart, BrowsingHistory browsingHistory) {
        List<Book> availableBooks = bookService.getAllBooks();
        if (availableBooks.isEmpty()) {
            System.out.println("⚠️ No books in store.");
            return;
        }
        System.out.println("📖 Available Books:");
        for (int i = 0; i < availableBooks.size(); i++) {
            System.out.println((i + 1) + ". " + availableBooks.get(i));
        }

        System.out.print("Enter book number to add: ");
        int addChoice = readIntOrMinusOne();
        if (addChoice < 1 || addChoice > availableBooks.size()) {
            System.out.println("⚠️ Invalid book number.");
            return;
        }

        Book selected = availableBooks.get(addChoice - 1);

        System.out.print("Enter quantity: ");
        int qty = readIntOrMinusOne();
        if (qty <= 0) {
            System.out.println("⚠️ Invalid quantity.");
            return;
        }

        cart.addBook(selected, qty);

        // ✅ Track in browsing history
        browsingHistory.add(selected);

        System.out.printf("✅ Added: %s (x%d)%n", selected, qty);
        System.out.printf("🛒 Cart total so far: $%.2f%n", cart.calculateTotal());
    }

    private static void removeBookByTitleOrAuthorWithQuantity(Cart cart) {
        if (cart.getItems().isEmpty()) {
            System.out.println("🛒 Cart is empty.");
            return;
        }
        printCart(cart);

        System.out.print("Enter title or author to remove: ");
        String key = scanner.nextLine().trim();

        // Find first matching item by title or author (case-insensitive, contains)
        Book match = null;
        for (Book b : cart.getItems().keySet()) {
            if (b.getTitle().toLowerCase().contains(key.toLowerCase())
                    || b.getAuthor().toLowerCase().contains(key.toLowerCase())) {
                match = b;
                break;
            }
        }

        if (match == null) {
            System.out.println("⚠️ No matching book found in cart.");
            return;
        }

        int currentQty = cart.getItems().getOrDefault(match, 0);
        System.out.printf("Found: %s (x%d in cart)%n", match, currentQty);

        System.out.print("Enter quantity to remove: ");
        int qtyToRemove = readIntOrMinusOne();
        if (qtyToRemove <= 0) {
            System.out.println("⚠️ Invalid quantity.");
            return;
        }

        if (qtyToRemove >= currentQty) {
            cart.removeBook(match); // remove completely
            System.out.printf("🗑️ Removed all of: %s%n", match);
        } else {
            cart.getItems().put(match, currentQty - qtyToRemove);
            System.out.printf("🗑️ Removed %d of: %s (remaining %d)%n",
                    qtyToRemove, match, currentQty - qtyToRemove);
        }

        System.out.printf("🛒 Cart total now: $%.2f%n", cart.calculateTotal());
    }

    private static void printCart(Cart cart) {
        if (cart.getItems().isEmpty()) {
            System.out.println("🛒 Cart is empty.");
            return;
        }
        System.out.println("🛒 Your Cart:");
        cart.getItems().forEach((b, q) -> System.out.println(b + " (x" + q + ")"));
        System.out.printf("💰 Total Price: $%.2f%n", cart.calculateTotal());
    }

    private static int readIntOrMinusOne() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private static boolean askYesNo(String prompt) {
        System.out.print(prompt);
        String ans = scanner.nextLine().trim().toLowerCase();
        return ans.equals("y") || ans.equals("yes");
    }
}
