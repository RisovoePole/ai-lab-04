package org.example.Values;

import com.google.common.base.Function;

public enum Advertisement {
    HIGH,
    LOW;

    // in thousands
    public int min = 0;
    // in thousands
    public int max = 100;
    //function
    public Function<Float, Float> fun;
}
