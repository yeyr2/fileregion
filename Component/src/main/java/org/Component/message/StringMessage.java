package org.Component.message;

public class StringMessage extends Message {
    String string;
    Long start;

    public StringMessage(String s) {
        this.string = s;
        this.start = 0L;
    }

    public StringMessage(String s,Long start) {
        this.string = s;
        this.start = start;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getType() {
        return StringMessage;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }
}
