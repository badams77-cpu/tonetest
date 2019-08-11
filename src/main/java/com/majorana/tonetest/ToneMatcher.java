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

    private static final int volumeLength = FFTEngine.SAMPLES_PER_SECOND * 8;

    private static final int MINIMUM_FOR_QUIET = FFTEngine.SAMPLES_PER_SECOND/2;

    private static final double RATIO_FOR_QUIET = 1.0/4.0;

    private static final double matchFraction = 1.0/12.0;

    private static final int octaveSize = 8;

    private static final Logger LOGGER = LogManager.getLogger(ToneMatcher.class);

    private SampleStore sampleStore;

    private Sample nullSample;

    private LinkedList<Double>[] octaveVolumes;

    private int[] quietCount;


    private LinkedList<Sample>[] octaveTexts = new LinkedList[octaveSize];

    public ToneMatcher(SampleStore sampleStore){
        this.sampleStore = sampleStore;
        nullSample = new Sample(" ",0);
        octaveVolumes = new LinkedList[octaveSize];
        quietCount = new int[octaveSize];
        for(int i=0;i<octaveSize;i++){
            octaveTexts[i] = new LinkedList<Sample>();
            octaveVolumes[i]= new LinkedList<Double>();
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
                    double[] octaveData = engine.getOctave(octave);
                    double rms = FFTEngine.rms(octaveData);
                    double vol = 0;
                    for(double vols : octaveVolumes[octave]){
                        vol+= vols;
                    }
                    vol = octaveVolumes[octave].size()==0 ? 0 :vol/octaveVolumes[octave].size();
                    octaveVolumes[octave].add(rms);
                    if (octaveVolumes[octave].size()>volumeLength){
                        octaveVolumes[octave].removeFirst();
                    }
                    if (rms < vol*RATIO_FOR_QUIET){
                        if (quietCount[octave]++>MINIMUM_FOR_QUIET){
                            bestSample = nullSample;
                        }
                    } else {
                        quietCount[octave] = 0;
                        for (Sample sample : sampleStore.getSamples()) {
                            double match = sample.match(octaveData);
                            if (match > matchFraction * rms && match > bestMatch) {
                                bestSample = sample;
                                bestMatch = match;
                            }
                        }
                    }
                    if (bestSample==null){ continue; }
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
