package org.motechproject.voiceserver;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.DocumentServer;
import org.jvoicexml.JVoiceXmlMain;
import org.jvoicexml.Session;
import org.jvoicexml.event.ErrorEvent;
import org.motechproject.voiceserver.utility.FileUtils;

import java.io.File;
import java.net.URI;

public class VoiceServer {
    private ServerLogAppender appender;
    private final JVoiceXmlMain jVoiceXml;
    private File temporaryConfigDirectory = null;

    public VoiceServer() {
        Logger logger = Logger.getLogger("org.jvoicexml");
        logger.setLevel(Level.DEBUG);
        appender = new ServerLogAppender();
        logger.addAppender(appender);

        setupConfigurationDirectory();
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
        org.apache.commons.io.FileUtils.deleteQuietly(temporaryConfigDirectory);
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

    private void setupConfigurationDirectory() {
        String[] configFileNames = new String[] { "/jmf.properties", "/jvoicexml.gram", "/jvoicexml.policy",
                "/jvoicexml.xml", "/jvxml-callmanager-0-7.xsd", "/jvxml-grammar-0-7.xsd", "/jvxml-grammar.xml",
                "/jvxml-implementation-0-7.xsd", "/jvxml-implementation.xml", "/jvxml-tagsupport-0-7.xsd",
                "/logging.properties", "/spring-beans-2.0.xsd", "/text-implementation.xml", "/vxml2.1-tagsupport.xml" };

        temporaryConfigDirectory = new File("jvxml_configs");
        temporaryConfigDirectory.deleteOnExit();

        System.setProperty("jvoicexml.config", temporaryConfigDirectory.getAbsolutePath());
        FileUtils.copyClasspathFilesToDirectory(configFileNames, temporaryConfigDirectory);
    }
}
