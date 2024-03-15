package org.gauravagrwl.myApp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gauravagrwl.myApp.exception.AppException;
import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.AppHelper;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.accountTransaction.BankAccountTransactionDocument;
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
    private AccountAsyncService accountAsyncService;

    private Query getDuplicateTransactionQuery(BankAccountTransactionDocument t) {
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
            AccountTransactionDocumentRepository accountTransactionDocumentRepository, MongoTemplate template,
            AccountAsyncService accountAsyncService) {
        this.accountDocumentRepository = accountDocumentRepository;
        this.profileService = profileService;
        this.template = template;
        this.accountAsyncService = accountAsyncService;
    }

    public String addUserAccount(AccountDocument accountDocument, String userName) {

        String profileId = profileService.getProfileDocument(userName).getId();
        accountDocument.setProfileDocumentId(profileId);
        accountDocument.setStatementCollectionName(
                AppHelper.getStatementCollectionName(accountDocument.getAccountNumber()));
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
    public void processAccountTransactions(List<BankAccountTransactionDocument> transactionList,
            @NonNull AccountDocument accountDocument) {
        String transactionCollectionName = accountDocument.getStatementCollectionName();
        transactionList.stream().forEach(
                t -> {
                    Query query = getDuplicateTransactionQuery(t);
                    Update update = Update.update("duplicate", Boolean.TRUE);
                    UpdateResult updateMultiResult = template.updateMulti(query, update,
                            BankAccountTransactionDocument.class, transactionCollectionName);
                    if (updateMultiResult.getMatchedCount() > 0) {
                        LOGGER.warn("Total Duplicate Records found: " + updateMultiResult.getMatchedCount()
                                + "and total updated records are: " + updateMultiResult.getModifiedCount());
                        t.setDuplicate(Boolean.TRUE);
                    }
                    template.save(t, transactionCollectionName);
                });
        performAccountProcessing(accountDocument);
    }

    @Async
    public List<BankAccountTransactionDocument> getAccountTransactionDocuments(
            @NonNull AccountDocument accountDocument) {
        List<BankAccountTransactionDocument> transactionList = new ArrayList<>();
        String collectionName = accountDocument.getStatementCollectionName();
        if (StringUtils.isNotBlank(collectionName)) {
            transactionList.addAll(template.findAll(BankAccountTransactionDocument.class, collectionName));
        }
        transactionList.sort(BankAccountTransactionDocument.sortBankStatment);
        return transactionList;
    }

    @SuppressWarnings("null")
    public Boolean deleteAccountTransaction(AccountDocument accountDocument, @NonNull String transactionId) {
        String accountTransactionCollectionName = accountDocument.getStatementCollectionName();

        Query findById = findById(transactionId);
        BankAccountTransactionDocument one = template.findOne(findById, BankAccountTransactionDocument.class,
                accountTransactionCollectionName);
        DeleteResult remove = template.remove(findById(transactionId), BankAccountTransactionDocument.class,
                accountTransactionCollectionName);

        Query findDuplicateQuery = getDuplicateTransactionQuery(one);
        long count = template.count(findDuplicateQuery, BankAccountTransactionDocument.class,
                accountTransactionCollectionName);
        if (count == 1) {
            Update updateDefination = Update.update("duplicate", Boolean.FALSE);
            template.findAndModify(findDuplicateQuery, updateDefination, BankAccountTransactionDocument.class,
                    accountTransactionCollectionName);
        }

        LOGGER.info("Account Transaction deleted for id: " + transactionId + "and Total delete count is: "
                + remove.getDeletedCount());
        accountDocument.setIsBalanceCalculated(Boolean.FALSE);
        accountDocumentRepository.save(accountDocument);
        performAccountProcessing(accountDocument);
        return (remove.wasAcknowledged());

    }

    public void performAccountProcessing() {
        List<AccountDocument> profileAccountDocuments = new ArrayList<>();
        profileService.getAllProfileDocument()
                .forEach(profile -> profileAccountDocuments.addAll(getUserAccounts(profile.getUserName())));

        profileAccountDocuments.forEach(accountDocument -> performAccountProcessing(accountDocument));

    }

    // TODO:
    // Add filter for which all account balance can be calculated and be added
    // Balance calculation: Bank Account expect credit (primary account)
    // to cashflow statement for all cash in and cash out (primary account)
    @Async
    public void performAccountProcessing(AccountDocument accountDocument) {
        List<BankAccountTransactionDocument> accountTransactionDocuments = getAccountTransactionDocuments(
                accountDocument);
        if ((InstitutionCategoryEnum.BANKING.compareTo(accountDocument.getInstitutionCategory()) == 0) &&
                AccountTypeEnum.CREDIT.compareTo(accountDocument.getAccountType()) != 0) {
            accountAsyncService.calculateAccountTransactionBalance(accountDocument, accountTransactionDocuments);
        }
        if (InstitutionCategoryEnum.BANKING.compareTo(accountDocument.getInstitutionCategory()) == 0) {
            accountAsyncService.updateCashFlowDocuments(accountDocument, accountTransactionDocuments);
        }

    }

}
