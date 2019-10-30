package com.spark.bitrade.model.screen;

import com.spark.bitrade.constant.LegalWalletState;
import lombok.Data;

@Data
public class LegalWalletRechargeScreen {
    LegalWalletState status;
    String username;
    String coinName;

}
