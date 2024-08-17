package com.armaninvestment.parsparandreporterapplication.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link BaseFilter}
 */
@Value
public class BaseFilter implements Serializable {
    int paginationPage;
    int paginationSize;
    String sortingOrder;
    String sortingSortBy;
}