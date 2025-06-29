package com.awwwsl.worldmarketplace.api;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MarketItem {
    private final BigDecimal basePrice;
    private BigDecimal offset;
    private final BigDecimal volume;
    private BigDecimal traded; // + for buying from market, - for selling to market
    // 0-1
    private final BigDecimal tau;

    private static final MathContext MATH_CTX = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal PRECISION = new BigDecimal("0.001");

    public MarketItem(BigDecimal basePrice, BigDecimal volume, BigDecimal tau) {
        this.basePrice = basePrice;
        this.offset = BigDecimal.ZERO;
        this.volume = volume;
        this.tau = tau;
        this.traded = BigDecimal.ZERO;
    }

    public void setOffset(@NotNull BigDecimal offset) {
        this.offset = offset;
    }

    public void sell(@NotNull BigDecimal quantity) {
        if (quantity.signum() == 0) return; // No trade if quantity is zero
        this.traded = this.traded.subtract(quantity, MATH_CTX);
    }
    public void buy(@NotNull BigDecimal quantity) {
        if (quantity.signum() == 0) return; // No trade if quantity is zero
        this.traded = this.traded.add(quantity, MATH_CTX);
    }

    public BigDecimal getPrice() {
        // price = (basePrice + offset) * e^(traded / volume)
        BigDecimal baseWithOffset = this.basePrice.add(this.offset);
        BigDecimal exponent = this.traded.divide(this.volume, MATH_CTX);
        BigDecimal price = baseWithOffset.multiply(BigDecimal.valueOf(Math.exp(exponent.doubleValue())), MATH_CTX);
        return price.setScale(4, RoundingMode.HALF_EVEN);
    }

    public BigDecimal getTotalPrice(BigDecimal quantity) {
        BigDecimal baseWithOffset = this.basePrice.add(this.offset);

        BigDecimal exponentStart = this.traded.divide(this.volume, MATH_CTX);
        BigDecimal exponentEnd = this.traded.add(quantity).divide(this.volume, MATH_CTX);

        double eStart = Math.exp(exponentStart.doubleValue());
        double eEnd = Math.exp(exponentEnd.doubleValue());

        BigDecimal integral = baseWithOffset
                .multiply(this.volume, MATH_CTX)
                .multiply(BigDecimal.valueOf(eEnd - eStart), MATH_CTX);

        return integral.setScale(4, RoundingMode.HALF_EVEN);
    }

    public void decay() {
        traded = traded.multiply(BigDecimal.ONE.subtract(tau, MATH_CTX), MATH_CTX);

        if (traded.abs().divide(this.volume, MATH_CTX).compareTo(PRECISION) < 0) {
            traded = BigDecimal.ZERO;
        }
    }

    public @NotNull  BigDecimal getTraded() {
        return traded;
    }
}
