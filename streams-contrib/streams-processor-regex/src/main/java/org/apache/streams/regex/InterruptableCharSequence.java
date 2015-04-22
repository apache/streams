package org.apache.streams.regex;

public class InterruptableCharSequence implements CharSequence {
    CharSequence sequence;

    public InterruptableCharSequence(CharSequence sequence) {
        super();
        this.sequence = sequence;
    }

    @Override
    public int length() {
        return sequence.length();
    }

    @Override
    public char charAt(int index) {
        if(Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("charAt method interrupted for InterruptableCharSequence.");
        }

        return sequence.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new InterruptableCharSequence(sequence.subSequence(start, end));
    }

    @Override
    public String toString() {
        return sequence.toString();
    }
}