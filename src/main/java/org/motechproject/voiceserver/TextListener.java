package org.motechproject.voiceserver;

import org.jvoicexml.xml.ssml.SsmlDocument;

import java.net.InetSocketAddress;

class TextListener implements org.jvoicexml.client.text.TextListener {
    private CallController callController;
    private final Object lock;

    public TextListener(CallController callController, Object lock) {
        this.callController = callController;
        this.lock = lock;
    }

    @Override
    public void outputText(String text) {
        callController.wasExpecting(text);
    }

    @Override
    public void outputSsml(SsmlDocument document) {
        callController.wasExpecting(document);
    }

    @Override
    public void started() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void connected(InetSocketAddress remote) {
    }

    @Override
    public void disconnected() {
    }
}
