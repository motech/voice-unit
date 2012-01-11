package org.voiceunit.server;

import org.jvoicexml.DocumentDescriptor;
import org.jvoicexml.FetchAttributes;
import org.jvoicexml.GrammarDocument;
import org.jvoicexml.Session;
import org.jvoicexml.documentserver.JVoiceXmlDocumentServer;
import org.jvoicexml.documentserver.schemestrategy.FileSchemeStrategy;
import org.jvoicexml.documentserver.schemestrategy.HttpSchemeStrategy;
import org.jvoicexml.documentserver.schemestrategy.MappedDocumentStrategy;
import org.jvoicexml.documentserver.schemestrategy.builtin.BuiltinSchemeStrategy;
import org.jvoicexml.event.error.BadFetchError;
import org.jvoicexml.xml.vxml.VoiceXmlDocument;

import javax.sound.sampled.AudioInputStream;
import java.net.URI;

public class DocumentServerWhichCanResolveRelativeURLs implements org.jvoicexml.DocumentServer {

    private final JVoiceXmlDocumentServer realDocumentServer;
    private URI baseURI;

    public DocumentServerWhichCanResolveRelativeURLs() {
        realDocumentServer = new JVoiceXmlDocumentServer();
        realDocumentServer.addSchemeStrategy(new MappedDocumentStrategy());
        realDocumentServer.addSchemeStrategy(new FileSchemeStrategy());

        HttpSchemeStrategy httpSchemeStrategy = new HttpSchemeStrategy();
        httpSchemeStrategy.setFetchTimeout(5000);

        HttpSchemeStrategy httpsSchemeStrategy = new HttpSchemeStrategy();
        httpSchemeStrategy.setFetchTimeout(5000);
        httpSchemeStrategy.setScheme("https");

        realDocumentServer.addSchemeStrategy(httpSchemeStrategy);
        realDocumentServer.addSchemeStrategy(httpsSchemeStrategy);
        realDocumentServer.addSchemeStrategy(new BuiltinSchemeStrategy());

        FetchAttributes fetchAttributes = new FetchAttributes();
        fetchAttributes.setFetchTimeout(5000);
        realDocumentServer.setFetchAttributes(fetchAttributes);
    }

    @Override
    public VoiceXmlDocument getDocument(Session session, DocumentDescriptor descriptor) throws BadFetchError {
        return realDocumentServer.getDocument(session, descriptor);
    }

    @Override
    public GrammarDocument getGrammarDocument(Session session, URI uri, FetchAttributes attributes) throws BadFetchError {
        return realDocumentServer.getGrammarDocument(session, uri, attributes);
    }

    @Override
    public AudioInputStream getAudioInputStream(Session session, URI uri) throws BadFetchError {
        return realDocumentServer.getAudioInputStream(session, uri);
    }

    @Override
    public Object getObject(Session session, DocumentDescriptor descriptor, String type) throws BadFetchError {
        descriptor.setURI(baseURI.resolve(descriptor.getUri()));
        return realDocumentServer.getObject(session, descriptor, type);
    }

    @Override
    public URI storeAudio(AudioInputStream in) throws BadFetchError {
        return realDocumentServer.storeAudio(in);
    }

    @Override
    public void sessionClosed(Session session) {
        realDocumentServer.sessionClosed(session);
    }

    public void setBaseURI(URI uri) {
        baseURI = uri;
    }
}
