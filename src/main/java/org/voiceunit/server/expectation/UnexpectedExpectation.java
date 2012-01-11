package org.voiceunit.server.expectation;

import org.voiceunit.server.CallController;

import java.text.MessageFormat;

public class UnexpectedExpectation implements Expectation {
    @Override
    public void actOn(Object actualContent, CallController callController) {
        callController.expectationWasNotMatched(new ExpectationException(MessageFormat.format("Expected nothing. Got: ''{0}''.", actualContent)));
    }

    @Override
    public String description() {
        return "Unexpected";
    }
}
