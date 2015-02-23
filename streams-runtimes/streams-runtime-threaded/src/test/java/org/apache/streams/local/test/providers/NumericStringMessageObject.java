package org.apache.streams.local.test.providers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class NumericStringMessageObject implements Serializable {

    private String number = "Number: " + 0;

    @JsonProperty("number")
    public String getNumber()          { return this.number; }
    public void setNumber(String val)  { this.number = val; }

    public NumericStringMessageObject() { /* default constructor */ }

    public NumericStringMessageObject(int number) {
        this.number = "Number: " + number;
    }
}