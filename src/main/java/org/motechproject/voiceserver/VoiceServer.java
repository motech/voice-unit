package org.motechproject.voiceserver;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.DocumentServer;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.event.ErrorEvent;

import java.io.File;
import java.net.URI;

public class VoiceServer {
    private ServerLogAppender appender;
    private final JVoiceXmlMain jVoiceXml;

    public VoiceServer() {
        Logger logger = Logger.getLogger("org.jvoicexml");
        logger.setLevel(Level.DEBUG);
        appender = new ServerLogAppender();
        logger.addAppender(appender);

        String pathToConfigDir = new File(getClass().getResource("/jvoicexml.xml").getFile()).getParent();
        System.setProperty("jvoicexml.config", pathToConfigDir);
        jVoiceXml = new JVoiceXmlMain();
    }

    public Session createSession(ConnectionInformation connectionInformation) throws ErrorEvent {
        return jVoiceXml.createSession(connectionInformation);
    }

    public VoiceServer start() {
        jVoiceXml.start();
        appender.waitTillServerHasStarted();
        return this;
    }

    public void shutdown() {
        jVoiceXml.shutdown();
        jVoiceXml.waitShutdownComplete();

        killCrazyTerminationThreadWhichKillsTheJVM();
    }

    private void killCrazyTerminationThreadWhichKillsTheJVM() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] activeThreads = new Thread[group.activeCount() * 2];

        int numberOfThreads = group.enumerate(activeThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            Thread thread = activeThreads[i];
            if (thread.getName().equals("TerminationThread")) {
                thread.interrupt();
            }
        }
    }

    public void setBaseURIInDocumentServer(URI uri) {
        DocumentServerWhichCanResolveRelativeURLs documentServer = (DocumentServerWhichCanResolveRelativeURLs) jVoiceXml.getConfiguration().loadObject(DocumentServer.class);
        documentServer.setBaseURI(uri);
    }
}
