package com.bookstore;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBConfig {

    // Low-level client for DynamoDB Local
    public static DynamoDbClient getClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1) // region must match your config; any region works for local
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("fakeMyKeyId", "fakeSecretAccessKey")
                        )
                )
                .endpointOverride(URI.create("http://localhost:8000")) // Local DynamoDB
                .build();
    }

    // Enhanced client (object mapper for beans)
    public static DynamoDbEnhancedClient getEnhancedClient() {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(getClient())
                .build();
    }
}
