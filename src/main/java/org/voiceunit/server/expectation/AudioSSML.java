package org.voiceunit.server.expectation;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.text.MessageFormat;

public class AudioSSML {
    private Object actualContent;

    public AudioSSML(Object actualContent) {
        this.actualContent = actualContent;
    }

    public ExpectationException matchAgainstExpectedPath(String expectedAudioFilePath) {
        try {
            String actualAudioPath = getPath();
            if (!actualAudioPath.equals(expectedAudioFilePath) && !actualAudioPath.endsWith(expectedAudioFilePath)) {
                return exceptionFor(expectedAudioFilePath, actualAudioPath);
            }
        } catch (Exception e) {
            return exceptionFor(expectedAudioFilePath, actualContent.toString());
        }
        return null;
    }

    private String getPath() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document doc = factory.newDocumentBuilder().parse(IOUtils.toInputStream(actualContent.toString()));
        XPathExpression xPath = XPathFactory.newInstance().newXPath().compile("//audio/@src");
        return ((NodeList) xPath.evaluate(doc, XPathConstants.NODESET)).item(0).getNodeValue();
    }

    private ExpectationException exceptionFor(String expectedAudioFilePath, String actualPath) {
        String message = MessageFormat.format("Expected to find ''{0}'' in ''{1}''.", expectedAudioFilePath, actualPath);
        return new ExpectationException(message);
    }
}
