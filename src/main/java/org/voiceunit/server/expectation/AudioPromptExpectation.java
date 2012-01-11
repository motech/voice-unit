package org.voiceunit.server.expectation;

import org.voiceunit.server.CallController;

import java.text.MessageFormat;

public class AudioPromptExpectation implements Expectation {
    private String expectedAudioFilePath;
    private char responseToSend;

    public AudioPromptExpectation(String expectedAudioFilePath, char responseToSend) {
        this.expectedAudioFilePath = expectedAudioFilePath;
        this.responseToSend = responseToSend;
    }

    @Override
    public void actOn(Object actualContent, CallController callController) {
        ExpectationException exception = new AudioSSML(actualContent).matchAgainstExpectedPath(expectedAudioFilePath);
        if (exception != null) {
            callController.expectationWasNotMatched(exception);
        }
        callController.sendDTMF(responseToSend);
    }

    @Override
    public String description() {
        return MessageFormat.format("Listens to ''{0}'', respond with ''{1}''.", expectedAudioFilePath, responseToSend);
    }
}
