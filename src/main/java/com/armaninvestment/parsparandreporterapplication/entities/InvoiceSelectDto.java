package com.armaninvestment.parsparandreporterapplication.entities;

import lombok.Data;

import java.io.Serializable;


@Data
public class InvoiceSelectDto implements Serializable {
    private Long id;
    private String name;

    public InvoiceSelectDto() {
    }

    public InvoiceSelectDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
