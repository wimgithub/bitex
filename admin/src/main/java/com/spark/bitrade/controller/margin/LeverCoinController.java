package com.spark.bitrade.controller.margin;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.system.CoinExchangeFactory;
import com.spark.bitrade.util.FileUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.sparkframework.security.Encrypt;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 币币交易手续费
 * @date 2018/1/19 15:16
 */
@RestController
@RequestMapping("lever/lever-coin")
public class LeverCoinController extends BaseAdminController {

    @Value("${spark.system.md5.key}")
    private String md5Key;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private CoinService coinService;

    @Autowired
    private CoinExchangeFactory coinExchangeFactory;

    @Autowired
    private LeverWalletService leverWalletService;

    @Autowired
    private LeverCoinService leverCoinService;

    @Autowired
    private LoanRecordService loanRecordService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private LeverWalletTransferRecordService leverWalletTransferRecordService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ExchangeCoinService exchangeCoinService;

    @GetMapping("allCoin")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "查询所有币种")
    public MessageResult listAllCoin(){
        MessageResult result=MessageResult.success();
        List<Coin> coinList=coinService.findByStatus(CommonStatus.NORMAL);
        result.setData(coinList);
        return result;
    }

    @RequiresPermissions("lever:lever-coin:merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "添加杠杆币对")
    public MessageResult exchangeCoinList(
            @Valid LeverCoin leverCoin) {
        if(leverCoin.getCoinSymbol().equalsIgnoreCase(leverCoin.getBaseSymbol())){
            return MessageResult.error(msService.getMessage("MUST_DIFFERENT_COIN"));
        }
        ExchangeCoin oldExchangeCoin=exchangeCoinService.findBySymbol(leverCoin.getSymbol());
        if(oldExchangeCoin==null){
            return MessageResult.error(msService.getMessage("NEED_EXCHANGE_COIN"));
        }
        if(leverCoin.getProportion().compareTo(BigDecimal.ONE)<=0){
            return MessageResult.error("validate proportion");
        }
        leverCoin = leverCoinService.save(leverCoin);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), leverCoin);
    }

    @RequiresPermissions("lever:lever-coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "分页查找杠杆币对")
    public MessageResult exchangeCoinList(PageModel pageModel) {
        if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            list.add("symbol");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        Page<LeverCoin> all = leverCoinService.findAll(null, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("lever:lever-coin:page-query")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "杠杆币对详情")
    public MessageResult detail(
            @RequestParam(value = "symbol") String symbol) {
        LeverCoin exchangeCoin = leverCoinService.getBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        return success(exchangeCoin);
    }

    @RequiresPermissions("lever:lever-coin:deletes")
    @PostMapping("deletes")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "杠杆币对删除")
    public MessageResult deletes(
            @RequestParam(value = "ids") Long[] ids) {
        leverCoinService.deletes(ids);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("lever:lever-coin:alter-rate")
    @PostMapping("alter-rate")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "修改杠杆交易币对")
    public MessageResult alterExchangeCoinRate(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "enable", required = false) BooleanEnum enable,
            @RequestParam(value = "sort", required = false) Integer sort,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "interestRate",required = false)BigDecimal interestRate,
            @RequestParam(value = "")BigDecimal proportion,
            /*@RequestParam(value = "minTurnIntoAmount",defaultValue = "0")BigDecimal minTurnIntoAmount,
            @RequestParam(value = "minTurnOutAmount",defaultValue ="0")BigDecimal minTurnOutAmount,*/
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        password = Encrypt.MD5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        LeverCoin exchangeCoin = leverCoinService.getBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if(proportion.compareTo(BigDecimal.ONE)<=0){
            return MessageResult.error("validate proportion");
        }
        if (sort != null)
            exchangeCoin.setSort(sort);//设置排序
        if (enable != null )
            exchangeCoin.setEnable(enable);//设置启用 禁用
        if(interestRate!=null){
            exchangeCoin.setInterestRate(interestRate);
        }
        exchangeCoin.setProportion(proportion);
        /*if(minTurnIntoAmount!=null){
            exchangeCoin.setMinTurnIntoAmount(minTurnIntoAmount);
        }
        if(minTurnOutAmount!=null){
            exchangeCoin.setMinTurnOutAmount(minTurnOutAmount);
        }*/
        leverCoinService.save(exchangeCoin);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("lever:lever-coin:list")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "导出杠杆币对 Excel")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = leverCoinService.findAll();
        return new FileUtil().exportExcel(request, response, all, "exchangeCoin");
    }

    /**
     * 获取所有交易区币种的单位
     *
     * @return
     */
    @RequiresPermissions("lever:lever-coin:list")
    @PostMapping("all-base-symbol-units")
    public MessageResult getAllBaseSymbolUnits() {
        List<String> list = leverCoinService.getBaseSymbol();
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * 获取交易区币种 所支持的交易 币种
     *
     * @return
     */
    @RequiresPermissions("lever:lever-coin:list")
    @PostMapping("all-coin-symbol-units")
    public MessageResult getAllCoinSymbolUnits(@RequestParam("baseSymbol") String baseSymbol) {
        List<String> list = leverCoinService.getCoinSymbol(baseSymbol);
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * 查询杠杆交易钱包
     * @param memberId
     * @return
     */
    @RequiresPermissions("lever:lever-coin:list")
    @GetMapping("list")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "查询杠杆交易钱包")
    public MessageResult listLeverWallet(String symbol,Long memberId){
        List<LeverCoin> leverCoinList=new ArrayList<>();
        if(symbol==null || symbol.equals("")){
            leverCoinList=leverCoinService.findByEnable(BooleanEnum.IS_TRUE);
        }else{
            LeverCoin leverCoin=leverCoinService.getBySymbol(symbol);
            if(leverCoin==null){
                return MessageResult.error(msService.getMessage("SYMBOL_NOT_FOUND"));
            }
            leverCoinList.add(leverCoin);
        }
        MessageResult result=MessageResult.success();
        if(leverCoinList!=null&&leverCoinList.size()>0){
            List<LeverWallet> leverWalletList;
            if(memberId !=null && !memberId.equals("")){
                leverWalletList=leverWalletService.findByMemberId(memberId);
            }else {
                leverWalletList = leverWalletService.findAll();
            }
            List<LeverWalletVO> voList=new ArrayList<>();
            //Coin btcCoin=coinService.findByUnit(LeverWalletVO.btcUnit);
            //CoinExchangeFactory.ExchangeRate btcRate=coinExchangeFactory.get(LeverWalletVO.btcUnit);
            //btcCoin.setSgdRate(btcRate.getSgdRate());
            //btcCoin.setCnyRate(btcRate.getCnyRate());
            //btcCoin.setUsdRate(btcRate.getUsdRate());
            for(LeverCoin leverCoin:leverCoinList){
                List<LeverWallet> list=new ArrayList<>();
                if(leverWalletList!=null&&leverWalletList.size()>0){
                    for(LeverWallet leverWallet:leverWalletList){
                        if(leverWallet.getLeverCoin().getSymbol().equalsIgnoreCase(leverCoin.getSymbol())){
                            list.add(leverWallet);
                        }
                        LeverWalletVO vo=new LeverWalletVO();
                        vo.setRiskRate(getCalculatedRisk(leverWallet.getMemberId(),leverCoin.getId()).getRiskRate());
                        vo.setMemberId(leverWallet.getMemberId());
                        vo.setSymbol(leverCoin.getSymbol());
                        if(list.size()==0){
                            Coin baseCoin=coinService.findByUnit(leverCoin.getBaseSymbol());
                            Coin coin=coinService.findByUnit(leverCoin.getCoinSymbol());
                            LeverWallet baseWallet=new LeverWallet();
                            baseWallet.setCoin(baseCoin);
                            baseWallet.setMemberId(leverWallet.getMemberId());
                            baseWallet.setMemberName(leverWallet.getMemberName());
                            baseWallet.setLeverCoin(leverCoin);
                            list.add(baseWallet);
                            LeverWallet coinWallet=new LeverWallet();
                            coinWallet.setCoin(coin);
                            coinWallet.setMemberId(leverWallet.getMemberId());
                            coinWallet.setMemberName(leverWallet.getMemberName());
                            coinWallet.setLeverCoin(leverCoin);
                            list.add(coinWallet);
                        }else if(list.size()==1){
                            leverWallet=list.get(0);
                            Coin coin=null;
                            if(leverWallet.getCoin().getUnit().equals(leverCoin.getBaseSymbol())){
                                coin=coinService.findByUnit(leverCoin.getCoinSymbol());
                            }else if(leverWallet.getCoin().getUnit().equals(leverCoin.getCoinSymbol())){
                                coin=coinService.findByUnit(leverCoin.getBaseSymbol());
                            }
                            if(coin!=null){
                                LeverWallet coinWallet=new LeverWallet();
                                coinWallet.setCoin(coin);
                                coinWallet.setMemberId(leverWallet.getMemberId());
                                coinWallet.setMemberName(leverWallet.getMemberName());
                                coinWallet.setLeverCoin(leverCoin);
                                list.add(coinWallet);
                            }
                        }
                        vo.setLeverWalletList(list);
                        checkLoanUpper(vo);
                        //vo.setBtcCoin(btcCoin);
                        voList.add(vo);
                    }
                }
            }
            result.setData(voList);
        }
        return result;
    }

    @RequiresPermissions("lever:lever-coin:list")
    @GetMapping("transfer")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "查询划转记录")
    public MessageResult listTransferRecord(String symbol,String coinUnit,Integer type,String userName,PageModel pageModel){
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(symbol)){
            LeverCoin leverCoin=leverCoinService.getBySymbol(symbol);
            if(leverCoin==null){
                return MessageResult.error("symbol has not found");
            }
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.leverCoin.eq(leverCoin));
        }
        if(StringUtils.isNotBlank(coinUnit)){
            Coin coin=coinService.findByUnit(coinUnit);
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.coin.eq(coin));
        }
        if(type!=null){
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.type.eq(type));
        }
        if(StringUtils.isNotBlank(userName)){
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.memberName.like(userName));
        }
        Predicate predicate=PredicateUtils.getPredicate(booleanExpressions);
        Page<LeverWalletTransferRecord> page=leverWalletTransferRecordService.findAll(predicate,pageModel);
        MessageResult result=MessageResult.success();
        result.setData(page);
        return result;
    }

    @RequiresPermissions("lever:lever-coin:list")
    @PostMapping("riskList")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "查询各个杠杆用户风险率")
    public MessageResult riskList(PageModel pageModel){
        Map map=leverWalletService.listMarginMember(pageModel.getPageable());
        List<MarginMemberVO> marginMemberVOList= (List<MarginMemberVO>) map.get("content");
        if(marginMemberVOList!=null&&marginMemberVOList.size()>0){
            for(MarginMemberVO marginMemberVO:marginMemberVOList){
                InspectBean inspectBean=getCalculatedRisk(marginMemberVO.getMemberId(),marginMemberVO.getLeverCoinId());
                marginMemberVO.setInspectBean(inspectBean);
            }
        }
        MessageResult result=MessageResult.success();
        result.setData(map);
        return result;
    }

    /**
     * 查询借贷上线
     * @param vo
     */
    public void checkLoanUpper(LeverWalletVO vo){
        LeverCoin leverCoin=vo.getLeverWalletList().get(0).getLeverCoin();
        List<LoanRecord> loanRecordList=loanRecordService.findByMemberIdAndLeverCoinAndRepayment(vo.getMemberId(),leverCoin,BooleanEnum.IS_FALSE);
        //可借贷总金额，单位美元
        List<LeverWallet> leverWalletList=vo.getLeverWalletList();
        BigDecimal totalCanLoanAmountUsd=BigDecimal.ZERO;//钱包余额
        for(LeverWallet leverWallet:leverWalletList){
            CoinExchangeFactory.ExchangeRate coinRate=coinExchangeFactory.get(leverWallet.getCoin().getUnit());
            totalCanLoanAmountUsd=totalCanLoanAmountUsd.add(leverWallet.getBalance()
                    .multiply(leverCoin.getProportion().subtract(BigDecimal.ONE)).multiply(coinRate.getUsdRate()));
            leverWallet.getCoin().setUsdRate(coinRate.getUsdRate());
            leverWallet.getCoin().setCnyRate(coinRate.getCnyRate());
            leverWallet.getCoin().setSgdRate(coinRate.getSgdRate());
            BigDecimal walletBalance=leverWallet.getBalance().add(leverWallet.getFrozenBalance());
            //leverWallet.setFoldBtc(walletBalance.multiply(coinRate.getUsdRate()).divide(btcCoin.getUsdRate(),8,BigDecimal.ROUND_DOWN));
        }
        //已借贷总金额，单位美元
        BigDecimal totalLoanAmountUsd=BigDecimal.ZERO;
        //已接待金额加利息，单位美元
        BigDecimal totalLoanAmountAndAccumulativeUsd=BigDecimal.ZERO;
        BigDecimal baseLoanCount=BigDecimal.ZERO;
        BigDecimal coinLoanCount=BigDecimal.ZERO;
        BigDecimal baseAccumulativeCount=BigDecimal.ZERO;
        BigDecimal coinAccumulativeCount=BigDecimal.ZERO;
        for(LoanRecord loanRecord:loanRecordList){
            CoinExchangeFactory.ExchangeRate coinRate=coinExchangeFactory.get(loanRecord.getCoin().getUnit());
            totalLoanAmountUsd=totalLoanAmountUsd.add(loanRecord.getAmount()).multiply(coinRate.getUsdRate());
            totalLoanAmountAndAccumulativeUsd=totalLoanAmountAndAccumulativeUsd
                    .add(loanRecord.getAmount().add(loanRecord.getAccumulative()).multiply(coinRate.getUsdRate()));
            if(loanRecord.getCoin().getUnit().equals(leverCoin.getBaseSymbol())){
                baseLoanCount=baseLoanCount.add(loanRecord.getAmount());
                baseAccumulativeCount=baseAccumulativeCount.add(loanRecord.getAccumulative());
            }else if(loanRecord.getCoin().getUnit().equals(leverCoin.getCoinSymbol())){
                coinLoanCount=coinLoanCount.add(loanRecord.getAmount());
                coinAccumulativeCount=coinAccumulativeCount.add(loanRecord.getAccumulative());
            }
        }
        vo.setBaseLoanCount(baseLoanCount);
        vo.setBaseAccumulativeCount(baseAccumulativeCount);
        vo.setCoinLoanCount(coinLoanCount);
        vo.setCoinAccumulativeCount(coinAccumulativeCount);
        //持有金额减去已借贷金额，是为实际的本金
        BigDecimal totalCanLoan=totalCanLoanAmountUsd.subtract(totalLoanAmountUsd);
        //本金乘以(倍率-1)，减去已借贷金额和利息，结果为可借贷金额
        totalCanLoan=totalCanLoan.multiply(vo.getProportion().subtract(BigDecimal.ONE)).subtract(totalLoanAmountAndAccumulativeUsd);
        if(totalCanLoan.compareTo(BigDecimal.ZERO)>0){
            vo.setBaseCanLoan(totalCanLoan.divide(coinExchangeFactory.get(leverCoin.getBaseSymbol()).getUsdRate(),8,BigDecimal.ROUND_DOWN));
            vo.setCoinCanLoan(totalCanLoan.divide(coinExchangeFactory.get(leverCoin.getCoinSymbol()).getUsdRate(),8,BigDecimal.ROUND_DOWN));
        }else{
            vo.setBaseCanLoan(BigDecimal.ZERO);
            vo.setCoinCanLoan(BigDecimal.ZERO);
        }

    }


    /**
     * 计算风险率
     * @param memberId
     * @param leverCoinId
     * @return
     */
    public InspectBean getCalculatedRisk(Long memberId,Long leverCoinId){
        System.out.println("用户"+memberId+"币对ID:"+leverCoinId);
        InspectBean inspectBean=new InspectBean();
        LeverCoin leverCoin=leverCoinService.findOne(leverCoinId);
        Member member=memberService.findOne(memberId);
        inspectBean.setLeverCoin(leverCoin);
        inspectBean.setMember(member);
        inspectBean.setMemberId(memberId);
        List<LoanRecord> loanRecordList=loanRecordService.findByMemberIdAndLeverCoinAndRepayment(memberId,leverCoin,BooleanEnum.IS_FALSE);
        List<LeverWallet> leverWalletList=leverWalletService.findByMemberIdAndLeverCoin(memberId,leverCoin);
        if(loanRecordList!=null&&loanRecordList.size()>0){
            if(leverWalletList!=null&&leverWalletList.size()>0){
                //借款金额
                BigDecimal totalLoan=BigDecimal.ZERO;
                //钱包总金额
                BigDecimal totalAmount=BigDecimal.ZERO;
                for(LoanRecord loanRecord:loanRecordList){
                    CoinExchangeFactory.ExchangeRate coinRate=coinExchangeFactory.get(loanRecord.getCoin().getUnit());
                    BigDecimal amount=coinRate.getUsdRate().multiply(loanRecord.getAmount().add(loanRecord.getAccumulative()));
                    totalLoan=totalLoan.add(amount);
                }
                for(LeverWallet leverWallet:leverWalletList){
                    CoinExchangeFactory.ExchangeRate coinRate=coinExchangeFactory.get(leverWallet.getCoin().getUnit());
                    BigDecimal amount=coinRate.getUsdRate().multiply(leverWallet.getBalance().add(leverWallet.getFrozenBalance()));
                    totalAmount=totalAmount.add(amount);
                }
                //钱包余额除以借贷金额等于风险率
                if(totalLoan.compareTo(BigDecimal.ZERO)>0){
                    System.out.println("总资产=============="+totalAmount);
                    System.out.println("借贷资产=============="+totalLoan);
                    inspectBean.setRiskRate(totalAmount.multiply(new BigDecimal(100)).divide(totalLoan,8,BigDecimal.ROUND_DOWN));
                }else{
                    //无借贷用户
                    inspectBean.setRiskRate(BigDecimal.ZERO);
                }
            }else{
                //标识已经冻结了钱包的用户
                inspectBean.setRiskRate(BigDecimal.ZERO);
            }
        }else {
            inspectBean.setRiskRate(BigDecimal.ZERO);
        }
        return inspectBean;
    }

}
