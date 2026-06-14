package com.codingshuttle.razorpay.common.enums;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Embeddable
@AllArgsConstructor
public class Money {
    private int amountUnits;
    private String currency;

    public Money addMoney(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }

    public Money subtractMoney(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }

    public Money convertToCurrency(String targetCurrency, BigDecimal exchangeRate) {
        if (this.currency.equals(targetCurrency)) {
            return this;
        }
        int convertedAmountUnits = this.amountUnits * exchangeRate.intValue();
        return new Money(convertedAmountUnits, targetCurrency);
    }



}
