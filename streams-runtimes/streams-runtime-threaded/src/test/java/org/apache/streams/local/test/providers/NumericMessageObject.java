package org.apache.streams.local.test.providers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class NumericMessageObject implements Serializable {

    private int number = 0;

    @JsonProperty("number")
    public int getNumber()          { return this.number; }
    public void setNumber(int val)  { this.number = val; }

    public NumericMessageObject() { /* default constructor */ }

    public NumericMessageObject(int number) {
        this.number = number;
    }

}
