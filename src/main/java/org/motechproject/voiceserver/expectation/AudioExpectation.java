package org.motechproject.voiceserver.expectation;

import org.apache.commons.io.IOUtils;
import org.motechproject.voiceserver.CallController;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.text.MessageFormat;

public class AudioExpectation implements Expectation {
    private String expectedAudioFilePath;

    public AudioExpectation(String expectedAudioFilePath) {
        this.expectedAudioFilePath = expectedAudioFilePath;
    }

    @Override
    public void actOn(Object actualContent, CallController callController) {
        try {
            String actualAudioPath = audioPathFromSSML(actualContent);
            if (!actualAudioPath.equals(expectedAudioFilePath) && !actualAudioPath.endsWith(expectedAudioFilePath)) {
                callController.expectationWasNotMatched(exceptionFor(actualAudioPath));
            }
        } catch (Exception e) {
            callController.expectationWasNotMatched(exceptionFor(actualContent.toString()));
        }
    }

    @Override
    public String description() {
        return MessageFormat.format("Listens to a file by name: ''{0}''.", expectedAudioFilePath);
    }

    private ExpectationException exceptionFor(String actualPath) {
        String message = MessageFormat.format("Expected to find ''{0}'' in ''{1}''.", expectedAudioFilePath, actualPath);
        return new ExpectationException(message);
    }

    private String audioPathFromSSML(Object actualContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document doc = factory.newDocumentBuilder().parse(IOUtils.toInputStream(actualContent.toString()));
        XPathExpression xPath = XPathFactory.newInstance().newXPath().compile("//audio/@src");
        return ((NodeList) xPath.evaluate(doc, XPathConstants.NODESET)).item(0).getNodeValue();
    }
}
