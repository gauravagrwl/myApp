package org.gauravagrwl.myApp.model.repositories;

import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

import javax.management.Query;

public interface AccountDocumentRepository extends MongoRepository<AccountDocument, String> {

    public List<AccountDocument> findByProfileDocumentId(String profileDocumentId);

    public Boolean existsByIdAndProfileDocumentId(String id, String profileDocumentId);

}
