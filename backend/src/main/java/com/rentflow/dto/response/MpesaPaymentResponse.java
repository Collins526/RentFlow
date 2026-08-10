package com.rentflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MpesaPaymentResponse {
    private String checkoutRequestId;
    private String responseCode;
    private String responseDescription;
    private boolean success;
}
