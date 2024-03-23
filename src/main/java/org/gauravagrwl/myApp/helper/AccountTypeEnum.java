package org.gauravagrwl.myApp.helper;

import java.util.EnumSet;

public enum AccountTypeEnum {

	CHECKING(InstitutionCategoryEnum.BANKING, "CHK", "101"),
	SAVING(InstitutionCategoryEnum.BANKING, "SAV", "102"),
	DEPOSIT(InstitutionCategoryEnum.BANKING, "DEP", "103"),
	PPF(InstitutionCategoryEnum.BANKING, "PPF", "104"),
	CREDIT(InstitutionCategoryEnum.BANKING, "CRE", "105"),

	STOCK(InstitutionCategoryEnum.INVESTMENT, "STOCK", "201"),
	CRYPTO(InstitutionCategoryEnum.INVESTMENT, "CRYPTO", "202"),

	PROPERTY(InstitutionCategoryEnum.ASSETS, "PROPERTY", "301"),

	LOAN(InstitutionCategoryEnum.LOAN, "LOAN", "401"),
	;

	private final InstitutionCategoryEnum accountCategory;
	private final String accountTypeName, accountTypeCode;

	/**
	 * 
	 * @param accountCategory
	 * @param accountTypeName
	 * @param accountTypeCode
	 */
	private AccountTypeEnum(InstitutionCategoryEnum accountCategory, String accountTypeName, String accountTypeCode) {
		this.accountCategory = accountCategory;
		this.accountTypeName = accountTypeName;
		this.accountTypeCode = accountTypeCode;
	}

	public InstitutionCategoryEnum getAccountCategory() {
		return accountCategory;
	}

	public String getAccountTypeName() {
		return accountTypeName;
	}

	public String getAccountTypeCode() {
		return accountTypeCode;
	}

	public EnumSet<AccountTypeEnum> BankingEnumSet() {
		return EnumSet.of(AccountTypeEnum.CHECKING, AccountTypeEnum.SAVING, AccountTypeEnum.DEPOSIT,
				AccountTypeEnum.PPF, AccountTypeEnum.CREDIT);
	}

}
