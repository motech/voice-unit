package org.motechproject.voiceserver;

import org.junit.Test;

import java.io.File;

public class TestOutputSynthesis {

    public static final String COMMON_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><vxml version=\"2.1\" xmlns=\"http://www.w3.org/2001/vxml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schematicLocation=\"http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd\"><meta content=\"JVoiceXML group\" name=\"author\"></meta><meta content=\"2005-2010 JVoiceXML group - http://jvoicexml.sourceforge.net\" name=\"copyright\"></meta>";
    public static final String COMMON_FOOTER = "</vxml>";

    @Test
    public void shouldRecognizeTextToSpeech() {
        String xmlInput = COMMON_HEADER + "<form id=\"say_hello\"><block>Hello World!</block></form>" + COMMON_FOOTER;

        org.motechproject.voiceserver.Caller caller = new org.motechproject.voiceserver.Caller();
        caller.hears("Hello World!");

        new org.motechproject.voiceserver.IVR(xmlInput).getsCallFrom(caller);
    }

    @Test
    public void shouldAcceptDTMF() throws Exception {
        org.motechproject.voiceserver.Caller caller = new org.motechproject.voiceserver.Caller();
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
        new org.motechproject.voiceserver.IVR(voiceXmlFile).getsCallFrom(caller);
    }

    @Test
    public void shouldFindAudioByPartialPath() throws Exception {
        String xmlInput = vxmlWithAudioTag();

        org.motechproject.voiceserver.Caller caller = new org.motechproject.voiceserver.Caller();
        caller.listensTo("Hello.wav");

        new org.motechproject.voiceserver.IVR(xmlInput).getsCallFrom(caller);
    }

    @Test
    public void shouldFindAudioByFullPath() throws Exception {
        String xmlInput = vxmlWithAudioTag();

        org.motechproject.voiceserver.Caller caller = new org.motechproject.voiceserver.Caller();
        caller.listensTo("http://audio.example.com/Hello.wav");

        new org.motechproject.voiceserver.IVR(xmlInput).getsCallFrom(caller);
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
}
