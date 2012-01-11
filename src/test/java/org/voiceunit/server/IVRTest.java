package org.voiceunit.server;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class IVRTest {

    public static final String COMMON_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><vxml version=\"2.1\" xmlns=\"http://www.w3.org/2001/vxml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schematicLocation=\"http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd\"><meta content=\"JVoiceXML group\" name=\"author\"></meta><meta content=\"2005-2010 JVoiceXML group - http://jvoicexml.sourceforge.net\" name=\"copyright\"></meta>";
    public static final String COMMON_FOOTER = "</vxml>";
    private File fileContainingVoiceXml;

    @Test
    public void shouldRecognizeTextToSpeech() {
        String xmlInput = COMMON_HEADER + "<form id=\"say_hello\"><block><value expr=\"'Call from ' + session.connection.local.uri\"/></block></form>" + COMMON_FOOTER;

        Caller caller = new Caller();
        caller.hears("Call from 5771101");

        new IVR(fileFor(xmlInput)).at("5771101").getsCallFrom(caller);
    }

    @Test
    public void shouldAcceptDTMF() throws Exception {
        Caller caller = new Caller();
        caller.respondToPrompt("Please enter something.", '1');
        caller.hears("You entered 1");

        File voiceXmlFile = new File(getClass().getResource("/jobaid.xml").getPath());
        new IVR(voiceXmlFile).getsCallFrom(caller);
    }

    @Test
    public void shouldResolveDynamicScriptWithRelativePath() throws Exception {
        Caller caller = new Caller();
        caller.hears("Life is 42");

        File voiceXmlFile = new File(getClass().getResource("/vxml_with_dynamic_script.xml").getPath());
        new IVR(voiceXmlFile).getsCallFrom(caller);
    }

    @Test
    public void shouldFindAudioByPartialPath() throws Exception {
        String xmlInput = vxmlWithAudioTag();

        Caller caller = new Caller();
        caller.listensTo("Hello.wav");

        new IVR(fileFor(xmlInput)).getsCallFrom(caller);
    }

    @Test
    public void shouldFindAudioByFullPath() throws Exception {
        String xmlInput = vxmlWithAudioTag();

        Caller caller = new Caller();
        caller.listensTo("http://audio.example.com/Hello.wav");

        new IVR(fileFor(xmlInput)).getsCallFrom(caller);
    }

    @Test
    public void shouldRespondToAudioPromptWithDTMFInput() {
        String xmlInput = vxmlWithAudioPrompt();

        Caller caller = new Caller();
        caller.respondToAudio("PromptForInput.wav", '1');
        caller.hears("You entered:");
        caller.hears("1");

        new IVR(fileFor(xmlInput)).getsCallFrom(caller);
    }

    @Test
    public void shouldRespondToAudioPromptUsingFullURLWithDTMFInput() {
        String xmlInput = vxmlWithAudioPrompt();

        Caller caller = new Caller();
        caller.respondToAudio("http://audio.example.com/PromptForInput.wav", '1');
        caller.hears("You entered:");
        caller.hears("1");

        new IVR(fileFor(xmlInput)).getsCallFrom(caller);
    }

    /* Cannot hangup when there is a prompt, which expects input, is coming up next. */
    @Test
    public void shouldBeAbleToHangupTheCallAtCertainPoints() {
        String xmlInput = vxmlWithAudioPrompt();

        Caller caller = new Caller();
        caller.respondToAudio("http://audio.example.com/PromptForInput.wav", '1');
        caller.hangup();

        new IVR(fileFor(xmlInput)).getsCallFrom(caller);
    }

    @After
    public void tearDown() {
        if (fileContainingVoiceXml != null) {
            fileContainingVoiceXml.deleteOnExit();
            FileUtils.deleteQuietly(fileContainingVoiceXml);
        }

    }
    private String vxmlWithAudioTag() {
        return COMMON_HEADER +
                    "<script>var GREETING = \"http://audio.example.com/Hello.wav\";</script>" +
                    "<form id=\"abc\">" +
                    "  <block name=\"welcome\">" +
                    "    <prompt name=\"welcome\" bargein=\"false\">" +
                    "      <audio expr=\"GREETING\"/>" +
                    "    </prompt>" +
                    "  </block>" +
                    "</form>" + COMMON_FOOTER;
    }

    private String vxmlWithAudioPrompt() {
        return COMMON_HEADER +
                "<script>var GREETING = \"http://audio.example.com/PromptForInput.wav\";</script>" +
                "<form id=\"abc\">" +
                "  <field name=\"responseToPrompt\">" +
                "    <prompt name=\"welcome\" bargein=\"false\">" +
                "      <audio expr=\"GREETING\"/>" +
                "    </prompt>" +
                "    <grammar mode=\"dtmf\" version=\"1.0\" root=\"root\">\n" +
                "      <rule id=\"root\">\n" +
                "        <one-of>\n" +
                "          <item>0</item>\n" +
                "          <item>1</item>\n" +
                "          <item>2</item>\n" +
                "          <item>3</item>\n" +
                "          <item>4</item>\n" +
                "          <item>5</item>\n" +
                "          <item>6</item>\n" +
                "          <item>7</item>\n" +
                "          <item>8</item>\n" +
                "          <item>9</item>\n" +
                "          <item>*</item>\n" +
                "          <item>#</item>\n" +
                "        </one-of>\n" +
                "      </rule>\n" +
                "    </grammar>" +
                "  </field>" +
                "  <block name=\"confirmation\">You entered: <value expr=\"responseToPrompt\"/></block>" +
                "</form>" + COMMON_FOOTER;
    }

    private File fileFor(String xmlInput) {
        try {
            fileContainingVoiceXml = File.createTempFile("voice", "xml");
            FileUtils.writeStringToFile(fileContainingVoiceXml, xmlInput);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileContainingVoiceXml;
    }
}
