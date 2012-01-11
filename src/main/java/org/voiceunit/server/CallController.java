package org.voiceunit.server;

import org.apache.log4j.Logger;
import org.jvoicexml.client.text.TextConnectionInformation;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.interpreter.JVoiceXmlSession;
import org.voiceunit.server.expectation.Expectation;
import org.voiceunit.server.expectation.ExpectationException;

import java.net.URI;
import java.text.MessageFormat;
import java.util.regex.Pattern;

public class CallController {
    private Caller caller;
    private VoiceServer voiceServer;
    private TextServer textServer;
    private JVoiceXmlSession session;
    private volatile ExpectationException exception;
    private boolean hasHungUp = false;

    private Logger LOGGER = Logger.getLogger(this.getClass());

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

    public void startCallFor(String phoneNumberAtWhichIVRIsSetup, URI uriOfVoiceXML) {
        try {
            URI baseURI = new URI(Pattern.compile("/[^/]*$").matcher(uriOfVoiceXML.toASCIIString()).replaceAll("/"));
            voiceServer.setBaseURIInDocumentServer(baseURI);

            TextConnectionInformation connectionInformation = (TextConnectionInformation) textServer.getConnectionInformation();
            connectionInformation.setCalledDevice(new URI(phoneNumberAtWhichIVRIsSetup));

            session = (JVoiceXmlSession) voiceServer.createSession(connectionInformation);
            session.call(uriOfVoiceXML);

            session.waitSessionEnd();
            hangUp();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
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
            LOGGER.info("Caller has hung up.");
            hasHungUp = true;
        }
    }

    private void handleEvent(Object actualContent) {
        if (exception == null && !hasHungUp) {
            try {
                Expectation expectation = caller.nextExpectation();
                LOGGER.debug("Checking expectation: " + expectation.description() + " against " + actualContent);
                expectation.actOn(actualContent, this);
                LOGGER.debug("Expectation matched.");
            } catch (Exception e) {
                expectationWasNotMatched(new ExpectationException("Failed in expectation: " + e.toString()));
            }
        }
    }
}
