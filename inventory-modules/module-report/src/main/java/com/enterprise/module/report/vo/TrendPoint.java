package com.enterprise.module.report.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendPoint(LocalDate date, BigDecimal purchaseAmount, BigDecimal saleAmount) {}
