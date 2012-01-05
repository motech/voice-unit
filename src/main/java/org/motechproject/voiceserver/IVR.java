package org.motechproject.voiceserver;

import org.apache.commons.io.FileUtils;
import org.jvoicexml.client.text.TextServer;

import java.io.File;
import java.io.IOException;

public class IVR {
    private String voiceXml;
    final Object lock = new Object();
    private volatile Throwable exception;

    public IVR(String voiceXml) {
        this.voiceXml = voiceXml;
    }

    public IVR(File fileContainingVoiceXml) {
        try {
            this.voiceXml = FileUtils.readFileToString(fileContainingVoiceXml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getsCallFrom(Caller caller) {
        CallController callController = new CallController(caller);

        setupExceptionHandler();
        TextListener listener = new TextListener(callController, lock);
        VoiceServer voiceServer = new VoiceServer().start();
        TextServer textServer = startTextServer(listener);

        callController.withServer(voiceServer).withTextServer(textServer).startCallFor(voiceXml);
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
}
