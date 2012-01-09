About:
=====

This is a unit-testing framework for [VoiceXML](http://www.w3.org/TR/voicexml20/), using [JVoiceXML](http://jvoicexml.sourceforge.net/). As of now, some of the features are:

* Ability to assert on TTS (Text-To-Speech) output.
* Ability to assert on Audio output.
* Ability to enter DTMF input in response to TTS and Audio prompts.

Examples:
=========

Take a look at [TestIVR.java](https://github.com/motech/voice-unit/blob/master/src/test/java/org/motechproject/voiceserver/TestIVR.java).

Opening the project in IntelliJ Idea
====================================

1. Clone this repository from github.
2. Run "mvn validate"
3. Run "mvn clean install"
4. Wait for Maven to download the internet. :)
5. You can use one of the Community Edition versions. Install the "Maven Integration" plugin and then go to File -> Open Project and then select the pom.xml.

  Build. Run the tests.
