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
            LOGGER.debug("testing "+name);
            if (name.endsWith(".wav")) {
                Pattern pat = Pattern.compile("^([a-zA-Z0-9]*)_(\\d)\\.wav");
                Matcher matcher = pat.matcher(name);
                if (!matcher.find()) {
                    LOGGER.debug(name+" does not match pattern test_octavenumber");
                    continue;
                }
                String text = matcher.group(1);
                int octave = Integer.parseInt(matcher.group(2));
                Sample samp = new Sample(text, octave);
                LOGGER.debug("Reading "+f.getName());
                samp.readWavFully(f);
                samples.add(samp);
            }

        }

    }

    public List<Sample> getSamples(){
        return samples;
    }

}
