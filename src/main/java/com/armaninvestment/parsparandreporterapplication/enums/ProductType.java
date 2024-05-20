package com.armaninvestment.parsparandreporterapplication.enums;

public enum ProductType {
    MAIN(2, "اصلی"),
    SCRAPT(6, "ضایعات"),
    RAWMATERIAL(1, "مواد اولیه");

    private final int value;
    private final String caption;

    ProductType(int value, String caption) {
        this.value = (value > 0) ? value : 2;
        this.caption =  (caption != null) ? caption : "اصلی";
    }

    public int getValue() {
        return value;
    }

    public String getCaption() {
        return caption;
    }

    public static ProductType fromCaption(String caption) {
        for (ProductType type : values()) {
            if (type.getCaption().equals(caption)) {
                return type;
            }
        }
        throw new IllegalArgumentException("مقدار نادرست برای ProductType: " + caption);
    }

    public static ProductType fromValue(int value) {
        for (ProductType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("مقدار نادرست برای ProductType: " + value);
    }
}
