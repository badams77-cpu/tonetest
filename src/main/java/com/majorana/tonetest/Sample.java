package com.majorana.tonetest;

import WavFile.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Sample {

    private double matchCutoff = 0.300;

    public static final int SEMITONES_IN_OCTAVE = 12;
    private static Logger LOGGER = LogManager.getLogger(Sample.class);

    private String name;
    private int octaveNumber;
    private double[] octaveData;

    public Sample(String name, int octaveNumber){
        this.name = name;
        this.octaveNumber = octaveNumber;
        octaveData = new double[SEMITONES_IN_OCTAVE];
    }

    public void readWavFully(File inFile){
        try {
            WavFile wavFile = WavFile.openWavFile(inFile);
            FFTEngine fftEngine = new FFTEngine();
            int read=1;
            while(read>0) {
                read = fftEngine.computeWavBlock(wavFile);
                if (read>0){
                    double data[] = fftEngine.getOctave(octaveNumber);
                    for(int i = 0; i< SEMITONES_IN_OCTAVE; i++){
                        octaveData[i]+= data[i];
                    }
                }

            }
            octaveData = FFTEngine.normalize(octaveData);
            wavFile.close();
        } catch (IOException e){
            LOGGER.error("readWavFully: IOException ",e);
        } catch (WavFileException e){
            LOGGER.error("readWavFully: WavFileException",e);
        }
    }

    public double match(double testOctaveData[]){
        if (name.equals("wo")){
           LOGGER.trace("testing wo");
        }
        double match = 0.0;
        for(int i=0; i<SEMITONES_IN_OCTAVE; i++ ){
            match += (octaveData[i]-matchCutoff)* testOctaveData[i];
        }
        return match;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOctaveNumber() {
        return octaveNumber;
    }

    public void setOctaveNumber(int octaveNumber) {
        this.octaveNumber = octaveNumber;
    }

    public double[] getOctaveData() {
        return octaveData;
    }

    public void setOctaveData(double[] octaveData) {
        this.octaveData = octaveData;
    }
}
