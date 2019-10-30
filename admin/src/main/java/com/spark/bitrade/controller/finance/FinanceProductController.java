package com.spark.bitrade.controller.finance;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("financeProduct")
public class FinanceProductController {
    @Autowired
    private FinanceProductService financeProductService;
    @Autowired
    private FinanceProductDetailService financeProductDetailService;
    @Autowired
    private FinanceRecordService financeRecordService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private FinanceCoinService financeCoinService;

    @Autowired
    private CoinService coinService;

    @RequiresPermissions("financeProduct:add")
    @PostMapping("add")
    public MessageResult addProduct(@RequestParam("productName") String productName,@RequestParam("productExplain") String productExplain,
                                    @RequestParam("productLabel") String productLabel,@RequestParam("coinName") String coinName,
                                    @RequestParam("type") Integer type, @RequestParam("totalAmount") BigDecimal totalAmount,
                                    @RequestParam("minAmount") String minAmount, @RequestParam("maxAmount") String maxAmount,
                                    @RequestParam("timeRate") BigDecimal timeRate, @RequestParam("fixedRate") String fixedRate,
                                    @RequestParam("fixedBreakRate") String fixedBreakRate,
                                    @RequestParam("period") String period, @RequestParam("note") String note) {

        if (StringUtils.isEmpty(productName)) return MessageResult.error("产品名称不能为空");
        if (StringUtils.isEmpty(coinName)) return MessageResult.error("币种不能为空");
//        if (coinService.findByUnit(coinName) == null) return MessageResult.error("系统不存在该币种");
        if (type == null) return MessageResult.error("请设置产品类型");
        if (type == 0 && timeRate == null) return MessageResult.error("活期日利率不能为空");
        if (type == 1 && fixedRate == null) return MessageResult.error("定期类型月利率不能为空");
        if (type == 1 && fixedBreakRate == null) return MessageResult.error("定期类型解约月利率不能为空");
        if (type == 1 && period == null) return MessageResult.error("定期类型周期不能为空");

        FinanceProduct financeProduct = new FinanceProduct();
        financeProduct.setProductName(productName);
        financeProduct.setProductExplain(productExplain);
        financeProduct.setProductLabel(productLabel);
        financeProduct.setCoinName(coinName);
        financeProduct.setType(type);
        if (totalAmount != null) financeProduct.setTotalAmount(totalAmount);
        if (!StringUtils.isEmpty(minAmount)) financeProduct.setMinAmount(minAmount);
        if (!StringUtils.isEmpty(maxAmount)) financeProduct.setMaxAmount(maxAmount);
        if (timeRate != null) financeProduct.setTimeRate(timeRate);
        if (fixedRate != null) financeProduct.setFixedRate(fixedRate);
        if (!StringUtils.isEmpty(fixedBreakRate)) financeProduct.setFixedBreakRate(fixedBreakRate);
        if (period != null) financeProduct.setPeriod(period);
        financeProduct.setNote(note);

        if (financeProductService.save(financeProduct) != null) {
            return MessageResult.success("添加成功");
        } else {
            return MessageResult.error("添加失败");
        }
    }

    @RequiresPermissions("financeProduct:update")
    @PostMapping("update")
    public MessageResult updateProduct(@RequestParam("productId") Long productId,
                                       String productName, String productExplain,
                                       String productLabel,String coinName,
                                       Integer type, BigDecimal totalAmount,
                                       String minAmount, String maxAmount,
                                       BigDecimal timeRate, String fixedRate, String fixedBreakRate,
                                       String period, String note) {

        FinanceProduct financeProduct = financeProductService.findOne(productId);
        if (financeProduct == null) {
            return MessageResult.error("理财产品不存在");
        }

        if (!StringUtils.isEmpty(productName)) financeProduct.setProductName(productName);
        if (!StringUtils.isEmpty(productExplain)) financeProduct.setProductExplain(productExplain);
        if (!StringUtils.isEmpty(productLabel)) financeProduct.setProductLabel(productLabel);
        if (!StringUtils.isEmpty(coinName)) {
//            if (coinService.findByUnit(coinName) == null) return MessageResult.error("系统不存在该币种");
//            else financeProduct.setCoinName(coinName);
            financeProduct.setCoinName(coinName);
        }

        if (type != null) financeProduct.setType(type);
        if (totalAmount != null) financeProduct.setTotalAmount(totalAmount);
        if (!StringUtils.isEmpty(minAmount)) financeProduct.setMinAmount(minAmount);
        if (!StringUtils.isEmpty(maxAmount)) financeProduct.setMaxAmount(maxAmount);
        if (period != null) financeProduct.setPeriod(period);
        if (!StringUtils.isEmpty(note)) financeProduct.setNote(note);
        if (timeRate != null) financeProduct.setTimeRate(timeRate);
        if (fixedRate != null) financeProduct.setFixedRate(fixedRate);
        if (!StringUtils.isEmpty(fixedBreakRate)) financeProduct.setFixedBreakRate(fixedBreakRate);

        if (financeProductService.save(financeProduct) != null) {
            return MessageResult.success("修改成功");
        } else {
            return MessageResult.error("修改失败");
        }
    }

