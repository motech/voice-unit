package org.voiceunit.server.expectation;

import org.voiceunit.server.CallController;

public class HangupExpectation implements Expectation {
    @Override
    public void actOn(Object actualContent, CallController callController) {
        throw new RuntimeException("Unsupported.");
    }

    @Override
    public String description() {
        throw new RuntimeException("Should not have come here. This is just a marker to know when to hangup.");
    }
}
