package edu.cmu.sei.mtdigraph;

public abstract class LogBuilder {
    Object[] parms;

    String category="";

    public String getCategory(){
        return category;
    }

    public void setCategory(String c){
        category = c;
    }

    public LogBuilder(Object[] p){
        parms = p;
    }

    public LogBuilder(Object[] p, String category){
        parms = p;
        setCategory(category);
    };

    public abstract String buildLog();
}
