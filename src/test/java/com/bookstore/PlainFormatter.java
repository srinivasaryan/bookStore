package com.bookstore;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;

public class PlainFormatter implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        // Hook into different cucumber events
        publisher.registerHandlerFor(TestSourceRead.class, this::handleFeature);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleScenario);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleStep);
    }

    private void handleFeature(TestSourceRead event) {
        System.out.println("\nFeature: " + event.getSource().split("\n")[0].replace("Feature: ", ""));
    }

    private void handleScenario(TestCaseStarted event) {
        System.out.println("\n  Scenario: " + event.getTestCase().getName());
    }

    private void handleStep(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();
            System.out.println("    " + step.getStep().getKeyword() + step.getStep().getText());
        }
    }
}
