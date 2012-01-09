package org.motechproject.voiceserver;

import org.apache.commons.io.FileUtils;
import org.jvoicexml.Session;
import org.jvoicexml.client.text.TextServer;
import org.motechproject.voiceserver.expectation.ExpectationException;

import java.io.File;
import java.text.MessageFormat;

public class CallController {
    private Caller caller;
    private VoiceServer voiceServer;
    private TextServer textServer;
    private Session session;
    private volatile ExpectationException exception;
    private boolean hasHungUp = false;

    public CallController(Caller caller) {
        this.caller = caller;
    }

    public CallController withServer(VoiceServer voiceServer) {
        this.voiceServer = voiceServer;
        return this;
    }

    public CallController withTextServer(TextServer textServer) {
        this.textServer = textServer;
        return this;
    }

    public void startCallFor(String voiceXml) {
        File voiceFile = null;
        try {
            voiceFile = File.createTempFile("voice", "xml");
            FileUtils.writeStringToFile(voiceFile, voiceXml);

            session = voiceServer.createSession(textServer.getConnectionInformation());
            session.call(voiceFile.toURI());

            session.waitSessionEnd();
            hangUp();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (voiceFile != null) {
                voiceFile.deleteOnExit();
                FileUtils.deleteQuietly(voiceFile);
            }
            stopServers();
        }
    }

    public void sendDTMF(char responseToSend) {
        try {
            session.getCharacterInput().addCharacter(responseToSend);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void wasExpecting(Object actualContent) {
        checkForCallerHangup();
        handleEvent(actualContent);
        checkForCallerHangup();
    }

    public void expectationWasNotMatched(ExpectationException exceptionWhichNeedsToBeThrown) {
        exception = exceptionWhichNeedsToBeThrown;
        hangUp();
    }

    public ExpectationException getException() {
        return exception;
    }

    private void hangUp() {
        session.hangup();

        if (exception == null && caller.hasMoreExpectations()) {
            String message = MessageFormat.format("Expected more interactions. Next expected interaction: {0}",
                    caller.nextExpectation().description());
            exception = new ExpectationException(message);
        }
    }

    private void stopServers() {
        textServer.stopServer();
        voiceServer.shutdown();
    }

    private void checkForCallerHangup() {
        if (!hasHungUp && caller.nextActionIsToHangup()) {
            caller.nextExpectation();
            hasHungUp = true;
        }
    }

    private void handleEvent(Object actualContent) {
        if (exception == null && !hasHungUp) {
            try {
                caller.nextExpectation().actOn(actualContent, this);
            } catch (Exception e) {
                expectationWasNotMatched(new ExpectationException("Failed in expectation: " + e.toString()));
            }
        }
    }
}
