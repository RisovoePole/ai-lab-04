package org.example.Values;

import com.google.common.base.Function;

public enum Temperature {
    LOW,
    HIGH;

    public int min = -10;
    public int max = 40;
    //function
    public Function<Float, Float> fun;
}
