package org.motechproject.voiceserver.utility;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

public class FileUtils {

    public static void copyClasspathFilesToDirectory(String[] filePathsRelativeToClasspath, File directory) {
        for (String configFileName : filePathsRelativeToClasspath) {
            try {
                File targetFile = new File(directory, configFileName);
                copyInputStreamToFile(FileUtils.class.getResourceAsStream(configFileName), targetFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
