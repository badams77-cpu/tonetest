package com.majorana.tonetest;

public class Sample {

    private String name;
    private int octaveNumber;
    private double[] octaveData;

    public Sample(String name, int octaveNumber){
        this.name = name;
        this.octaveNumber = octaveNumber;
        octaveData = new double[12];
    }
}
