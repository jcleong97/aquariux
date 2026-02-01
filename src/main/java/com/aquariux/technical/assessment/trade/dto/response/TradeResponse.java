package com.aquariux.technical.assessment.trade.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class TradeResponse {
    // TODO: What should you return after a trade is executed?
    private Long tradeId;
    private String pairName;
    private String tradeType;      // "BUY" or "SELL"
    private BigDecimal quantity;
    private BigDecimal price;      // execution price
    private BigDecimal totalAmount;
    private LocalDateTime tradeTime;
    private String status;         // e.g. "SUCCESS"
}