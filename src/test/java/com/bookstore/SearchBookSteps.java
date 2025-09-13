package com.bookstore;

import com.bookstore.Book;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchBookSteps {

    private final List<Book> store = new ArrayList<>();
    private List<Book> results = new ArrayList<>();

    @Given("the bookstore has {string} by {string}")
    public void the_bookstore_has(String title, String author) {
        store.add(new Book(title, author, 10.0)); // test data with dummy price
    }

    @When("the user searches for {string}")
    public void the_user_searches_for(String keyword) {
        results = store.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase())
                          || b.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Then("the system shows {string} with author name {string}")
    public void the_system_shows_with_author_name(String expectedTitle, String expectedAuthor) {
        boolean found = results.stream()
                .anyMatch(b -> b.getTitle().equals(expectedTitle) && b.getAuthor().equals(expectedAuthor));
        Assertions.assertTrue(found, "Expected to find book: " + expectedTitle + " by " + expectedAuthor);
    }
}
