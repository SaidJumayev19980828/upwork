package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class Product {
    public Attributes attributes;
    @JsonProperty("Id")
    public String id;
    @JsonProperty("Name")
    public String name;
    @JsonProperty("ProductCode")
    public String productCode;
    @JsonProperty("Item_No__c")
    public String itemNoC;
    @JsonProperty("Description")
    public String description;
    @JsonProperty("Stk_Unit__c")
    public String stockUnitC;
    @JsonProperty("Stk_Unit__r")
    public String stockUnitR;
    @JsonProperty("EItem_Name__c")
    public String englishItemName;
    @JsonProperty("Icon_Attachment_Id__c")
    public String iconAttachmentId;
    @JsonProperty("Arabic_Category__c")
    public String arabicCategory;
    @JsonProperty("Arabic_Class__c")
    public String arabicClass;
    @JsonProperty("Arabic_Color__c")
    public String arabicColor;
    @JsonProperty("Arabic_Cut__c")
    public String arabicCut;
    @JsonProperty("Arabic_Depth__c")
    public String arabicDepth;
    @JsonProperty("Arabic_Drain__c")
    public String arabicDrain;
    @JsonProperty("Arabic_Factory__c")
    public String arabicFactory;
    @JsonProperty("Arabic_Family__c")
    public String arabicFamily;
    @JsonProperty("Arabic_Glaze__c")
    public String arabicGlaze;
    @JsonProperty("Arabic_Mixer__c")
    public String arabicMixer;
    @JsonProperty("Arabic_Model__c")
    public String arabicModel;
    @JsonProperty("Arabic_Origin__c")
    public String arabicOrigin;
    @JsonProperty("Arabic_Shape__c")
    public String arabicShape;
    @JsonProperty("Arabic_Specifications__c")
    public String arabicSpecifications;
    @JsonProperty("Arabic_Style__c")
    public String arabicStyle;
    @JsonProperty("Arabic_Tank__c")
    public String arabicTank;
    @JsonProperty("Arabic_Texture__c")
    public String arabicTexture;
    @JsonProperty("Arabic_Type__c")
    public String arabicType;
    @JsonProperty("English_Category__c")
    public String englishCategory;
    @JsonProperty("English_Class__c")
    public String englishClass;
    @JsonProperty("English_Color__c")
    public String englishColor;
    @JsonProperty("English_Cut__c")
    public String englishCut;
    @JsonProperty("English_Depth__c")
    public String englishDepth;
    @JsonProperty("English_Drain__c")
    public String englishDrain;
    @JsonProperty("English_Factory__c")
    public String englishFactory;
    @JsonProperty("English_Family__c")
    public String englishFamily;
    @JsonProperty("English_Glaze__c")
    public String englishGlaze;
    @JsonProperty("English_Mixer__c")
    public String englishMixer;
    @JsonProperty("English_Model__c")
    public String englishModel;
    @JsonProperty("English_Origin__c")
    public String englishOrigin;
    @JsonProperty("English_Shape__c")
    public String englishShape;
    @JsonProperty("English_Specifications__c")
    public String englishSpecifications;
    @JsonProperty("English_Style__c")
    public String englishStyle;
    @JsonProperty("English_Tank__c")
    public String englishTank;
    @JsonProperty("English_Texture__c")
    public String englishTexture;
    @JsonProperty("English_Type__c")
    public String englishType;
    @JsonProperty("Category__c")
    public String category;
    @JsonProperty("Class__c")
    public String classC;
    @JsonProperty("Color__c")
    public String color;
    @JsonProperty("Cut__c")
    public String cut;
    @JsonProperty("Depth__c")
    public String depth;
    @JsonProperty("Drain__c")
    public String drain;
    @JsonProperty("Factory__c")
    public String factory;
    @JsonProperty("Family")
    public String family;
    @JsonProperty("Glaze__c")
    public String glaze;
    @JsonProperty("Mixer__c")
    public String mixer;
    @JsonProperty("Model__c")
    public String model;
    @JsonProperty("Origin__c")
    public String origin;
    @JsonProperty("Shape__c")
    public String shape;
    @JsonProperty("Specifications__c")
    public String specifications;
    @JsonProperty("Style__c")
    public String style;
    @JsonProperty("Tank__c")
    public String tank;
    @JsonProperty("Texture__c")
    public String texture;
    @JsonProperty("Type__c")
    public String type;
    @JsonProperty("Model_No__c")
    public Object modelNo;
    @JsonProperty("Size__c")
    public String size;
    @JsonProperty("Pack_Closing__c")
    public Double packClosing;
}
