package org.gauravagrwl.myApp.model.assets;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CryptoAsset extends Asset{

    private String crypto_coin_name;
    private String crypto_coin_symbol;

}
// d6084c19-35c4-47ee-8d95-7c6fb6cb7700