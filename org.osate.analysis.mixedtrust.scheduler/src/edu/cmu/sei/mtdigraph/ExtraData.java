package edu.cmu.sei.mtdigraph;

public class ExtraData {

    public long length;
    public String styleClass;


    public ExtraData(long lengthMs, String styleClass) {
        super();
        this.length = lengthMs;
        this.styleClass = styleClass;
    }
    public long getLength() {
        return length;
    }
    public void setLength(long length) {
        this.length = length;
    }
    public String getStyleClass() {
        return styleClass;
    }
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


}
