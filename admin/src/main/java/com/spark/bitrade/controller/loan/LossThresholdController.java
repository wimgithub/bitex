package com.spark.bitrade.controller.loan;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.entity.LeverCoin;
import com.spark.bitrade.entity.LoanRecord;
import com.spark.bitrade.entity.LossThreshold;
import com.spark.bitrade.entity.QLoanRecord;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.PerformActionsEnum;
import com.spark.bitrade.service.LeverCoinService;
import com.spark.bitrade.service.LoanRecordService;
import com.spark.bitrade.service.LossThresholdService;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 管理杠杆交易亏损阈值
 */
@Slf4j
@RestController
@RequestMapping("/system/lossThreshold")
public class LossThresholdController {
    @Autowired
    private LossThresholdService lossThresholdService;
    @Autowired
    private LeverCoinService leverCoinService;
    @Autowired
    private LoanRecordService loanRecordService;

    @RequiresPermissions("lossThreshold:all")
    @GetMapping("list")
    @AccessLog(module = AdminModule.MEMBER, operation = "查询所有的亏损阈值")
    public MessageResult listLossThreshold(){
        List<LeverCoin> leverCoinList=leverCoinService.findByEnable(BooleanEnum.IS_TRUE);
        MessageResult result=MessageResult.success();
        if(leverCoinList!=null&&leverCoinList.size()>0){
            List<LossThreshold> lossThresholdList=lossThresholdService.getAll();
            /*Map<String,List<LossThreshold>> map=new HashMap<>();
            for(LeverCoin leverCoin:leverCoinList){
                if(!map.containsKey(leverCoin.getSymbol())){
                    map.put(leverCoin.getSymbol(),new ArrayList<>());
                }
                if(lossThresholdList!=null&&lossThresholdList.size()>0){
                    for(LossThreshold lossThreshold:lossThresholdList){
                        if(lossThreshold.getLeverCoin().getSymbol().equalsIgnoreCase(leverCoin.getSymbol())){
                            map.get(leverCoin.getSymbol()).add(lossThreshold);
                        }
                    }
                }
            }
            result.setData(map);*/
            result.setData(lossThresholdList);
        }
        return result;
    }

    @RequiresPermissions("lossThreshold:all")
    @PostMapping("create")
    @AccessLog(module = AdminModule.MEMBER, operation = "创建亏损阈值")
    public MessageResult create(@RequestParam("coinUnit") String leverCoinSymbol, @RequestParam("threshold")BigDecimal threshold,
                                @RequestParam("performActions")PerformActionsEnum performActions){
        Assert.isTrue(leverCoinSymbol!=null&&threshold!=null&&performActions!=null,"缺少必要参数");
        if(threshold.compareTo(new BigDecimal("100"))<=0){
            return MessageResult.error("风险率必须高于100%");
        }
        LeverCoin leverCoin=leverCoinService.getBySymbol(leverCoinSymbol);
        Assert.notNull(leverCoin,"leverCoinSymbol不存在");
        LossThreshold oldData=lossThresholdService.findByLeverCoinAndThreshold(leverCoin,threshold);
        Assert.isNull(oldData,leverCoinSymbol+"已经设置过这种比例的阈值");
        LossThreshold lossThreshold=new LossThreshold();
        lossThreshold.setLeverCoin(leverCoin);
        lossThreshold.setPerformActions(performActions);
        lossThreshold.setThreshold(threshold);
        lossThreshold.setCreateTime(new Date());
        lossThresholdService.save(lossThreshold);
        return MessageResult.success();
    }

    @RequiresPermissions("lossThreshold:all")
    @PatchMapping("update")
    @AccessLog(module = AdminModule.MEMBER, operation = "修改亏损阈值")
    public MessageResult update(@RequestParam("id")Long id, @RequestParam("threshold")BigDecimal threshold,
                                @RequestParam("performActions")PerformActionsEnum performActions,
                                @RequestParam("status")CommonStatus status){
        Assert.notNull(id,"缺少必要参数");
        Assert.isTrue(threshold!=null||performActions!=null||status!=null,"缺少参数");
        LossThreshold lossThreshold=lossThresholdService.findById(id);
        if(threshold!=null){
            if(threshold.compareTo(new BigDecimal(100))<=0){
                return MessageResult.error("风险率必须高于100%");
            }
            LossThreshold oldData=lossThresholdService.findByLeverCoinAndThreshold(lossThreshold.getLeverCoin(),threshold);
            Assert.isNull(oldData,lossThreshold.getLeverCoin().getSymbol()+"已经设置过这种比例的阈值");
            lossThreshold.setThreshold(threshold);
        }
        if(performActions!=null){
            lossThreshold.setPerformActions(performActions);
        }
        if(status!=null){
            lossThreshold.setStatus(status);
        }
        lossThreshold.setUpdateTime(new Date());
        lossThresholdService.save(lossThreshold);
        return MessageResult.success();
    }

    @RequiresPermissions("lossThreshold:all")
    @DeleteMapping("delete")
    @AccessLog(module = AdminModule.MEMBER, operation = "删除亏损阈值")
    public MessageResult delete(@RequestParam("id")Long id){
        Assert.notNull(id,"缺少参数");
        lossThresholdService.deleteById(id);
        return MessageResult.success();
    }

    /**
     * 查询借贷记录
     *
     * @param pageModel
     * @return
     */
    @RequiresPermissions("lossThreshold:all")
    @GetMapping("record")
    public MessageResult record(String userName,String symbol,BooleanEnum repayment, PageModel pageModel){
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(StringUtils.isNotBlank(userName)){
            booleanExpressions.add(QLoanRecord.loanRecord.memberName.like(userName));
        }
        if(StringUtils.isNotBlank(symbol)){
            booleanExpressions.add(QLoanRecord.loanRecord.leverCoin.symbol.eq(symbol));
        }
        if(repayment!=null){
            booleanExpressions.add(QLoanRecord.loanRecord.repayment.eq(repayment));
        }
        Predicate predicate= PredicateUtils.getPredicate(booleanExpressions);
        Page<LoanRecord> loanRecordList=loanRecordService.findAll(predicate,pageModel);
        MessageResult result=MessageResult.success();
        result.setData(loanRecordList);
        return result;
    }
}
