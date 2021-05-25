package com.nasnav.enumerations;

public enum FileType {

    XLSX("xlsx"), CSV("csv");

    private String value ;

    FileType(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
