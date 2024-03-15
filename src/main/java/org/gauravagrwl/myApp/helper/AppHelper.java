package org.gauravagrwl.myApp.helper;

import org.apache.commons.lang3.StringUtils;

public class AppHelper {

    public static final String UNDER_SCORE = "_";

    public static String prependAccountNumber(String accountNumber) {
        return StringUtils.join("XXXX", StringUtils.right(accountNumber, 4));
    }

    public static String getStatementCollectionName(String... elements) {
        return StringUtils.join(elements, UNDER_SCORE).concat("_statementCollection");
    }

    public static String getLedgerCollectionName(String... elements) {
        return StringUtils.join(elements, UNDER_SCORE).concat("_ledgerDocument");
    }

}
