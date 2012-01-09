package org.motechproject.voiceserver;

import org.junit.Test;

import java.io.File;

public class TestIVR {

    public static final String COMMON_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><vxml version=\"2.1\" xmlns=\"http://www.w3.org/2001/vxml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schematicLocation=\"http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd\"><meta content=\"JVoiceXML group\" name=\"author\"></meta><meta content=\"2005-2010 JVoiceXML group - http://jvoicexml.sourceforge.net\" name=\"copyright\"></meta>";
    public static final String COMMON_FOOTER = "</vxml>";

    @Test
    public void shouldRecognizeTextToSpeech() {
        String xmlInput = COMMON_HEADER + "<form id=\"say_hello\"><block>Hello World!</block></form>" + COMMON_FOOTER;

        Caller caller = new Caller();
        caller.hears("Hello World!");

        new IVR(xmlInput).getsCallFrom(caller);
    }

    @Test
    public void shouldAcceptDTMF() throws Exception {
        Caller caller = new Caller();
        caller.hears("Welcome to the Job Aid Course. This will help you in your duties.");
        caller.hears(".\n            Number of levels are");
        caller.hears("2.0");
        caller.hears(".");
        caller.respondToPrompt("Please select a level.\n                Current no input count is: 0.0", '1');
        caller.hears("You entered 1");
        caller.hears("This is an intro for level");
        caller.hears("1");
        caller.respondToPrompt("Select chapter within level 1.", '2');
        caller.hears("You entered 2");
        caller.hears("You called from");

        File voiceXmlFile = new File(getClass().getResource("/jobaid.xml").getPath());
        new IVR(voiceXmlFile).getsCallFrom(caller);
    }

    @Test
    public void shouldFindAudioByPartialPath() throws Exception {
        String xmlInput = vxmlWithAudioTag();

        Caller caller = new Caller();
        caller.listensTo("Hello.wav");

        new IVR(xmlInput).getsCallFrom(caller);
    }

    @Test
    public void shouldFindAudioByFullPath() throws Exception {
        String xmlInput = vxmlWithAudioTag();

        Caller caller = new Caller();
        caller.listensTo("http://audio.example.com/Hello.wav");

        new IVR(xmlInput).getsCallFrom(caller);
    }

    @Test
    public void shouldRespondToAudioPromptWithDTMFInput() {
        String xmlInput = vxmlWithAudioPrompt();

        Caller caller = new Caller();
        caller.respondToAudio("PromptForInput.wav", '1');
        caller.hears("You entered:");
        caller.hears("1");

        new IVR(xmlInput).getsCallFrom(caller);
    }

    @Test
    public void shouldRespondToAudioPromptUsingFullURLWithDTMFInput() {
        String xmlInput = vxmlWithAudioPrompt();

        Caller caller = new Caller();
        caller.respondToAudio("http://audio.example.com/PromptForInput.wav", '1');
        caller.hears("You entered:");
        caller.hears("1");

        new IVR(xmlInput).getsCallFrom(caller);
    }

    /* Cannot hangup when there is a prompt, which expects input, is coming up next. */
    @Test
    public void shouldBeAbleToHangupTheCallAtCertainPoints() {
        String xmlInput = vxmlWithAudioPrompt();

        Caller caller = new Caller();
        caller.respondToAudio("http://audio.example.com/PromptForInput.wav", '1');
        caller.hangup();

        new IVR(xmlInput).getsCallFrom(caller);
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
}
