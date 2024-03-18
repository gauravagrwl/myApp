package org.gauravagrwl.myApp.service;

import java.math.BigDecimal;
import java.util.List;

import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.AppHelper;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.ProfileDocument;
import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.accountTransaction.BankAccountStatementDocument;
import org.gauravagrwl.myApp.model.reports.CashFlowTransactionDocument;
import org.gauravagrwl.myApp.model.repositories.AccountDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.AccountStatementDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.CashFlowTransactionDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.ProfileDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.NonNull;

@Service
public class AccountAsyncService {

    @Autowired
    MongoTemplate template;

    AccountDocumentRepository accountDocumentRepository;

    ProfileDocumentRepository profileDocumentRepository;
    CashFlowTransactionDocumentRepository cashFlowTransactionDocumentRepository;
    AccountStatementDocumentRepository accountStatementDocumentRepository;

    public AccountAsyncService(MongoTemplate template, AccountDocumentRepository accountDocumentRepository,
            ProfileDocumentRepository profileDocumentRepository,
            CashFlowTransactionDocumentRepository cashFlowTransactionDocumentRepository,
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

            accountStatementDocumentList.sort(BankAccountStatementDocument.statementSort);
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

    private void updateTransactionDocument(BankAccountStatementDocument transactionDocument,
            @NonNull String accountTransactionCollectionName) {
        Query query = new Query(Criteria.where("id").is(transactionDocument.getId()));
        template.findAndReplace(query, transactionDocument, accountTransactionCollectionName);
    }

    private Long getNextSequenceNumber(Long initalValue) {
        return initalValue + 1;
    }

    @Async
    public void updateCashFlowDocuments(AccountDocument accountDocument,
            List<BankAccountStatementDocument> bankAccountStatementList) {
        bankAccountStatementList.forEach(statement -> {
            if (!statement.getReconciled()) {
                buildCashFlowTransaction(statement);
            }
        });

        // Map<Integer, List<CashFlowTransactionDocument>> cashFlowTransactionList =
        // cashFlowTransactions.stream()
        // .collect(Collectors
        // .groupingBy(cashFlowTransaction ->
        // cashFlowTransaction.getTransactionDate().getYear()));

        // ProfileDocument profileDocument =
        // profileDocumentRepository.findById(accountDocument.getProfileDocumentId())
        // .get();

        // cashFlowTransactionList.keySet().forEach(key -> {
        // updateCashFlowStatement(key, cashFlowTransactionList.get(key),
        // profileDocument);
        // });

        // cashFlowTransactionList.size();
    }

    private void updateCashFlowStatement(Integer year, List<CashFlowTransactionDocument> cashFlowTransactionList,
            ProfileDocument profileDocument) {
        String collectionName = profileDocument.getUserName() + "_" + "cashflow_statement_" + year;
        // profileDocument.getCashFlowDocumentCollectionSet().add(collectionName);
        profileDocumentRepository.save(profileDocument);

        cashFlowTransactionList.stream().forEach(s -> {
            template.save(s, collectionName);
        });
    }

    private void buildCashFlowTransaction(BankAccountStatementDocument accountStatement) {
        CashFlowTransactionDocument cashFlowTransactionDocument = new CashFlowTransactionDocument();

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
