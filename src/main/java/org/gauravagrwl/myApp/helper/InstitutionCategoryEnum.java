package org.gauravagrwl.myApp.helper;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum InstitutionCategoryEnum {
    BANKING("BankAccount", "1"), 
    INVESTMENT("InvestmentAccount", "2"), 
	ASSETS("AssetsAccount", "3"), 
    LOAN("LoanAccount", "4"),;
	
    
    private String categoryName;
	private String categoryCode;

	/**
	 * @param categoryName
	 * @param categoryCode
	 */
	private InstitutionCategoryEnum(String categoryName, String categoryCode) {
		this.categoryName = categoryName;
		this.categoryCode = categoryCode;
	}

	/**
	 * @param categoryName
	 * @param categoryCode
	 */
	private InstitutionCategoryEnum(String categoryName, Integer categoryCode) {
		this(categoryName, categoryCode.toString());
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	/**
     * 
     * @param value
     * @return
     */
    @JsonCreator
	public static InstitutionCategoryEnum find(String value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		for (InstitutionCategoryEnum c : values()) {
			if (value.equalsIgnoreCase(c.getCategoryName())) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
