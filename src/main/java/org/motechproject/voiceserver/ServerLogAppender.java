package org.motechproject.voiceserver;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServerLogAppender extends AppenderSkeleton {
    private AtomicBoolean started = new AtomicBoolean(false);

    @Override
    protected void append(LoggingEvent loggingEvent) {
        String message = loggingEvent.getRenderedMessage();
        if (message != null && message.startsWith("VoiceXML interpreter") && message.endsWith("started.")) {
            started.set(true);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public void waitTillServerHasStarted()  {
        for (int i = 0; i < 10; i++) {
            if (started.get()) {
                return;
            }
            System.out.println("Waiting for server to start.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void waitTillServerHasStopped() {
        throw new RuntimeException("Unsupported.");
    }
}
