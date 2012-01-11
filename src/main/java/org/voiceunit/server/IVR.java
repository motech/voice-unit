package org.voiceunit.server;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.LevelRangeFilter;
import org.jvoicexml.client.text.TextServer;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URI;

public class IVR {
    private URI uriOfVoiceXML;
    final Object lock = new Object();
    private volatile Throwable exception;

    public IVR(File fileContainingVoiceXml) {
        this.uriOfVoiceXML = fileContainingVoiceXml.toURI();
    }

    public IVR(URI url) {
        this.uriOfVoiceXML = url;
    }

    public void getsCallFrom(Caller caller) {
        CallController callController = new CallController(caller);

        setupExceptionHandler();
        TextListener listener = new TextListener(callController, lock);
        VoiceServer voiceServer = new VoiceServer().start();
        TextServer textServer = startTextServer(listener);

        callController.withServer(voiceServer).withTextServer(textServer).startCallFor(uriOfVoiceXML);
        failIfErrorsHaveBeenThrown(callController);
    }

    private TextServer startTextServer(TextListener listener) {
        TextServer textServer = new TextServer(8011);
        textServer.addTextListener(listener);
        textServer.start();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return textServer;
    }

    private void failIfErrorsHaveBeenThrown(CallController callController) {
        if (callController.getException() != null) {
            throw new RuntimeException(callController.getException());
        }
        if (exception != null) {
            throw new RuntimeException(exception);
        }
    }

    private void setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                exception = throwable;
            }
        });
    }

    public IVR verbose() {
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setWriter(new OutputStreamWriter(System.out));
        consoleAppender.setLayout(new PatternLayout("%6r [%-20.20t] %-5p %30.30c (%6L) %x %m%n"));
        LevelRangeFilter levelRangeFilter = new LevelRangeFilter();
        levelRangeFilter.setLevelMin(Level.DEBUG);

        consoleAppender.addFilter(levelRangeFilter);
        Logger.getRootLogger().addAppender(consoleAppender);

        return this;
    }
}
