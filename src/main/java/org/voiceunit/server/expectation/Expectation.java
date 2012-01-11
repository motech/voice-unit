package org.voiceunit.server.expectation;

import org.voiceunit.server.CallController;

public interface Expectation {
    void actOn(Object actualContent, CallController callController);

    String description();
}
