package org.gauravagrwl.myApp.model.profileAccount.holding;


import lombok.Data;
import org.gauravagrwl.myApp.helper.ExchangeEnum;
import org.gauravagrwl.myApp.model.assets.Asset;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Document(collection = "holding_documents")
public abstract class Holding {

    @MongoId
    private String id;

    private Asset asset;

    private BigDecimal quantity;

    private BigDecimal purchaseRate;

    private LocalDate purchaseDate;

    private String exchangeBrokerName;

}
