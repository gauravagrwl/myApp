package org.gauravagrwl.myApp.model.repositories;

import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.gauravagrwl.myApp.model.accountTransaction.BankAccountTransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountTransactionDocumentRepository extends MongoRepository<BankAccountTransactionDocument, String> {

}
