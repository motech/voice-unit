package org.motechproject.voiceserver.expectation;

import org.motechproject.voiceserver.CallController;

import java.text.MessageFormat;

public class TTSExpectation implements Expectation {
    private String expectedText;

    public TTSExpectation(String expectedText) {
        this.expectedText = expectedText;
    }

    @Override
    public void actOn(Object actualContent, CallController callController) {
        if (!expectedText.equals(actualContent) && !actualContent.toString().endsWith(expectedText + "</speak>")) {
            callController.expectationWasNotMatched(new ExpectationException(expectedText, actualContent));
        }
    }

    @Override
    public String description() {
        return MessageFormat.format("Hears ''{0}''.", expectedText);
    }
}
