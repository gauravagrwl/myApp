package org.gauravagrwl.myApp.model.accountStatement;

import org.gauravagrwl.myApp.model.audit.AuditMetadata;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "account_statement_document")
public abstract class AccountStatementDocument {

        @MongoId
        private String id;

        private String accountDocumentId;

        @Indexed
        private Boolean reconciled = Boolean.FALSE;

        private Boolean duplicate = Boolean.FALSE;

        private AuditMetadata audit = new AuditMetadata();

        @Version
        private Integer version;

}
