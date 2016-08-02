package org.cytoscape.viPEr.ui.datamodels;

/**
 * Classdefinition for Resultentries
 * @author MGarmhausen
 */
public class ResultEntry {
    private float score;
    private String path;
    private String pathWithNames;
    private int pathLength;
    
    public float getScore(){
        return score;
    }
    
    public String getPath(){
        return path;
    }
    
    public String getPathWithNames(){
        return pathWithNames;
    }
    
    public int getPathLength(){
        return pathLength;
    }
    
    public void setScore(float value){
        this.score = value;
    }
    
    public void setPath(String text){
        this.path = text;
    }
    
    public void setPathWithNames(String text){
        this.pathWithNames = text;
    }
    
    public void setPathLength(int value){
        this.pathLength = value;
    }
    
    public ResultEntry(float value, String text, int length, String namesText){
        this.path = text;
        this.score = value;
        this.pathLength = length;
        this.pathWithNames = namesText;
    }
}
