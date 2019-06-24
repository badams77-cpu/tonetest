package com.majorana.tonetest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleStore {

    private static Logger LOGGER = LogManager.getLogger(SampleStore.class);

    private List<Sample> samples;

    private  String directory;


    public SampleStore(String directory){
        this.directory = directory;
        this.samples = new LinkedList<>();
    }

    public void readAll(){
        File dir = new File(directory);
        LOGGER.debug("Reading directory "+dir.getAbsolutePath());
        for(File f: dir.listFiles()){
            String name = f.getName();
            if (name.equalsIgnoreCase(".wav")) {
                Pattern pat = Pattern.compile("^([a-zA-Z0-0])*_(\\d)");
                Matcher matcher = pat.matcher(name);
                if (!matcher.find()) {
                    LOGGER.debug(name+" does match pattern test_octavenumber");
                    continue;
                }
                String text = matcher.group(1);
                int octave = Integer.parseInt(matcher.group(2));
            }

        }

    }
}
