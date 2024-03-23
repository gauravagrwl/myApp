package org.gauravagrwl.myApp.helper;

public enum ExchangeEnum {

    NYSE("USD"),
    NSE("INR"),
    CRYPTO("USD"),
    ;

    private final String exchange_currency;

    ExchangeEnum(String exchangeCurrency) {
        exchange_currency = exchangeCurrency;
    }
}