    /**
     * 上架
     * @param productId
     * @return
     */
    @RequiresPermissions("financeProduct:putOn")
    @PostMapping("putOn")
    public MessageResult putOnProduct(@RequestParam("productId") Long productId) {
        FinanceProduct financeProduct = financeProductService.findOne(productId);
        if (financeProduct == null) {
            return MessageResult.error("理财产品不存在");
        }
        if (financeProduct.getStatus() == 0) {
            financeProduct.setStatus(1);
            financeProduct.setStartDate(new Date());
            if (financeProductService.save(financeProduct) != null) {
                return MessageResult.success("上架成功");
            } else {
                return MessageResult.error("上架失败");
            }
        } else {
            return MessageResult.error("该产品已上架或已结束");
        }
    }

    /**
     * 下架，没有计算定期收益
     * @param productId
     * @return
     */
    @RequiresPermissions("financeProduct:putOff")
    @PostMapping("putOff")
    public MessageResult putOffProduct(@RequestParam("productId") Long productId) {
        FinanceProduct financeProduct = financeProductService.findOne(productId);
        if (financeProduct == null) {
            return MessageResult.error("理财产品不存在");
        }
        if (financeProduct.getStatus() == 1) {
            financeProductService.putOff(financeProduct);
            List<FinanceProductDetail> financeProductDetailList = financeProductDetailService.getListByProduct(financeProduct);
            //返还冻结金额
            financeProductDetailList.stream().forEach(financeProductDetail -> {
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(financeProductDetail.getCoinName(), financeProductDetail.getMember().getId());
                if (memberWallet != null) {
                    memberWalletService.thawBalance(memberWallet, financeProductDetail.getAmount());
                }
            });
            return MessageResult.success("下架成功");
        } else {
            return MessageResult.error("该产品已下架或已结束");
        }
    }

    @RequiresPermissions("financeProduct:detail")
    @PostMapping("detail")
    public MessageResult getProduct(@RequestParam("productId") Long productId) {

        FinanceProduct financeProduct = financeProductService.findOne(productId);
        if (financeProduct == null) {
            return MessageResult.error("理财产品不存在");
        }

        MessageResult result = MessageResult.success();
        result.setData(financeProduct);
        return result;
    }

    @RequiresPermissions("financeProduct:list")
    @PostMapping("list")
    public Page<FinanceProduct> getProductList(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        return financeProductService.getAll(pageNo, pageSize);
    }

    /**
     * 某个产品用户参与明细
     * @param productId
     * @param pageNo
     * @param pageSize
     */
    @RequiresPermissions("financeProduct:join:list")
    @PostMapping("join/list")
    public Page<FinanceProductDetail> getProductJoinList(@RequestParam("productId") Long productId,
                                                         @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                                                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        FinanceProduct financeProduct = financeProductService.findOne(productId);
        if (financeProduct == null) {
            return null;
        }

        return financeProductDetailService.getListByProduct(financeProduct,pageNo,pageSize);
    }

    /**
     * 某个产品明细的收益明细
     */
    @RequiresPermissions("financeProduct:member:income:list")
    @PostMapping("member/income/list")
    public Page<FinanceRecord> getMemberIncomeList(@RequestParam("productDetailId") Long productDetailId,
                                                   @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        FinanceProductDetail financeProductDetail = financeProductDetailService.findOne(productDetailId);
        if (financeProductDetail == null) {
            return null;
        }
        return financeRecordService.getAllByProductDetail(financeProductDetail,pageNo,pageSize);
    }

    /**
     * 添加理财支持的币种
     * @param coinName
     * @return
     */
    @RequiresPermissions("financeProduct:coin:add")
    @PostMapping("coin/add")
    public MessageResult addCoin(@RequestParam("coinName") String coinName) {
        Coin coin = coinService.findOne(coinName);
        if (coin == null) return MessageResult.error("不支持的币种");
        List<FinanceCoin> financeCoinList = financeCoinService.findByCoin(coin);
        if (financeCoinList.size() > 0) return MessageResult.error("已配置该币种");

        FinanceCoin financeCoin = new FinanceCoin();
        financeCoin.setCoin(coin);
        if (financeCoinService.save(financeCoin)!=null){
            return MessageResult.success("添加成功");
        }else {
            return MessageResult.error("添加失败");
        }
    }

    /**
     * 启用或禁用币种
     * @param financeCoinId
     * @param status
     * @return
     */
    @RequiresPermissions("financeProduct:coin:setting")
    @PostMapping("coin/setting")
    public MessageResult updateCoinStatus(@RequestParam("financeCoinId") Long financeCoinId, @RequestParam("status") CommonStatus status) {
        FinanceCoin financeCoin = financeCoinService.findOne(financeCoinId);
        if (financeCoin == null) return MessageResult.error("没有配置的理财币种");

        financeCoin.setStatus(status);
        if (financeCoinService.save(financeCoin) != null) {
            return MessageResult.success("设置成功");
        }else {
            return MessageResult.error("设置失败");
        }
    }

    /**
     * 所有的理财币种
     * @return
     */
    @RequiresPermissions("financeProduct:coin:all")
    @PostMapping("coin/all")
    public List<FinanceCoin> getAllCoins() {
        return financeCoinService.getAllCoins();
    }

    /**
     * 所有可用的理财币种
     * @return
     */
    @PostMapping("coin/list")
    @RequiresPermissions("financeProduct:coin:list")
    public String getAvailableCoins() {
        List<FinanceCoin> financeCoinList = financeCoinService.getAllAvailableCoins();
        String result = "";
        for (FinanceCoin financeCoin : financeCoinList) {
            result += financeCoin.getCoin().getUnit() + ";";
        }
        if (result.length() > 0) result = result.substring(0, result.length()-1);
        return result;
    }
}
