package org.motechproject.voiceserver.expectation;

import java.text.MessageFormat;

public class ExpectationException extends RuntimeException {

    public ExpectationException(String message) {
        super(message);
    }

    public ExpectationException(Object expected, Object actualContent) {
        super(MessageFormat.format("\n\n         Expected: ''{0}''" + "\nTo match (actual): ''{1}''.\n",
                expected, actualContent));
    }
}
