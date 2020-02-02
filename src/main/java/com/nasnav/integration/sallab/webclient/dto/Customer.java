package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Customer {

    private Attributes attributes;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("IsDeleted")
    private boolean isDeleted;

    @JsonProperty("MasterRecordId")
    private String masterRecordId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("ParentId")
    private String parentId;

    @JsonProperty("BillingStreet")
    private String billingStreet;

    @JsonProperty("BillingCity")
    private String billingCity;

    @JsonProperty("BillingState")
    private String billingState;

    @JsonProperty("BillingPostalCode")
    private String billingPostalCode;

    @JsonProperty("BillingCountry")
    private String billingCountry;

    @JsonProperty("BillingLatitude")
    private String billingLatitude;

    @JsonProperty("BillingLongitude")
    private String billingLongitude;

    @JsonProperty("BillingGeocodeAccuracy")
    private String billingGeocodeAccuracy;

    @JsonProperty("BillingAddress")
    private String billingAddress;

    @JsonProperty("ShippingStreet")
    private String shippingStreet;

    @JsonProperty("ShippingCity")
    private String shippingCity;

    @JsonProperty("ShippingState")
    private String shippingState;

    @JsonProperty("ShippingPostalCode")
    private String shippingPostalCode;

    @JsonProperty("ShippingCountry")
    private String shippingCountry;

    @JsonProperty("ShippingLatitude")
    private String shippingLatitude;

    @JsonProperty("ShippingLongitude")
    private String shippingLongitude;

    @JsonProperty("ShippingGeocodeAccuracy")
    private String shippingGeocodeAccuracy;

    @JsonProperty("ShippingAddress")
    private String shippingAddress;

    @JsonProperty("Phone")
    private String Phone;

    @JsonProperty("Fax")
    private String fax;

    @JsonProperty("Website")
    private String website;

    @JsonProperty("PhotoUrl")
    private String photoUrl;

    @JsonProperty("Industry")
    private String industry;

    @JsonProperty("NumberOfEmployees")
    private String numberOfEmployees;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Rating")
    private String rating;

    @JsonProperty("OwnerId")
    private String ownerId;

    @JsonProperty("CreatedDate")
    private String createdDate;

    @JsonProperty("CreatedById")
    private String createdById;

    @JsonProperty("LastModifiedDate")
    private String lastModifiedDate;

    @JsonProperty("LastModifiedById")
    private String lastModifiedById;

    @JsonProperty("SystemModstamp")
    private String systemModstamp;

    @JsonProperty("LastActivityDate")
    private LocalDateTime lastActivityDate;

    @JsonProperty("LastViewedDate")
    private String lastViewedDate;

    @JsonProperty("LastReferencedDate")
    private String
            lastReferencedDate;

    @JsonProperty("JigsawCompanyId")
    private String jigsawCompanyId;

    @JsonProperty("ECus_Name__c")
    private String EnglishCustomerName;

    @JsonProperty("Cust_Type__c")
    private String customerType;

    @JsonProperty("Sal_Area__c")
    private String salArea;

    @JsonProperty("Area_Code__c")
    private Double areaCode;

    @JsonProperty("Cust_Type_No__c")
    private Double customerTypeNumber;

    @JsonProperty("Cus_Address__c")
    private String customerAddress;

    @JsonProperty("Cus_Mobile__c")
    private String customerMobile ;

    @JsonProperty("Cus_Email__c")
    private String customerEmail;

    @JsonProperty("Notes__c")
    private String notes;

    @JsonProperty("CUS_DESC__c")
    private String customerDescription;

    @JsonProperty("Cus_No__c")
    private String customerNumber;

    @JsonProperty("Acc_No__c")
    private String accountNumber;

    @JsonProperty("Rev_Acc__c")
    private String revertAccount;

    @JsonProperty("Discount_Acc__c")
    private String discountAccount;

    @JsonProperty("Tax2_Acc__c")
    private String taxAccount;

    @JsonProperty("Sal_Tax_Acc__c")
    private String salTaxAccount;

    @JsonProperty("Norm_Stamp_Acc__c")
    private String normStampAccount;

    @JsonProperty("Work_Acc__c")
    private String workAccount;

    @JsonProperty("Stamp_Add_Acc__c")
    private String stampAddAccount;

    @JsonProperty("Ret_Cus_Acc__c")
    private String returnCustomerAccount;

    @JsonProperty("Sal_Int_Acc__c")
    private String salIntAccount;

    @JsonProperty("Down_Pay_Acc__c")
    private String downPayAccount;

    @JsonProperty("Penalty_Acc__c")
    private String penaltyAccount;

    @JsonProperty("Amanat_Acc__c")
    private String amanatAccount;

    @JsonProperty("Tashuinat_Acc__c")
    private String tashuinatAccount;

    @JsonProperty("Cus_Cr_Limit__c")
    private Double customerCrLimit;

    @JsonProperty("Cust_Flg__c")
    private Double customerFlag;

    @JsonProperty("Must_Have_Cus_No__c")
    private boolean mustHaveCustomerNnumber;

    @JsonProperty("customer_points__c")
    private String customerPoints;

    @JsonProperty("Birth_Day__c")
    private String birthDay;

    @JsonProperty("Birth_Month__c")
    private String birthMonth;

    @JsonProperty("Use_Consumer_Sales_Order__c")
    private boolean useConsumerSalesOrder;

    @JsonProperty("Sales_Order_Total__c")
    private float salesOrderTotal;

    @JsonProperty("Category__c")
    private String category;

    @JsonProperty("Redeem_value__c")
    private String redeemValue;

    @JsonProperty("Customer_Serial__c")
    private String customerSerial;

    @JsonProperty("value_delivered__c")
    private String valueDelivered;

    @JsonProperty("A_Reward__c")
    private Double aReward;

    @JsonProperty("Category_A_Changed__c")
    private boolean categoryAChanged;

    @JsonProperty("Category_A_Difference__c")
    private String CategoryADifference;

    @JsonProperty("Category_B_Changed__c")
    private boolean Category_B_Changed;

    @JsonProperty("Category_B_Difference__c")
    private String categoryBDifference;

    @JsonProperty("Category_B_Points_Reward__c")
    private Double categoryBPointsReward;

    @JsonProperty("Category_C_Changed__c")
    private boolean categoryCChanged;

    @JsonProperty("Category_C_Difference__c")
    private String categoryCDifference;

    @JsonProperty("Category_C_Points_Reward__c")
    private Double categoryCPointsReward;

    @JsonProperty("Category_D_Changed__c")
    private boolean categoryDChanged;

    @JsonProperty("Category_D_Difference__c")
    private String categoryDDifference;

    @JsonProperty("Category_D_Points_Reward__c")
    private Double categoryDPoints_Reward;

    @JsonProperty("Category_E_Changed__c")
    private boolean categoryEChanged;

    @JsonProperty("Category_E_Difference__c")
    private String categoryEDifference;

    @JsonProperty("Category_E_Points_Reward__c")
    private Double categoryEPoints_Reward;

    @JsonProperty("Total_Reward_Points__c")
    private Double totalRewardPoints;

    @JsonProperty("Sales_Order_Total_All_Years__c")
    private Double salesOrderTotalAllYears;

    @JsonProperty("Total_Amount_Used_by_Rewards__c")
    private Double totalAmountUsed_byRewards;

    @JsonProperty("Recalculate_Points__c")
    private boolean recalculatePoints;

    @JsonProperty("Remaining_Reward_Points__c")
    private Double remainingRewardPoints;

}
