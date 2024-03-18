package org.gauravagrwl.myApp.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.accountTransaction.BankAccountStatementDocument;
import org.gauravagrwl.myApp.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.MappingStrategy;

@RestController
@RequestMapping(value = "/upload")
public class DataUploadController {

    private AccountService accountService;

    public DataUploadController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/uploadStatements")
    public ResponseEntity<String> uploadAccountStatement(
            @RequestParam(name = "userName", required = true) String userName,
            @RequestParam(name = "accountId", required = true) String accountId,
            @RequestParam(required = true, name = "file") MultipartFile file) throws IOException {

        if ((file.isEmpty()) || (!accountService.isUserAccountExist(accountId, userName))) {
            return ResponseEntity.badRequest().body("No Accout exist for this user.");
        }
        AccountDocument accountDocument = accountService.getAccountDocument(accountId, userName);

        String profileType = accountDocument.getAccountType().getAccountTypeName();

        MappingStrategy<BankAccountStatementDocument> headerColumnNameMappingStrategy = BankAccountStatementDocument
                .getHeaderColumnNameMappingStrategy(
                        profileType);

        InputStreamReader reader = new InputStreamReader(file.getInputStream());
        CsvToBean<BankAccountStatementDocument> csvToBean = new CsvToBeanBuilder<BankAccountStatementDocument>(
                reader)
                .withProfile(accountDocument.getAccountType().getAccountTypeName())
                .withSeparator(',').withIgnoreLeadingWhiteSpace(true)
                .withMappingStrategy(headerColumnNameMappingStrategy)
                .build();
        List<BankAccountStatementDocument> transactionList = new ArrayList<>();
        csvToBean.iterator().forEachRemaining(e -> {
            transactionList.add(e);
        });
        transactionList.forEach(transDoc -> {
            if (StringUtils.equalsIgnoreCase("Credit", transDoc.getType())) {
                transDoc.setCredit(transDoc.getTransient_amount().abs());
            } else {
                transDoc.setDebit(transDoc.getTransient_amount().abs());
            }
            transDoc.setAccountDocumentId(accountDocument.getId());
        });

        accountService.processAccountStatements((List) transactionList, accountDocument);

        return ResponseEntity.ok("Account statement updated for account id : " + accountId);
    }

    public AccountService getAccountService() {
        return accountService;
    }

}
