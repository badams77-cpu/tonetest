package com.majorana.tonetest;


import WavFile.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FFTEngine {

    public static final int SEMITONES_IN_OCTAVE = 12;
    private static Logger LOGGER = LogManager.getLogger(FFTEngine.class);

    private static int samplesPerSecond=8;

    private int[] octaveFrequencies = {16,32,65,130,261,523,1047,2093,4186,8372};

    private double data[];
    private long rate;
    private int samplesPerBlock;

    public FFTEngine(){
        data = new double[0];
    }

    public int  computeWavBlock(WavFile wavFile){
        int numChannels = wavFile.getNumChannels();
        rate = wavFile.getSampleRate();
        samplesPerBlock = (int) rate/samplesPerSecond;
        double channelBuffer[] = new double[numChannels*samplesPerBlock];
        int valid_length = 1;
        while(valid_length<samplesPerBlock){ valid_length=valid_length*2; }
        try {
                int framesRead = wavFile.readFrames(channelBuffer, samplesPerBlock);
                if (framesRead==0 || framesRead<samplesPerBlock){ return 0; }
                double[] monoBuffer = new double[valid_length];
                // Add each channel onto total
                for(int i=0; i<samplesPerBlock;i+=1){
                    int pos = i*numChannels;
                    for(int j=0; j<numChannels; j++){
                        monoBuffer[i]+= channelBuffer[pos+j];
                    }
                }
                FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
                Complex[] fftData = fastFourierTransformer.transform(monoBuffer, TransformType.FORWARD);
                data = new double[fftData.length];
                for(int i=0;i<fftData.length;i++){
                    data[i] = fftData[i].abs();
                }
                return framesRead;
        } catch (WavFileException e){
            LOGGER.error("WavFileException ",e);
            return 0;
        } catch (IOException e){
            LOGGER.error("IOException ",e);
            return 0;
        }
    }

    public double[] getOctave(int octave){
        double[] ret = new double[SEMITONES_IN_OCTAVE];
        int[] counts = new int[SEMITONES_IN_OCTAVE];
        int lowFreq = octaveFrequencies[octave-1];
        int highFreq = octaveFrequencies[octave];
        int lowBin = (int) ( data.length*lowFreq/(2*rate));
        int highBin = (int) ( data.length*highFreq/(2*rate));
        if (highBin>data.length){ return ret; }
        for(int i=lowBin; i<highBin;i++){
            int noteNumber = (int) (((i-lowBin)*SEMITONES_IN_OCTAVE*1.0)/highBin);
            ret[noteNumber]+= data[i];
            counts[noteNumber]++;
        }
        for(int i = 0; i< SEMITONES_IN_OCTAVE; i++){
            ret[i] = counts[i]>0 ? ret[i]/counts[i] : 0;
        }
        return ret;
    }

    public static double[] normalize(double[] octaveData){
        double max=0;
        double ret[] = new double[SEMITONES_IN_OCTAVE];
        for(int i = 0; i< SEMITONES_IN_OCTAVE; i++){
            if (octaveData[i]>max){
                max = octaveData[i];
            }
        }
        if (max!=0.0) {
            for (int i = 0; i < SEMITONES_IN_OCTAVE; i++) {
                ret[i] = octaveData[i] / max;
            }
        } else {
            LOGGER.warn("Read null sample");
        }
        return ret;
    }

}
