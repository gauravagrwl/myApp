package org.gauravagrwl.myApp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.gauravagrwl.myApp.exception.AppException;
import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.AppHelper;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.accountStatement.AccountStatementDocument;
import org.gauravagrwl.myApp.model.accountTransaction.BankAccountStatementDocument;
import org.gauravagrwl.myApp.model.reports.CashFlowTransactionDocument;
import org.gauravagrwl.myApp.model.repositories.AccountDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.AccountStatementDocumentRepository;
import org.gauravagrwl.myApp.model.repositories.CashFlowTransactionDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.DeleteResult;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

@Service
public class AccountService {

    Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    private AccountDocumentRepository accountDocumentRepository;

    private AccountStatementDocumentRepository accountStatementDocumentRepository;

    private ProfileService profileService;
    private AccountAsyncService accountAsyncService;

    @Autowired
    CashFlowTransactionDocumentRepository cashFlowTransactionDocumentRepository;

    public AccountService(AccountDocumentRepository accountDocumentRepository, ProfileService profileService,
            AccountStatementDocumentRepository accountTransactionDocumentRepository,
            AccountAsyncService accountAsyncService,
            AccountStatementDocumentRepository accountStatementDocumentRepository) {
        this.accountDocumentRepository = accountDocumentRepository;
        this.profileService = profileService;
        this.accountAsyncService = accountAsyncService;
        this.accountStatementDocumentRepository = accountStatementDocumentRepository;
    }

    public String addUserAccount(AccountDocument accountDocument, String userName) {

        String profileId = profileService.getProfileDocument(userName).getId();
        accountDocument.setProfileDocumentId(profileId);
        // accountDocument.setStatementCollectionName(
        // AppHelper.getStatementCollectionName(accountDocument.getAccountNumber()));
        AccountDocument save = accountDocumentRepository.save(accountDocument);

        return save.getId();

    }

    public List<AccountDocument> getUserAccounts(String userName) {
        String profileId = profileService.getProfileDocument(userName).getId();
        List<AccountDocument> byProfileDocumentId = accountDocumentRepository.findByProfileDocumentId(profileId);
        return byProfileDocumentId;
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
    public void processAccountStatements(List<? extends AccountStatementDocument> accountStatementList,
            @NonNull AccountDocument accountDocument) {
        accountStatementList.forEach(statement -> {
            List<AccountStatementDocument> duplicateStatementList = findAllByStatementDocument(statement);
            if (duplicateStatementList.size() > 0) {
                duplicateStatementList.forEach(
                        s -> accountStatementDocumentRepository.findAndUpdateDuplicateById(s.getId(), Boolean.TRUE));
                statement.setDuplicate(Boolean.TRUE);
            }
            accountStatementDocumentRepository.insert(statement);
        });
        performAccountProcessing(accountDocument);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<AccountStatementDocument> findAllByStatementDocument(
            @NonNull AccountStatementDocument statementDocument) {
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("id", "reconciled",
                "duplicate", "audit", "balance", "version");
        Example<AccountStatementDocument> example = Example.of(statementDocument, matcher);
        return (List) accountStatementDocumentRepository.findAll(example);
    }

    public List<? extends AccountStatementDocument> getAccountStatementDocuments(
            @NonNull AccountDocument accountDocument) {
        List<AccountStatementDocument> accountStatementList = accountStatementDocumentRepository
                .findByAccountDocumentId(accountDocument.getId());

        return accountStatementList;

    }

    public Boolean deleteAccountStatementDocument(AccountDocument accountDocument, @NonNull String accountStatementId) {
        AccountStatementDocument statement = accountStatementDocumentRepository.findById(accountStatementId).get();

        if (accountStatementDocumentRepository.existsById(accountStatementId)) {
            deleteCashFlowDocument(accountStatementId);
            accountStatementDocumentRepository.deleteById(accountStatementId);
            LOGGER.info("Account Statment deleted for id: " + accountStatementId
                    + !accountStatementDocumentRepository.existsById(accountStatementId));
        }
        List<AccountStatementDocument> duplicateStatementList = findAllByStatementDocument(statement);
        if (duplicateStatementList.size() == 1) {
            accountStatementDocumentRepository.findAndUpdateDuplicateById(duplicateStatementList.get(0).getId(),
                    Boolean.FALSE);
        }
        accountDocument.setIsBalanceCalculated(Boolean.FALSE);
        accountDocumentRepository.save(accountDocument);
        performAccountProcessing(accountDocument);
        return true;
    }

    private boolean deleteCashFlowDocument(@NonNull String accountStatementId) {
        if (cashFlowTransactionDocumentRepository.existsByAccountStatementId(accountStatementId)) {
            LOGGER.info("CashFlow Transaction deleted for id: " + accountStatementId + "and Total delete count is: "
                    + cashFlowTransactionDocumentRepository.deleteByAccountStatementId(accountStatementId));
            return true;
        }
        return false;
    }

    // @Scheduled(cron = "${updateCashFlowStatement}")
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
    @SuppressWarnings("unchecked")
    private void performAccountProcessing(AccountDocument accountDocument) {
        if (InstitutionCategoryEnum.BANKING.compareTo(accountDocument.getInstitutionCategory()) == 0) {
            List<BankAccountStatementDocument> bankAccountStatementList = (List<BankAccountStatementDocument>) getAccountStatementDocuments(
                    accountDocument);
            if (AccountTypeEnum.CREDIT.compareTo(accountDocument.getAccountType()) != 0) {
                accountAsyncService.calculateAccountStatementBalance(accountDocument,
                        bankAccountStatementList);
            }

            accountAsyncService.updateCashFlowDocuments(accountDocument,
                    bankAccountStatementList);
        }
    }
}
