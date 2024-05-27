package com.armaninvestment.parsparandreporterapplication.searchForms;


import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Invoice}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceSearch implements Serializable {
    private Long id;
    private Long invoiceNumber;
    private LocalDate issuedDate;
    private SalesType salesType;
    private String customerName;
    private String invoiceStatusName;
    private Integer jalaliYear;

}