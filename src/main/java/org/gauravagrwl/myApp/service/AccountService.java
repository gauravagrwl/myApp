package org.gauravagrwl.myApp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gauravagrwl.myApp.exception.AppException;
import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.AppHelper;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.AccountDocument;
import org.gauravagrwl.myApp.model.AccountTransactionDocument;
import org.gauravagrwl.myApp.model.repositories.AccountDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.AccountTransactionDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

@Service
public class AccountService {

    Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    private AccountDocumentRepository accountDocumentRepository;

    private MongoTemplate template;

    private ProfileService profileService;

    private Query getDuplicateTransactionQuery(AccountTransactionDocument t) {
        Query query = new Query(
                Criteria.where("transactionDate").is(t.getTransactionDate()).and("descriptions")
                        .is(t.getDescriptions()).and("type").is(t.getType()).and("debit").is(t.getDebit())
                        .and("credit")
                        .is(t.getCredit()));
        return query;
    }

    private Query findById(@NonNull String id) {
        Query query = new Query(
                Criteria.where("id").is(id));
        return query;

    }

    public AccountService(AccountDocumentRepository accountDocumentRepository, ProfileService profileService,
            AccountTransactionDocumentRepository accountTransactionDocumentRepository, MongoTemplate template) {
        this.accountDocumentRepository = accountDocumentRepository;
        this.profileService = profileService;
        this.template = template;
    }

    public String addUserAccount(AccountDocument accountDocument, String userName) {

        String profileId = profileService.getProfileDocument(userName).getId();
        accountDocument.setProfileDocumentId(profileId);
        accountDocument.setStatementCollectionName(
                AppHelper.getCollectionName(accountDocument.getAccountNumber(), "Account_Statement"));
        AccountDocument save = accountDocumentRepository.save(accountDocument);

        return save.getId();

    }

    public List<AccountDocument> getUserAccounts(String userName) {
        String profileId = profileService.getProfileDocument(userName).getId();
        return accountDocumentRepository.findByProfileDocumentId(profileId);
    }

    public void toggleAccountActiveStatus(String userName, @NotNull String accountId) {
        if (accountId != null && isUserAccountExist(accountId, userName)) {
            AccountDocument document = accountDocumentRepository.findById(accountId).get();
            document.setIsActive(!document.getIsActive());
            accountDocumentRepository.save(document);
        }
        throw new AppException("Account Id is null or not exist.");

    }

    public boolean isUserAccountExist(String accountId, String userName) {
        String profileId = profileService.getProfileDocument(userName).getId();
        return accountDocumentRepository.existsByIdAndProfileDocumentId(accountId,
                profileId);
    }

    public AccountDocument getAccountDocument(String accountId, String userName) {
        if (accountId != null && isUserAccountExist(accountId, userName)) {
            return accountDocumentRepository.findById(accountId).get();
        }
        throw new AppException("Account Id is null or not exist.");

    }

    @SuppressWarnings("null")
    public void processAccountTransactions(List<AccountTransactionDocument> transactionList,
            @NonNull AccountDocument accountDocument) {
        String transactionCollectionName = accountDocument.getStatementCollectionName();
        transactionList.stream().forEach(
                t -> {
                    Query query = getDuplicateTransactionQuery(t);
                    Update update = Update.update("duplicate", Boolean.TRUE);
                    UpdateResult updateMultiResult = template.updateMulti(query, update,
                            AccountTransactionDocument.class, transactionCollectionName);
                    if (updateMultiResult.getMatchedCount() > 0) {
                        LOGGER.warn("Total Duplicate Records found: " + updateMultiResult.getMatchedCount()
                                + "and total updated records are: " + updateMultiResult.getModifiedCount());
                        t.setDuplicate(Boolean.TRUE);
                    }
                    template.save(t, transactionCollectionName);
                });
    }

    public List<AccountTransactionDocument> getAccountTransactionDocuments(@NonNull AccountDocument accountDocument) {
        List<AccountTransactionDocument> transactionList = new ArrayList<>();
        String collectionName = accountDocument.getStatementCollectionName();
        if (StringUtils.isNotBlank(collectionName)) {
            transactionList.addAll(template.findAll(AccountTransactionDocument.class, collectionName));
        }
        transactionList.sort(AccountTransactionDocument.sortBankStatment);
        return transactionList;
    }

    @Async
    public void calculateAccountTransactionBalance(AccountDocument accountDocument) {
        if (!accountDocument.getIsBalanceCalculated()) {
            LOGGER.info("Processing Account Balance for account: "
                    + AppHelper.prependAccountNumber(accountDocument.getAccountNumber()));
            List<AccountTransactionDocument> accountTransactionDocumentList = getAccountTransactionDocuments(
                    accountDocument);
            accountTransactionDocumentList.sort(AccountTransactionDocument.sortBankStatment);
            BigDecimal accountBalance = BigDecimal.ZERO;
            Long seqNum = 0L;

            for (AccountTransactionDocument transactionDocument : accountTransactionDocumentList) {
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

    private void updateTransactionDocument(AccountTransactionDocument transactionDocument,
            @NonNull String accountTransactionCollectionName) {
        Query query = new Query(Criteria.where("id").is(transactionDocument.getId()));
        template.findAndReplace(query, transactionDocument, accountTransactionCollectionName);
    }

    private Long getNextSequenceNumber(Long initalValue) {
        return initalValue + 1;
    }

    @SuppressWarnings("null")
    public Boolean deleteAccountTransaction(AccountDocument accountDocument, @NonNull String transactionId) {
        String accountTransactionCollectionName = accountDocument.getStatementCollectionName();

        Query findById = findById(transactionId);
        AccountTransactionDocument one = template.findOne(findById, AccountTransactionDocument.class,
                accountTransactionCollectionName);
        DeleteResult remove = template.remove(findById(transactionId), AccountTransactionDocument.class,
                accountTransactionCollectionName);

        Query findDuplicateQuery = getDuplicateTransactionQuery(one);
        long count = template.count(findDuplicateQuery, AccountTransactionDocument.class,
                accountTransactionCollectionName);
        if (count == 1) {
            Update updateDefination = Update.update("duplicate", Boolean.FALSE);
            template.findAndModify(findDuplicateQuery, updateDefination, AccountTransactionDocument.class,
                    accountTransactionCollectionName);
        }

        LOGGER.info("Account Transaction deleted for id: " + transactionId + "and Total delete count is: "
                + remove.getDeletedCount());
        accountDocument.setIsBalanceCalculated(Boolean.FALSE);
        accountDocumentRepository.save(accountDocument);
        calculateAccountTransactionBalance(accountDocument);
        return (remove.wasAcknowledged());

    }

}
