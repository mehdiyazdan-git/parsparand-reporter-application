package com.armaninvestment.parsparandreporterapplication.enums;

public enum ProductType {
    MAIN(2, "اصلی"),
    SCRAPT(6, "ضایعات"),
    RAWMATERIAL(1, "مواد اولیه");

    private final int value;
    private final String caption;

    ProductType(int value, String caption) {
        this.value = value;
        this.caption = caption;
    }

    public int getValue() {
        return value;
    }

    public String getCaption() {
        return caption;
    }
}

