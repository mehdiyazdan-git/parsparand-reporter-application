package com.armaninvestment.parsparandreporterapplication.enums;

public enum SalesType {
    CASH_SALES("فروش نقدی"),             // "فروش نقدی" is passed to the constructor
    CONTRACTUAL_SALES("فروش قراردادی");  // "فروش قراردادی" is passed to the constructor

    private final String persianCaption;

    SalesType(String persianCaption) {
        this.persianCaption = persianCaption;  // Assigns the provided Persian caption to the enum constant
    }

    public String getPersianCaption() {
        return persianCaption;  // Returns the assigned Persian caption
    }
}

