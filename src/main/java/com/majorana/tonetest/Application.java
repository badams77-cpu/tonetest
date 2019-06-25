package com.majorana.tonetest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Application {

    Logger LOGGER = LogManager.getLogger(Application.class);

    private SampleStore sampleStore;

    private String sampleDir = "sample";

    public static void main(String argv[]) {
        Application app = new Application();
        app.run(argv);
    }

    public void run(String argv[]){
        if (argv.length<1){
            LOGGER.error("Usage: Application wavFile");
            LOGGER.error("Requires input file");
        }
        sampleStore = new SampleStore(sampleDir);
        sampleStore.readAll();
        File f = new File(argv[0]);
        ToneMatcher matcher = new ToneMatcher(sampleStore);
        matcher.readFully(f);
        for(int octave=ToneMatcher.octaveMin ; octave<=ToneMatcher.octaveMax; octave++){
            LOGGER.info(octave+":"+ matcher.getOctaveText(octave));
        }
    }



}
