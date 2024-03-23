package org.gauravagrwl.myApp.model.repositories;

import org.gauravagrwl.myApp.model.reports.CashFlowReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CashFlowReportDocumentRepository
                extends MongoRepository<CashFlowReportDocument, String> {

        CashFlowReportDocument findByAccountStatementId(String accountStatementId);

        Boolean existsByAccountStatementId(String accountStatementId);

        Long deleteByAccountStatementId(String accountStatementId);

}
