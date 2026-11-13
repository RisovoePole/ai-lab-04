package org.example.Values;

import com.google.common.base.Function;

public enum Price {
    LOW,
    MEDIUM,
    HIGH;

    //in dollars
    public int min = 10;
    //in dollars
    public int max = 100;
    //function
    public Function<Float, Float> fun;
}
