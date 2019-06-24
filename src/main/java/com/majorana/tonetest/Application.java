package com.majorana.tonetest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    Logger LOGGER = LogManager.getLogger(Application.class);

    private SampleStore sampleStore;

    private String sampleDir = "sample";

    public static void main(String argv[]) {
        Application app = new Application();
        app.run(argv);
    }

    public void run(String argv[]){
        sampleStore = new SampleStore(sampleDir);

    }



}
