package org.gauravagrwl.myApp.model.profileAccount.accountDocument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvestmentAccountApiDocument {

    private String userId;
    private String userSecrect;
    private String brokerProdApiUrl;
    private String brokerProdApiKey;
    private String brokerProdApiSecrect;
    private String brokerTestApiUrl;
    private String brokerTestApiKey;
    private String brokerTestApiSecrect;

}
