package com.bookstore;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class BookService {
    private final DynamoDbClient client;
    private final String tableName = "Books";

    public BookService() {
        this.client = DynamoDBConfig.getClient();
    }

    // ---------- CREATE (insert one book) ----------
    public void addBook(Book book) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("title", AttributeValue.builder().s(book.getTitle()).build());
        item.put("author", AttributeValue.builder().s(book.getAuthor()).build());
        item.put("price", AttributeValue.builder().n(String.valueOf(book.getPrice())).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        client.putItem(request);
    }

    // ---------- SEED (insert if not already present) ----------
    public void seedBooks(List<Book> books) {
        for (Book book : books) {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("title", AttributeValue.builder().s(book.getTitle()).build());

            GetItemRequest getRequest = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

            GetItemResponse response = client.getItem(getRequest);
            if (!response.hasItem()) {
                addBook(book);
                System.out.println("📚 Inserted: " + book);
            }
        }
    }

    // ---------- READ ALL ----------
    public List<Book> getAllBooks() {
        List<Book> results = new ArrayList<>();

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse response = client.scan(scanRequest);

        for (Map<String, AttributeValue> item : response.items()) {
            String title  = item.containsKey("title")  ? item.get("title").s()  : "Unknown Title";
            String author = item.containsKey("author") ? item.get("author").s() : "Unknown Author";
            double price  = item.containsKey("price")  ? Double.parseDouble(item.get("price").n()) : 0.0;
            results.add(new Book(title, author, price));
        }
        return results;
    }

    // ---------- READ ONE (by title) ----------
    public Book getBookByTitle(String titleKey) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("title", AttributeValue.builder().s(titleKey).build());

        GetItemRequest req = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse res = client.getItem(req);
        if (!res.hasItem()) return null;

        Map<String, AttributeValue> item = res.item();
        String title  = item.containsKey("title")  ? item.get("title").s()  : "Unknown Title";
        String author = item.containsKey("author") ? item.get("author").s() : "Unknown Author";
        double price  = item.containsKey("price")  ? Double.parseDouble(item.get("price").n()) : 0.0;
        return new Book(title, author, price);
    }

    // ---------- SEARCH (title or author) → returns Book objects ----------
    public List<Book> searchBooksAsBooks(String keyword) {
        List<Book> results = new ArrayList<>();

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("contains(title, :kw) OR contains(author, :kw)")
                .expressionAttributeValues(
                        Collections.singletonMap(":kw", AttributeValue.builder().s(keyword).build())
                )
                .build();

        ScanResponse response = client.scan(scanRequest);

        for (Map<String, AttributeValue> item : response.items()) {
            String title  = item.containsKey("title")  ? item.get("title").s()  : "Unknown Title";
            String author = item.containsKey("author") ? item.get("author").s() : "Unknown Author";
            double price  = item.containsKey("price")  ? Double.parseDouble(item.get("price").n()) : 0.0;
            results.add(new Book(title, author, price));
        }

        return results;
    }
}
