package com.armaninvestment.parsparandreporterapplication.enums;

public enum AdjustmentType {
    POSITIVE("تعدیل مثبت"),
    NEGATIVE("تعدیل منفی");

    private final String persianCaption;

    AdjustmentType(String persianCaption) {
        this.persianCaption = persianCaption;
    }

    public String getPersianCaption() {
        return persianCaption;  // Returns the assigned Persian caption
    }
}

