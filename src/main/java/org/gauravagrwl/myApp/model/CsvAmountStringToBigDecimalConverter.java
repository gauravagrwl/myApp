package org.gauravagrwl.myApp.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class CsvAmountStringToBigDecimalConverter extends AbstractBeanField<String, BigDecimal> {

    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        value = value.replaceAll("[^-1234567890.]", "");
        if (StringUtils.isEmpty(value) || StringUtils.equals("-", value)) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(value);
        }
    }

}
