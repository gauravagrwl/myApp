package org.gauravagrwl.myApp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.AppHelper;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.ProfileDocument;
import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.accountTransaction.BankAccountTransactionDocument;
import org.gauravagrwl.myApp.model.accountTransaction.CashFlowTransactionDocument;
import org.gauravagrwl.myApp.model.repositories.AccountDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.ProfileDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.NonNull;

@Service
public class AccountAsyncService {

    MongoTemplate template;

    AccountDocumentRepository accountDocumentRepository;

    ProfileDocumentRepository profileDocumentRepository;

    public AccountAsyncService(MongoTemplate template, AccountDocumentRepository accountDocumentRepository,
            ProfileDocumentRepository profileDocumentRepository) {
        this.template = template;
        this.accountDocumentRepository = accountDocumentRepository;
        this.profileDocumentRepository = profileDocumentRepository;
    }

    Logger LOGGER = LoggerFactory.getLogger(AccountAsyncService.class);

    @Async
    public void calculateAccountTransactionBalance(AccountDocument accountDocument,
            List<BankAccountTransactionDocument> accountTransactionDocumentList) {
        if (!accountDocument.getIsBalanceCalculated()) {
            LOGGER.info("Processing Account Balance for account: "
                    + AppHelper.prependAccountNumber(accountDocument.getAccountNumber()));

            accountTransactionDocumentList.sort(BankAccountTransactionDocument.sortBankStatment);
            BigDecimal accountBalance = BigDecimal.ZERO;
            Long seqNum = 0L;

            for (BankAccountTransactionDocument transactionDocument : accountTransactionDocumentList) {
                seqNum = getNextSequenceNumber(seqNum);
                transactionDocument.setSno(seqNum);
                accountBalance = accountBalance.add(transactionDocument.getCredit())
                        .subtract(transactionDocument.getDebit());
                transactionDocument.setBalance(accountBalance);
                updateTransactionDocument(transactionDocument, accountDocument.getStatementCollectionName());

                if (!AccountTypeEnum.CREDIT.equals(accountDocument.getAccountType())
                        && InstitutionCategoryEnum.BANKING.equals(accountDocument.getInstitutionCategory())) {
                    accountDocument.calculate(accountBalance);
                    accountDocument.setIsBalanceCalculated(Boolean.TRUE);
                    accountDocumentRepository.save(accountDocument);
                }
            }
        }
    }

    private void updateTransactionDocument(BankAccountTransactionDocument transactionDocument,
            @NonNull String accountTransactionCollectionName) {
        Query query = new Query(Criteria.where("id").is(transactionDocument.getId()));
        template.findAndReplace(query, transactionDocument, accountTransactionCollectionName);
    }

    private Long getNextSequenceNumber(Long initalValue) {
        return initalValue + 1;
    }

    @SuppressWarnings("null")
    @Async
    public void updateCashFlowDocuments(AccountDocument accountDocument,
            List<BankAccountTransactionDocument> accountTransactionDocumentList) {
        List<CashFlowTransactionDocument> cashFlowTransactions = new ArrayList<>();
        accountTransactionDocumentList
                .forEach(accountTransaction -> cashFlowTransactions.add(buildCashFlowTransaction(accountTransaction)));

        Map<Integer, List<CashFlowTransactionDocument>> cashFlowTransactionList = cashFlowTransactions.stream()
                .collect(Collectors
                        .groupingBy(cashFlowTransaction -> cashFlowTransaction.getTransactionDate().getYear()));

        ProfileDocument profileDocument = profileDocumentRepository.findById(accountDocument.getProfileDocumentId())
                .get();

        cashFlowTransactionList.keySet().forEach(key -> {
            updateCashFlowStatement(key, cashFlowTransactionList.get(key), profileDocument);
        });

        cashFlowTransactionList.size();
    }

    private void updateCashFlowStatement(Integer year, List<CashFlowTransactionDocument> cashFlowTransactionList,
            ProfileDocument profileDocument) {
        String collectionName = profileDocument.getUserName() + "_" + "cashflow_statement_" + year;
        profileDocument.getCashFlowDocumentCollectionSet().add(collectionName);
        profileDocumentRepository.save(profileDocument);

        cashFlowTransactionList.stream().forEach(s -> {
            template.save(s, collectionName);
        });
    }

    private CashFlowTransactionDocument buildCashFlowTransaction(BankAccountTransactionDocument accountTransaction) {
        CashFlowTransactionDocument cashFlowTransactionDocument = new CashFlowTransactionDocument();

        cashFlowTransactionDocument.setTransactionDate(accountTransaction.getTransactionDate());
        cashFlowTransactionDocument.setDescription(accountTransaction.getDescriptions());
        cashFlowTransactionDocument.setCashIn(accountTransaction.getCredit());
        cashFlowTransactionDocument.setCashOut(accountTransaction.getDebit());
        cashFlowTransactionDocument.setAccountTransactionId(accountTransaction.getId());
        if (cashFlowTransactionDocument.getCashIn().compareTo(BigDecimal.ZERO) > 0) {
            cashFlowTransactionDocument.setTransactionType("CashIn");
        } else {
            cashFlowTransactionDocument.setTransactionType("CashOut");
        }
        return cashFlowTransactionDocument;

    }
}
