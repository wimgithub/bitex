package com.spark.bitrade.controller;

import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1816:54
 */
@RestController
@RequestMapping("exchange-coin")
public class ExchangeCoinController extends BaseController {
    @Autowired
    private ExchangeCoinService service;

    //获取基币
    @RequestMapping("base-symbol")
    public MessageResult baseSymbol() {
        List<String> baseSymbol = service.getBaseSymbol();
        if (baseSymbol != null && baseSymbol.size() > 0) return success(baseSymbol);
        return error("baseSymbol null");

    }

}
