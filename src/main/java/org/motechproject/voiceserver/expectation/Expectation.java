package org.motechproject.voiceserver.expectation;

import org.motechproject.voiceserver.CallController;

public interface Expectation {
    void actOn(Object actualContent, CallController callController);

    String description();
}
