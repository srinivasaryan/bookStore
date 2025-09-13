package com.bookstore;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class CheckoutService {
    private final DynamoDbClient client;
    private final String tableName = "Orders"; // DynamoDB table
    private final Scanner sc = new Scanner(System.in);

    public CheckoutService() {
        this.client = DynamoDBConfig.getClient();
    }

    // Place order with review + optional removal + confirmation
    public void checkout(User user, Cart cart) {
        if (cart.getItems().isEmpty()) {
            System.out.println("⚠️ Cart is empty! Cannot checkout.");
            return;
        }

        // Step 1: Show cart
        System.out.println("\n🛒 Your Cart (Review Before Checkout):");
        cart.getItems().forEach((b, q) ->
                System.out.println(b + " (x" + q + ")"));
        System.out.printf("💰 Total: $%.2f%n", cart.calculateTotal());

        // Step 2: Ask if they want to remove anything
        System.out.print("👉 Do you want to remove any quantity before checkout? (yes/no): ");
        String removeAns = sc.nextLine().trim().toLowerCase();
        if (removeAns.equals("yes") || removeAns.equals("y")) {
            removeBookByTitleOrAuthorWithQuantity(cart);
            if (cart.getItems().isEmpty()) {
                System.out.println("⚠️ Cart is empty after removal. Checkout cancelled.");
                return;
            }
            // Show updated cart
            System.out.println("\n🛒 Updated Cart:");
            cart.getItems().forEach((b, q) ->
                    System.out.println(b + " (x" + q + ")"));
            System.out.printf("💰 Total: $%.2f%n", cart.calculateTotal());
        }

        // Step 3: Ask for final confirmation
        System.out.print("👉 Do you want to place this order? (yes/no): ");
        String confirmAns = sc.nextLine().trim().toLowerCase();
        if (!confirmAns.equals("yes") && !confirmAns.equals("y")) {
            System.out.println("❌ Checkout cancelled. Cart is still available.");
            return;
        }

        // Step 4: Place order
        double total = cart.calculateTotal();

        Map<String, AttributeValue> order = new HashMap<>();
        order.put("orderId", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        order.put("username", AttributeValue.builder().s(user.getUsername()).build());
        order.put("email", AttributeValue.builder().s(user.getEmail()).build());
        order.put("total", AttributeValue.builder().n(String.valueOf(total)).build());

        StringBuilder booksList = new StringBuilder();
        for (Map.Entry<Book, Integer> entry : cart.getItems().entrySet()) {
            booksList.append(entry.getKey().getTitle())
                     .append(" x")
                     .append(entry.getValue())
                     .append(", ");
        }
        order.put("books", AttributeValue.builder().s(booksList.toString()).build());

        client.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(order)
                .build());

        // Step 5: Print invoice
        System.out.println("\n🧾 Order Invoice:");
        cart.getItems().forEach((b, q) ->
                System.out.println(b + " (x" + q + ")"));
        System.out.println("💰 Total Paid: $" + total);
        System.out.println("✅ Order placed successfully for " + user.getUsername());

        // Step 6: Clear cart
        cart.clear();
    }

    // Helper: remove book by title/author with quantity
    private void removeBookByTitleOrAuthorWithQuantity(Cart cart) {
        if (cart.getItems().isEmpty()) {
            System.out.println("🛒 Cart is empty.");
            return;
        }

        System.out.print("Enter title or author to remove: ");
        String key = sc.nextLine().trim();

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
        int qtyToRemove;
        try {
            qtyToRemove = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("⚠️ Invalid number. Skipping removal.");
            return;
        }

        if (qtyToRemove <= 0) {
            System.out.println("⚠️ Invalid quantity.");
            return;
        }

        if (qtyToRemove >= currentQty) {
            cart.removeBook(match);
            System.out.printf("🗑️ Removed all of: %s%n", match);
        } else {
            cart.getItems().put(match, currentQty - qtyToRemove);
            System.out.printf("🗑️ Removed %d of: %s (remaining %d)%n",
                    qtyToRemove, match, currentQty - qtyToRemove);
        }
    }
}
