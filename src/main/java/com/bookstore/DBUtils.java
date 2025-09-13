package com.bookstore;

import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.Map;

public class DBUtils {

    // Delete one order by ID (safe input check)
    public static void deleteOrderById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            System.out.println("⚠️ No orderId provided. Cancelled delete.");
            return;
        }

        try {
            DynamoDbClient client = DynamoDBConfig.getClient();
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("orderId", AttributeValue.builder().s(orderId).build());

            client.deleteItem(DeleteItemRequest.builder()
                    .tableName("Orders")
                    .key(key)
                    .build());

            System.out.println("🗑️ Deleted order: " + orderId);
        } catch (DynamoDbException e) {
            System.out.println("❌ Failed to delete order: " + e.getMessage());
        }
    }

    // Clear all orders (iterates through table and deletes each)
    public static void clearAllOrders() {
        try {
            DynamoDbClient client = DynamoDBConfig.getClient();

            ScanResponse scan = client.scan(
                    ScanRequest.builder().tableName("Orders").build()
            );

            if (scan.count() == 0) {
                System.out.println("ℹ️ No orders to delete.");
                return;
            }

            for (Map<String, AttributeValue> item : scan.items()) {
                String orderId = item.get("orderId").s();
                deleteOrderById(orderId);
            }

            System.out.println("🧹 Cleared all orders.");
        } catch (DynamoDbException e) {
            System.out.println("❌ Failed to clear orders: " + e.getMessage());
        }
    }
}
