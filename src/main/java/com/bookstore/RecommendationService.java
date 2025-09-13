package com.bookstore;

import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {

    private final DynamoDbClient dynamoDb;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<RecommendedBook> table;

    public RecommendationService() {
        this.dynamoDb = DynamoDbClient.builder()
                .endpointOverride(java.net.URI.create("http://localhost:8000")) // Local DynamoDB
                .region(software.amazon.awssdk.regions.Region.US_EAST_1)
                .build();

        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDb)
                .build();

        this.table = enhancedClient.table("RecommendedBooks", TableSchema.fromBean(RecommendedBook.class));
    }

    /** Ensure table exists */
    public void ensureTable() {
        try {
            dynamoDb.describeTable(DescribeTableRequest.builder().tableName("RecommendedBooks").build());
        } catch (ResourceNotFoundException e) {
            dynamoDb.createTable(CreateTableRequest.builder()
                    .tableName("RecommendedBooks")
                    .keySchema(
                            KeySchemaElement.builder().attributeName("userId").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("bookId").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("userId").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("bookId").attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
                    )
                    .build());
        }
    }

    /** Seed sample data for a user if empty */
    public void seedIfEmpty(String userId) {
        boolean hasAny = table.query(r -> r
                        .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build()))
                        .limit(1))
                .stream()
                .anyMatch(page -> !page.items().isEmpty());

        if (hasAny) return;

        // ✅ Insert exactly 5 recommendations
        put(userId, 9.7, "B001", "Clean Code", "Robert C. Martin", "Programming", "You liked Java books");
        put(userId, 9.4, "B002", "Effective Java", "Joshua Bloch", "Programming", "Based on past purchases");
        put(userId, 9.1, "B003", "Design Patterns", "GoF", "Programming", "Similar readers enjoyed this");
        put(userId, 8.8, "B004", "Refactoring", "Martin Fowler", "Programming", "Popular in your network");
        put(userId, 8.6, "B005", "The Pragmatic Programmer", "Andrew Hunt", "Programming", "Trending now");
    }

    /** Helper: Insert one recommendation */
    private void put(String userId, double score, String bookId, String title, String author, String genre, String reason) {
        RecommendedBook rb = new RecommendedBook();
        rb.setUserId(userId);
        rb.setBookId(bookId);
        rb.setTitle(title);
        rb.setAuthor(author);
        rb.setGenre(genre);
        rb.setScore(score);
        rb.setReason(reason);
        rb.setRecommendedAt(Instant.now()); // ✅ FIXED: store as Instant

        table.putItem(rb);
    }

    /** Always return (and keep) only top-5 recommendations for a user */
    public List<RecommendedBook> top5(String userId) {
        List<RecommendedBook> all = table.query(r -> r
                        .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                                .partitionValue(userId).build())))
                .items()
                .stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore())) // sort high → low
                .collect(Collectors.toList());

        // Trim database to 5 if more exist
        if (all.size() > 5) {
            List<RecommendedBook> toDelete = all.subList(5, all.size());
            for (RecommendedBook rb : toDelete) {
                table.deleteItem(rb);
            }
            return all.subList(0, 5);
        }

        return all;
    }

    /** Force cleanup: keep only top-5 in DB */
    public void trimToTop5(String userId) {
        top5(userId); // already trims and deletes extras
    }
}
