package org.gauravagrwl.myApp.model.profileAccount.accountLedger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentAccountTransactionLedgerDocument {
    // Robinhood: Activity Date Process Date Settle Date Instrument Description
    // Trans Code Quantity Price Amount

    // Crypto: Timestamp (UTC), Transaction Description, Currency, Amount, To
    // Currency,
    // To Amount, Native Currency, Native Amount, Native Amount (in USD),
    // Transaction
    // Kind, Transaction Hash,
    // Coinbase: Timestamp, Transaction Type, Asset, Quantity Transacted, Spot Price
    // Currency, Spot Price at Transaction, Subtotal, Total (inclusive of fees
    // and/or
    // spread), Fees and/or Spread, Notes

    private LocalDateTime timestamp; // common in crypto and coinbase
    private String cryptoapp_transaction_description;
    private String cryptoapp_from_crypto_Coin;
    private BigDecimal cryptoapp_from_crypto_Coin_qty;
    private String cryptoapp_to_crypto_Coin;
    private BigDecimal cryptoapp_to_crypto_Coin_qty;
    private Currency cryptoapp_nativeCurrency = Currency.getInstance("USD");
    private BigDecimal cryptoapp_currency_amount;
    private String cryptoapp_transaction_kind;
    private String cryptoapp_transaction_hash;

    private String coinbase_transaction_type;
    private String coinbase_asset;
    private String coinbase_quantity_transacted;
    private Currency coinbase_spot_price_currency = Currency.getInstance("USD");
    private BigDecimal coinbase_spot_price_at_transaction;
    private BigDecimal coinbase_subtotal;
    private BigDecimal coinbase_total_inclusive_fees;
    private BigDecimal coinbase_fees;
    private String coinbase_notes;

}
