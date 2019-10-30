package com.spark.bitrade.controller.screen;

import com.spark.bitrade.constant.LegalWalletState;
import lombok.Data;

@Data
public class LegalWalletScreen {
    private LegalWalletState state;
    private String coinName;
}
