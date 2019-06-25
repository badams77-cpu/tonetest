package com.majorana.tonetest;

import WavFile.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ToneMatcher {

    // Number of Octaves to be counted
    public static final int octaveMax = 7;
    public static final int octaveMin = 2;


    private static final int octaveSize = octaveMax-octaveMin+1;

    private static final Logger LOGGER = LogManager.getLogger(ToneMatcher.class);

    private SampleStore sampleStore;

    private LinkedList<Sample>[] octaveTexts = new LinkedList[octaveSize];

    public ToneMatcher(SampleStore sampleStore){
        this.sampleStore = sampleStore;
        for(int i=0;i<octaveSize;i++){
            octaveTexts[i] = new LinkedList<Sample>();
        }
    }

    public void readFully(File inFile){
        FFTEngine engine = new FFTEngine();

        try {
            int framesRead=-1;
            WavFile wavFile = WavFile.openWavFile(inFile);
            while(framesRead!=0){
                framesRead=engine.computeWavBlock(wavFile);
                if (framesRead==0){ break; }
                for(int octave=octaveMin; octave<=octaveMax; octave++) {
                    double bestMatch =0.0;
                    Sample bestSample = null;
                    double[] octaveData = FFTEngine.normalize(engine.getOctave(octave));
                    for (Sample sample : sampleStore.getSamples()) {
                        double match = sample.match(octaveData);
                        if (match>0.0 && match>bestMatch){
                            bestSample = sample;
                            bestMatch = match;
                        }
                    }
                    LinkedList<Sample> samples = getSampleList(octave);
                    if (samples.isEmpty()){
                        samples.add(bestSample);
                    } else{
                        if (samples.peekLast()==null){
                            if (bestSample!=null){
                                samples.add(bestSample);
                            }
                        } else if (!samples.peekLast().equals(bestSample)){
                            samples.add(bestSample);
                        }
                    }
                }
            }
            wavFile.close();
        } catch (
                IOException e){
            LOGGER.error("readWavFully: IOException ",e);
        } catch (
                WavFileException e){
            LOGGER.error("readWavFully: WavFileException",e);
        }

    }

    private LinkedList<Sample> getSampleList(int octave){
        if (octave<octaveMin || octave>octaveMax){ return new LinkedList<>(); }
        return octaveTexts[octave-octaveMin];
    }

    public String getOctaveText(int octave){
        StringBuffer buf = new StringBuffer();
        for(Sample sample : getSampleList(octave)){
            if (sample==null){
                buf.append(" ");
            } else {
                buf.append(sample.getName());
            }
        }
        return buf.toString();
    }

}
