package com.rentflow.service;

import com.rentflow.dto.response.MpesaPaymentResponse;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface MpesaService {

    MpesaPaymentResponse initiateStkPush(String phoneNumber,
                                         BigDecimal amount,
                                         String accountReference,
                                         String transactionDesc,
                                         UUID tenantId,
                                         UUID invoiceId,
                                         UUID unitId,
                                         String reference);

    void processCallback(Map<String, Object> callbackPayload);
}
