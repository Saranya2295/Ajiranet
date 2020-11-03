package com.example.AjiraNet.model;

/**
 * Model class for getting user parameters
 * @author Saranya Kumar
 */
public class Devices {

    public String type;
    public String name;
    public String value;
    public String source;
    public String[] targets;

    public String[] getTargets() {
        return targets;
    }

    public void setTargets(String[] targets) {
        this.targets = targets;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }

    public Devices(){

    }

    public Devices(String type, String name){
        this.type = type;
        this.name = name;
    }
}
