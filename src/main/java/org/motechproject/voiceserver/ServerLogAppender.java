package org.motechproject.voiceserver;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerLogAppender extends AppenderSkeleton {
    private AtomicBoolean started = new AtomicBoolean(false);
    private List<String> messages = new ArrayList<String>();

    @Override
    protected void append(LoggingEvent loggingEvent) {
        String message = loggingEvent.getRenderedMessage();

        messages.add(message);
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
            System.out.println("Waiting for server to start. Count: " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Server failed to start in 10 seconds. Messages are:\n" + messages.toString());
    }
}
