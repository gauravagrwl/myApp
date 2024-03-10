package org.gauravagrwl.myApp.helper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class CsvDateStringToDateConverter extends AbstractBeanField<String, LocalDate> {
    // private static final DateTimeFormatter formatter =
    // DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy");

    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        return LocalDate.parse(value, formatter);

    }

}
