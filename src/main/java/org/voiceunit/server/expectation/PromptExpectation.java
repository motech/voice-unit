package org.voiceunit.server.expectation;

import org.voiceunit.server.CallController;

import java.text.MessageFormat;

public class PromptExpectation implements Expectation {
    private String expectedPromptText;
    private char responseToSend;

    public PromptExpectation(String expectedPromptText, char responseToSend) {
        this.expectedPromptText = expectedPromptText;
        this.responseToSend = responseToSend;
    }

    @Override
    public void actOn(Object actualContent, CallController callController) {
        if (!actualContent.toString().contains(expectedPromptText)) {
            callController.expectationWasNotMatched(new ExpectationException(expectedPromptText, actualContent));
        }
        callController.sendDTMF(responseToSend);
    }

    @Override
    public String description() {
        return MessageFormat.format("Hears ''{0}'', respond with ''{1}''.", expectedPromptText, responseToSend);
    }

}
