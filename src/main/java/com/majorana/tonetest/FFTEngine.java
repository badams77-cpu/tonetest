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

    Logger LOGGER = LogManager.getLogger(FFTEngine.class);

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
        try {
                int framesRead = wavFile.readFrames(channelBuffer, samplesPerBlock);
                if (framesRead==0){ return framesRead; }
                double[] monoBuffer = new double[samplesPerBlock];
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
        double[] ret = new double[12];
        int[] counts = new int[12];
        int lowFreq = octaveFrequencies[octave];
        int highFreq = octaveFrequencies[octave+1];
        int lowBin = (int) ( data.length*rate/(2*lowFreq));
        int highBin = (int) ( data.length*rate/(2*highFreq));
        for(int i=lowBin; i<highBin;i++){
            int noteNumber = (int) Math.floor(((i-lowBin)*12.0)/highBin);
            ret[noteNumber]+= data[i];
            counts[noteNumber]++;
        }
        for(int i=0;i<12;i++){
            ret[i] = counts[i]>0 ? ret[i]/counts[i] : 0;
        }
        return ret;
    }

}
