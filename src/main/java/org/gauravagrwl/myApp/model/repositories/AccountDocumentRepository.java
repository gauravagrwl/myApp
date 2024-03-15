package org.gauravagrwl.myApp.model.repositories;

import org.gauravagrwl.myApp.model.accountDocument.AccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AccountDocumentRepository extends MongoRepository<AccountDocument, String> {

    List<AccountDocument> findByProfileDocumentId(String profileDocumentId);

    boolean existsByIdAndProfileDocumentId(String id, String profileDocumentId);

}
