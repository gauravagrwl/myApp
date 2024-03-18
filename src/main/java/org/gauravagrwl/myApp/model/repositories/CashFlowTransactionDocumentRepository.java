package org.gauravagrwl.myApp.model.repositories;

import org.gauravagrwl.myApp.model.reports.CashFlowTransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CashFlowTransactionDocumentRepository
                extends MongoRepository<CashFlowTransactionDocument, String> {

        CashFlowTransactionDocument findByAccountStatementId(String accountStatementId);

        Boolean existsByAccountStatementId(String accountStatementId);

        Long deleteByAccountStatementId(String accountStatementId);

}
