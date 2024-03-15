package org.gauravagrwl.myApp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gauravagrwl.myApp.exception.AppException;
import org.gauravagrwl.myApp.helper.AccountTypeEnum;
import org.gauravagrwl.myApp.helper.AppHelper;
import org.gauravagrwl.myApp.helper.InstitutionCategoryEnum;
import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.accountTransaction.BankAccountTransactionDocument;
import org.gauravagrwl.myApp.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/profileAccount")
public class ProfileAccountController {

    AccountService accountService;

    Logger LOGGER = LoggerFactory.getLogger(ProfileAccountController.class);

    public ProfileAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 
     * @param userName
     * @param accountDocument
     * @return
     */
    @PostMapping(value = "/addAccount", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> addAccount(@RequestParam(required = false) String userName,
            @RequestBody AccountDocument accountDocument) {
        String userAccountId = accountService.addUserAccount(accountDocument, userName);
        return ResponseEntity.ok("Account Added with Id: " + userAccountId);
    }

    /**
     * 
     * @param userName
     * @param accountDocuments
     * @return
     */
    @PostMapping(value = "/addAccounts", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> addAccounts(@RequestParam(required = false) String userName,
            @RequestBody List<AccountDocument> accountDocuments) {
        Map<String, String> result = new HashMap<>();
        accountDocuments.forEach(accountDocument -> {
            try {
                String userAccountId = accountService.addUserAccount(accountDocument, userName);
                result.put(accountDocument.getAccountNumber(), "Account Added with Id: " + userAccountId);
            } catch (Exception e) {
                result.put(accountDocument.getAccountNumber(), "Account already exist.");
            }
        });
        return ResponseEntity.ok(result);
    }

    /**
     * 
     * @param userName
     * @param instCategory
     * @param accountType
     * @return
     */
    @GetMapping(value = "/getAccounts", produces = "application/json")
    public ResponseEntity<List<AccountDocument>> getProfileAccounts(
            @RequestParam(name = "userName", required = true) String userName,
            @RequestParam(name = "institutionCategory", required = false) InstitutionCategoryEnum instCategory,
            @RequestParam(name = "accountType", required = false) AccountTypeEnum accountType) {
        List<AccountDocument> userAccounts = accountService.getUserAccounts(userName);
        userAccounts.forEach(account -> account
                .setAccountNumber(AppHelper.prependAccountNumber(account.getAccountNumber())));
        return ResponseEntity.ok(userAccounts);
    }

    @GetMapping(value = "/getAccount", produces = "application/json")
    public ResponseEntity<AccountDocument> getProfileAccount(
            @RequestParam(name = "userName", required = true) String userName,
            @RequestParam(name = "accountId", required = true) String accountId) {
        AccountDocument accountDocument = accountService.getAccountDocument(accountId, userName);
        return ResponseEntity.ok(accountDocument);
    }

    /**
     * 
     * @param userName
     * @param accountId
     * @return
     */
    @PatchMapping(value = "/toggleAccountActive")
    public ResponseEntity<String> toggleAccountActiveStatus(
            @RequestParam(name = "userName", required = true) String userName,
            @RequestParam(name = "accountId", required = true) String accountId) {
        accountService.toggleAccountActiveStatus(userName, accountId);
        return ResponseEntity.ok("Account is toggled!");
    }

    @GetMapping(value = "/accountStatements")
    public ResponseEntity<List<BankAccountTransactionDocument>> getAccountTransactionStatements(
            @RequestParam(name = "userName", required = true) String userName,
            @RequestParam(name = "accountId", required = true) String accountId) {
        if (!accountService.isUserAccountExist(accountId, userName)) {
            throw new AppException("User Account do not exists.");
        }
        AccountDocument accountDocument = accountService.getAccountDocument(accountId, userName);
        return ResponseEntity.ok(accountService.getAccountTransactionDocuments(accountDocument));
    }

    @DeleteMapping(value = "/deleteTransaction")
    public ResponseEntity<String> deleteAccountTransaction(
            @RequestParam(name = "userName", required = true) String userName,
            @RequestParam(name = "accountId", required = true) String accountId,
            @RequestParam(name = "transactionId", required = true) String transactionId) {
        AccountDocument accountDocument = accountService.getAccountDocument(accountId, userName);
        if (accountService.deleteAccountTransaction(accountDocument, transactionId)) {
            return ResponseEntity.ok("Document is removed.");
        }
        return ResponseEntity.ok("Document is not removed.");

    }

}
