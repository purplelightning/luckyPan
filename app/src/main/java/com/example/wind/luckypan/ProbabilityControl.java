package com.example.wind.luckypan;

import java.util.Random;

/**
 * Created by wind on 17-6-25.
 */

public class ProbabilityControl {
    private float A, B, C, D, E, F;
    private int sum=0;

    public ProbabilityControl(int a, int b, int c, int d, int e, int f) {
        sum=a+b+c+d+e+f;
        A = 1000*a/sum;
        B = 1000*b/sum;
        C = 1000*c/sum;
        D = 1000*d/sum;
        E = 1000*e/sum;
        F = 1000*f/sum;
    }

    public int setPro(){
        double x= Math.random()*1000;

        if(x<=A){
            return 0;
        }else if(x<=A+B){
            return 1;
        }else if(x<=A+B+C){
            return 2;
        }else if(x<=A+B+C+D){
            return 3;
        }else if(x<=A+B+C+D+E){
            return 4;
        }else{
            return 5;
        }
    }





}
