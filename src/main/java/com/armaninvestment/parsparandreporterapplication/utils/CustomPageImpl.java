package com.armaninvestment.parsparandreporterapplication.utils;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@Setter
@Getter
public class CustomPageImpl<T> extends PageImpl<T> {

    private Double overallTotalQuantity;
    private Double overallTotalPrice;
    private Double overallTotalAmount;

    public CustomPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
    public CustomPageImpl(Set<T> content, Pageable pageable, long total) {
        super(content.stream().toList(), pageable, total);
    }

    public CustomPageImpl(List<T> content, Pageable pageable, long total, Double overallTotalQuantity, Double overallTotalPrice) {
        super(content, pageable, total);
        this.overallTotalQuantity = overallTotalQuantity;
        this.overallTotalPrice = overallTotalPrice;
    }


}
