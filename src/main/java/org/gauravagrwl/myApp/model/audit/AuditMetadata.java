package org.gauravagrwl.myApp.model.audit;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@AllArgsConstructor(onConstructor = @__({ @PersistenceCreator }))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuditMetadata {
    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant modifiedDate;

    @CreatedBy
    private String createdBy;

}
