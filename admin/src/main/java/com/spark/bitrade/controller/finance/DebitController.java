package com.spark.bitrade.controller.finance;

import com.spark.bitrade.entity.CoinRate;
import com.spark.bitrade.entity.DebitConfig;
import com.spark.bitrade.service.CoinRateService;
import com.spark.bitrade.service.DebitConfigService;
import com.spark.bitrade.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("debit")
public class DebitController {
    @Autowired
    private DebitConfigService debitConfigService;
    @Autowired
    private CoinRateService coinRateService;

    /**
     * 借记配置
     * @param type
     * @param content
     * @return
     */
    @RequiresPermissions("debit:config:add")
    @PostMapping("/config/add")
    public MessageResult addConfig(@RequestParam("type") Integer type, @RequestParam("content") String content) {
        DebitConfig debitConfig = new DebitConfig();
        debitConfig.setType(type);
        debitConfig.setContent(content);
        if (debitConfigService.save(debitConfig) != null) {
            return MessageResult.success("添加成功");
        }else {
            return MessageResult.error("添加失败");
        }
    }

    @RequiresPermissions("debit:config:detail")
    @PostMapping("/config/detail")
    public MessageResult getConfig(@RequestParam("configId") Long configId) {
        MessageResult result = MessageResult.success();
        result.setData(debitConfigService.findOne(configId));
        return result;
    }

    @RequiresPermissions("debit:config:list")
    @PostMapping("/config/list")
    public MessageResult getConfigList() {
        MessageResult result = MessageResult.success();
        result.setData(debitConfigService.getAll());
        return result;
    }

    @RequiresPermissions("debit:config:delete")
    @PostMapping("/config/delete")
    public MessageResult deleteConfig(@RequestParam("configId") Long configId) {
        debitConfigService.delete(configId);
        return MessageResult.success("删除成功");
    }

    @RequiresPermissions("debit:config:update")
    @PostMapping("/config/update")
    public MessageResult updateConfig(@RequestParam("configId") Long configId,
                                      @RequestParam("type") Integer type,
                                      @RequestParam("content") String content) {
        DebitConfig debitConfig = debitConfigService.findOne(configId);
        debitConfig.setType(type);
        debitConfig.setContent(content);
        debitConfigService.save(debitConfig);
        return MessageResult.success("更新成功");
    }


    /**
     * 费率配置
     * @param baseCoin
     * @param coin
     * @param rate
     * @return
     */
    @RequiresPermissions("coin:rate:add")
    @PostMapping("/rate/add")
    public MessageResult addCoinRate(@RequestParam("baseCoin") String baseCoin,
                                     @RequestParam("coin") String coin,
                                     @RequestParam("rate") BigDecimal rate) {
        CoinRate coinRate = new CoinRate();
        coinRate.setBaseCoin(baseCoin);
        coinRate.setCoin(coin);
        coinRate.setRate(rate);

        if (coinRateService.save(coinRate) != null) {
            return MessageResult.success("添加成功");
        }else {
            return MessageResult.error("添加失败");
        }
    }

    @RequiresPermissions("coin:rate:detail")
    @PostMapping("/rate/detail")
    public MessageResult getCoinRate(@RequestParam("rateId") Long rateId) {
        MessageResult result = MessageResult.success();
        result.setData(coinRateService.findOne(rateId));
        return result;
    }

    @RequiresPermissions("coin:rate:list")
    @PostMapping("/rate/list")
    public MessageResult getCoinRateList() {
        MessageResult result = MessageResult.success();
        result.setData(coinRateService.getAll());
        return result;
    }

    @RequiresPermissions("coin:rate:delete")
    @PostMapping("/rate/delete")
    public MessageResult deleteCoinRate(@RequestParam("rateId") Long rateId) {
        coinRateService.delete(rateId);
        return MessageResult.success("删除成功");
    }

    @RequiresPermissions("coin:rate:update")
    @PostMapping("/rate/update")
    public MessageResult updateCoinRate(@RequestParam("rateId") Long rateId,
                                        @RequestParam("baseCoin") String baseCoin,
                                        @RequestParam("coin") String coin,
                                        @RequestParam("rate") BigDecimal rate) {
        CoinRate coinRate = coinRateService.findOne(rateId);
        coinRate.setRate(rate);
        coinRate.setBaseCoin(baseCoin);
        coinRate.setCoin(coin);

        coinRateService.save(coinRate);
        return MessageResult.success("更新成功");
    }
}
