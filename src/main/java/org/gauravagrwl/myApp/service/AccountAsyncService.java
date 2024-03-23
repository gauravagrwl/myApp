package org.gauravagrwl.myApp.service;

import java.math.BigDecimal;
import java.util.List;

import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.AppHelper;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.profileAccount.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.profileAccount.accountStatement.BankAccountStatementDocument;
import org.gauravagrwl.myApp.model.reports.CashFlowReportDocument;
import org.gauravagrwl.myApp.model.repositories.AccountDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.AccountStatementDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.CashFlowReportDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.ProfileDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AccountAsyncService {

    @Autowired
    MongoTemplate template;

    AccountDocumentRepository accountDocumentRepository;

    ProfileDocumentRepository profileDocumentRepository;
    CashFlowReportDocumentRepository cashFlowTransactionDocumentRepository;
    AccountStatementDocumentRepository accountStatementDocumentRepository;

    public AccountAsyncService(MongoTemplate template, AccountDocumentRepository accountDocumentRepository,
            ProfileDocumentRepository profileDocumentRepository,
            CashFlowReportDocumentRepository cashFlowTransactionDocumentRepository,
            AccountStatementDocumentRepository accountStatementDocumentRepository) {
        this.template = template;
        this.accountDocumentRepository = accountDocumentRepository;
        this.profileDocumentRepository = profileDocumentRepository;
        this.cashFlowTransactionDocumentRepository = cashFlowTransactionDocumentRepository;
        this.accountStatementDocumentRepository = accountStatementDocumentRepository;
    }

    Logger LOGGER = LoggerFactory.getLogger(AccountAsyncService.class);

    @Async
    public void calculateAccountStatementBalance(AccountDocument accountDocument,
            List<BankAccountStatementDocument> accountStatementDocumentList) {
        if (!accountDocument.getIsBalanceCalculated()) {
            LOGGER.info("Processing Account Balance for account: "
                    + AppHelper.prependAccountNumber(accountDocument.getAccountNumber()));

            // accountStatementDocumentList.sort(BankAccountStatementDocument.statementSort);
            BigDecimal accountBalance = BigDecimal.ZERO;

            for (BankAccountStatementDocument statementDocument : accountStatementDocumentList) {
                accountBalance = accountBalance.add(statementDocument.getCredit())
                        .subtract(statementDocument.getDebit());
                statementDocument.setBalance(accountBalance);
                accountStatementDocumentRepository.findAndUpdateStaementBalanceById(statementDocument.getId(),
                        accountBalance);

                if (!AccountTypeEnum.CREDIT.equals(accountDocument.getAccountType())
                        && InstitutionCategoryEnum.BANKING.equals(accountDocument.getInstitutionCategory())) {
                    accountDocument.calculate(accountBalance);
                    accountDocument.setIsBalanceCalculated(Boolean.TRUE);
                    accountDocumentRepository.save(accountDocument);
                }
            }
        }
    }

    @Async
    public void updateCashFlowDocuments(AccountDocument accountDocument,
            List<BankAccountStatementDocument> bankAccountStatementList) {
        bankAccountStatementList.forEach(statement -> {
            if (!statement.getReconciled()) {
                buildCashFlowTransaction(statement);
            }
        });

    }

    private void buildCashFlowTransaction(BankAccountStatementDocument accountStatement) {
        CashFlowReportDocument cashFlowTransactionDocument = new CashFlowReportDocument();

        cashFlowTransactionDocument.setTransactionDate(accountStatement.getTransactionDate());
        cashFlowTransactionDocument.setYear(accountStatement.getTransactionDate().getYear());
        cashFlowTransactionDocument.setDescription(accountStatement.getDescriptions());
        cashFlowTransactionDocument.setCashIn(accountStatement.getCredit());
        cashFlowTransactionDocument.setCashOut(accountStatement.getDebit());
        cashFlowTransactionDocument.setAccountStatementId(accountStatement.getId());
        if (cashFlowTransactionDocument.getCashIn().compareTo(BigDecimal.ZERO) > 0) {
            cashFlowTransactionDocument.setTransactionType("CashIn");
        } else {
            cashFlowTransactionDocument.setTransactionType("CashOut");
        }
        cashFlowTransactionDocumentRepository.save(cashFlowTransactionDocument);
        // accountStatement.setReconciled(Boolean.TRUE);
        accountStatementDocumentRepository.findAndUpdateReconcileById(accountStatement.getId(),
                Boolean.TRUE);

        // Query query = new Query(Criteria.where("id").is(accountStatement.getId()));
        // Update update = Update.update("reconciled", Boolean.TRUE);
        // template.updateFirst(query, update, BankAccountStatementDocument.class);
        // AccountStatementDocument accountStatementDocument =
        // accountStatementDocumentRepository
        // .findById(accountStatement.getId()).get();
        // accountStatementDocument.setReconciled(Boolean.TRUE);
        // accountStatementDocumentRepository.save(accountStatementDocument);

        // accountStatementDocumentRepository.save(accountStatement);
        // return cashFlowTransactionDocument;

    }
}
