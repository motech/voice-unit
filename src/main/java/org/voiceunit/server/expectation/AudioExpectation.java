package org.voiceunit.server.expectation;

import org.voiceunit.server.CallController;

import java.text.MessageFormat;

public class AudioExpectation implements Expectation {
    private String expectedAudioFilePath;

    public AudioExpectation(String expectedAudioFilePath) {
        this.expectedAudioFilePath = expectedAudioFilePath;
    }

    @Override
    public void actOn(Object actualContent, CallController callController) {
        ExpectationException exception = new AudioSSML(actualContent).matchAgainstExpectedPath(expectedAudioFilePath);
        if (exception != null) {
            callController.expectationWasNotMatched(exception);
        }
    }

    @Override
    public String description() {
        return MessageFormat.format("Listens to a file by name: ''{0}''.", expectedAudioFilePath);
    }
}
