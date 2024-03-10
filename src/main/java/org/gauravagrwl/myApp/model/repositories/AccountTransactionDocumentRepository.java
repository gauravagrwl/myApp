package org.gauravagrwl.myApp.model.repositories;

import org.gauravagrwl.myApp.model.AccountDocument;
import org.gauravagrwl.myApp.model.AccountTransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountTransactionDocumentRepository extends MongoRepository<AccountTransactionDocument, String> {

}
