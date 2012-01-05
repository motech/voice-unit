package org.motechproject.voiceserver;

import org.motechproject.voiceserver.expectation.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Caller {
    Queue<Expectation> expectations;

    public Caller() {
        expectations = new ConcurrentLinkedQueue<Expectation>();
    }

    public void hears(String expectedText) {
        expectations.add(new TTSExpectation(expectedText));
    }

    public void respondToPrompt(String expectedPromptText, char responseToSend) {
        expectations.add(new PromptExpectation(expectedPromptText, responseToSend));
    }

    public void hangup() {
    }

    public Expectation nextExpectation() {
        if (!expectations.isEmpty()) {
            return expectations.remove();
        }
        return new UnexpectedExpectation();
    }

    public boolean hasMoreExpectations() {
        return !expectations.isEmpty();
    }

    public void listensTo(String audioFilePath) {
        expectations.add(new AudioExpectation(audioFilePath));
    }
}
