package org.gauravagrwl.myApp.model.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.gauravagrwl.myApp.model.profileAccount.accountStatement.AccountStatementDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface AccountStatementDocumentRepository extends MongoRepository<AccountStatementDocument, String> {

    List<AccountStatementDocument> findByAccountDocumentId(String accountDocumentId, Pageable pageable);

    List<AccountStatementDocument> findByAccountDocumentId(String accountDocumentId, Sort sort);

    @Update("{ '$set' : { 'reconciled' : ?#{[1]} } }")
    void findAndUpdateReconcileById(String id, Boolean value);

    @Update("{ '$set' : { 'duplicate' : ?#{[1]} } }")
    void findAndUpdateDuplicateById(String id, Boolean value);

    @Update("{ '$set' : { 'balance' : ?#{[1]} } }")
    void findAndUpdateStaementBalanceById(String id, BigDecimal value);

    @Query("{'transactionDate': ?0, 'descriptions': ?1, 'type': ?2, 'debit': ?3, 'credit': ?4}")
    List<AccountStatementDocument> findDuplicateBankStatement(LocalDate transactionDate, String description,
            String type, BigDecimal debit, BigDecimal credit);

}
