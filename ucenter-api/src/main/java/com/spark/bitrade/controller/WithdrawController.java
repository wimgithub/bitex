package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.ScanMemberAddress;
import com.spark.bitrade.entity.WithdrawWalletInfo;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.Md5;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.spark.bitrade.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.BigDecimalUtils.*;
import static org.springframework.util.Assert.*;

@RestController
@Slf4j
@RequestMapping(value = "/withdraw", method = RequestMethod.POST)
public class WithdrawController {
    @Autowired
    private MemberAddressService memberAddressService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private WithdrawRecordService withdrawApplyService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Autowired
    private MemberTransactionService memberTransactionService ;

    /**
     * 增加提现地址
     * @param address
     * @param unit
     * @param remark
     * @param code
     * @param aims
     * @param user
     * @return
     */
    @RequestMapping("address/add")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addAddress(String address, String unit, String remark, String code, String aims, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        hasText(address, sourceService.getMessage("MISSING_COIN_ADDRESS"));
        hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
        hasText(code, sourceService.getMessage("MISSING_VERIFICATION_CODE"));
        hasText(aims, sourceService.getMessage("MISSING_PHONE_OR_EMAIL"));
        Assert.isTrue(ValidateUtil.isAddress(address),sourceService.getMessage("ERROR_COIN_ADDRESS"));
        Coin coin = coinService.findByUnit(unit);
        address=address.trim();
        List<MemberAddress> memberAddress = memberAddressService.findByMemberIdAndCoinAndAddress(user.getId(),coin,address,CommonStatus.NORMAL);
        if(memberAddress!=null && memberAddress.size()>0) {
            return MessageResult.error("该地址已经存在，请确认地址");
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Member member = memberService.findOne(user.getId());
        if (member.getMobilePhone() != null && aims.equals(member.getMobilePhone())) {
            Object info = valueOperations.get(SysConstant.PHONE_ADD_ADDRESS_PREFIX + member.getMobilePhone());
            if(info==null){
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
            }
            if (!info.toString().equals(code)) {
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.PHONE_ADD_ADDRESS_PREFIX + member.getMobilePhone());
            }
        } else if (member.getEmail() != null && aims.equals(member.getEmail())) {
            Object info = valueOperations.get(SysConstant.ADD_ADDRESS_CODE_PREFIX + member.getEmail());
            if(info==null){
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
            }
            if (!info.toString().equals(code)) {
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.ADD_ADDRESS_CODE_PREFIX + member.getEmail());
            }
        } else {
            return MessageResult.error(sourceService.getMessage("ADD_ADDRESS_FAILED"));
        }
        MessageResult result = memberAddressService.addMemberAddress(user.getId(), address, unit, remark);
        if (result.getCode() == 0) {
            result.setMessage(sourceService.getMessage("ADD_ADDRESS_SUCCESS"));
        } else if (result.getCode() == 500) {
            result.setMessage(sourceService.getMessage("ADD_ADDRESS_FAILED"));
        } else if (result.getCode() == 600) {
            result.setMessage(sourceService.getMessage("COIN_NOT_SUPPORT"));
        }
        return result;
    }

    /**
     * 删除提现地址
     * @param id
     * @param user
     * @return
     */
    @RequestMapping("address/delete")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deleteAddress(long id, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = memberAddressService.deleteMemberAddress(user.getId(), id);
        if (result.getCode() == 0) {
            result.setMessage(sourceService.getMessage("DELETE_ADDRESS_SUCCESS"));
        } else {
            result.setMessage(sourceService.getMessage("DELETE_ADDRESS_FAILED"));
        }
        return result;
    }

    /**
     * 提现地址分页信息
     * @param user
     * @param pageNo
     * @param pageSize
     * @param unit
     * @return
     */
    @RequestMapping("address/page")
    public MessageResult addressPage(@SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize, String unit) {
        Page<MemberAddress> page = memberAddressService.pageQuery(pageNo, pageSize, user.getId(), unit);
        Page<ScanMemberAddress> scanMemberAddresses = page.map(x -> ScanMemberAddress.toScanMemberAddress(x));
        MessageResult result = MessageResult.success();
        result.setData(scanMemberAddresses);
        return result;
    }

    /**
     * 支持提现的地址
     * @return
     */
    @RequestMapping("support/coin")
    public MessageResult queryWithdraw() {
        List<Coin> list = coinService.findAllCanWithDraw();
        List<String> list1 = new ArrayList<>();
        list.stream().forEach(x -> list1.add(x.getUnit()));
        MessageResult result = MessageResult.success();
        result.setData(list1);
        return result;
    }

    /**
     * 提现币种详细信息
     * @param user
     * @return
     */
    @RequestMapping("support/coin/info")
    public MessageResult queryWithdrawCoin(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        List<Coin> list = coinService.findAllCanWithDraw();
        List<MemberWallet> list1 = memberWalletService.findAllByMemberId(user.getId());
        long id = user.getId();
        List<WithdrawWalletInfo> list2 = list1.stream().filter(x -> list.contains(x.getCoin())).map(x ->
                WithdrawWalletInfo.builder()
                        .balance(x.getBalance())
                        .withdrawScale(x.getCoin().getWithdrawScale())
                        .maxTxFee(x.getCoin().getMaxTxFee())
                        .minTxFee(x.getCoin().getMinTxFee())
                        .minAmount(x.getCoin().getMinWithdrawAmount())
                        .maxAmount(x.getCoin().getMaxWithdrawAmount())
                        .name(x.getCoin().getName())
                        .nameCn(x.getCoin().getNameCn())
                        .threshold(x.getCoin().getWithdrawThreshold())
                        .unit(x.getCoin().getUnit())
                        .canAutoWithdraw(x.getCoin().getCanAutoWithdraw())
                        .addresses(memberAddressService.queryAddress(id, x.getCoin().getName())).build()
        ).collect(Collectors.toList());
        MessageResult result = MessageResult.success();
        result.setData(list2);
        return result;
    }

    /**
     * 申请提币
     * @param user
     * @param unit
     * @param address
     * @param amount
     * @param fee
     * @param remark
     * @param jyPassword
     * @return
     * @throws Exception
     */
    @RequestMapping("apply")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult withdraw(@SessionAttribute(SESSION_MEMBER) AuthMember user, String unit, String address,
                                  BigDecimal amount, BigDecimal fee,String remark,String jyPassword) throws Exception {
        hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
        hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
        address=address.trim();
        Assert.isTrue(ValidateUtil.isAddress(address),sourceService.getMessage("ERROR_COIN_ADDRESS"));
        Coin coin = coinService.findByUnit(unit);
        amount.setScale(coin.getWithdrawScale(),BigDecimal.ROUND_DOWN);
        notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));
        isTrue(coin.getStatus().equals(CommonStatus.NORMAL) && coin.getCanWithdraw().equals(BooleanEnum.IS_TRUE), sourceService.getMessage("COIN_NOT_SUPPORT"));
        isTrue(compare(fee, new BigDecimal(String.valueOf(coin.getMinTxFee()))), sourceService.getMessage("CHARGE_MIN") + coin.getMinTxFee());
        isTrue(compare(new BigDecimal(String.valueOf(coin.getMaxTxFee())), fee), sourceService.getMessage("CHARGE_MAX") + coin.getMaxTxFee());
        isTrue(compare(coin.getMaxWithdrawAmount(), amount), sourceService.getMessage("WITHDRAW_MAX") + coin.getMaxWithdrawAmount());
        isTrue(compare(amount, coin.getMinWithdrawAmount()), sourceService.getMessage("WITHDRAW_MIN") + coin.getMinWithdrawAmount());
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
        isTrue(compare(memberWallet.getBalance(), amount), sourceService.getMessage("INSUFFICIENT_BALANCE"));
        isTrue(memberAddressService.findByMemberIdAndAddress(user.getId(), address).size() > 0, sourceService.getMessage("WRONG_ADDRESS"));
        isTrue(memberWallet.getIsLock()==BooleanEnum.IS_FALSE,"钱包已锁定");
        Member member = memberService.findOne(user.getId());
        String mbPassword = member.getJyPassword();
        isTrue(member.getMemberLevel()!=MemberLevelEnum.GENERAL,"请先进行实名认证!");
        Assert.hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(mbPassword), sourceService.getMessage("ERROR_JYPASSWORD"));
        MessageResult result = memberWalletService.freezeBalance(memberWallet, amount);
        if (result.getCode() != 0) {
            throw new InformationExpiredException("Information Expired");
        }
        WithdrawRecord withdrawApply = new WithdrawRecord();
        withdrawApply.setCoin(coin);
        withdrawApply.setFee(fee);
        withdrawApply.setArrivedAmount(sub(amount, fee));
        withdrawApply.setMemberId(user.getId());
        withdrawApply.setTotalAmount(amount);
        withdrawApply.setAddress(address);
        withdrawApply.setRemark(remark);
        withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());

        //提币数量低于或等于阈值并且该币种支持自动提币
        if (amount.compareTo(coin.getWithdrawThreshold()) <= 0 && coin.getCanAutoWithdraw().equals(BooleanEnum.IS_TRUE)) {
            //TODO 查询今天已经提现的数量（除了“失败”之外的所有记录）
            Double withAmountSum=sumDailyWithdraw(coin);
            //如果币种设置了单日最大提币量，并且当天已申请的数量（包括待审核、待放币、成功、转账中状态的所有记录）加上当前提币量大于每日最大提币量
            // 进入人工审核
            if(coin.getMaxDailyWithdrawRate()!=null&&coin.getMaxDailyWithdrawRate().compareTo(BigDecimal.ZERO)>0
                    &&coin.getMaxDailyWithdrawRate().compareTo(new BigDecimal(withAmountSum).add(amount))<0){
                withdrawApply.setStatus(WithdrawStatus.PROCESSING);
                withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
                if (withdrawApplyService.save(withdrawApply) != null) {
                    return MessageResult.success(sourceService.getMessage("APPLY_AUDIT"));
                } else {
                    throw new InformationExpiredException("Information Expired");
                }
            }else{
                withdrawApply.setStatus(WithdrawStatus.WAITING);
                withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
                withdrawApply.setDealTime(withdrawApply.getCreateTime());
                WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);
                JSONObject json = new JSONObject();
                json.put("uid", user.getId());
                //提币总数量
                json.put("totalAmount", amount);
                //手续费
                json.put("fee", fee);
                //预计到账数量
                json.put("arriveAmount", sub(amount, fee));
                //币种
                json.put("coin", coin);
                //提币地址
                json.put("address", address);
                //提币记录id
                json.put("withdrawId", withdrawRecord.getId());
                json.put("remark",remark);
                kafkaTemplate.send("withdraw", coin.getUnit(), json.toJSONString());
                return MessageResult.success(sourceService.getMessage("APPLY_SUCCESS"));
            }
        } else {
            withdrawApply.setStatus(WithdrawStatus.PROCESSING);
            withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
            if (withdrawApplyService.save(withdrawApply) != null) {
                return MessageResult.success(sourceService.getMessage("APPLY_AUDIT"));
            } else {
                throw new InformationExpiredException("Information Expired");
            }
        }
    }


    /**
     * 提币记录
     * @param user
     * @return
     */
    @GetMapping("record")
    public MessageResult pageWithdraw(@SessionAttribute(SESSION_MEMBER) AuthMember user, PageModel pageModel,
                                      String unit) {
        MessageResult mr = new MessageResult(0, "success");
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        booleanExpressions.add(QWithdrawRecord.withdrawRecord.memberId.eq(user.getId()));
        if (!StringUtils.isEmpty(unit)) {
            booleanExpressions.add(QWithdrawRecord.withdrawRecord.coin.unit.eq(unit));
        }
        Predicate predicate=PredicateUtils.getPredicate(booleanExpressions);
        Page<WithdrawRecord> records = withdrawApplyService.findAll(predicate,pageModel);
        records.map(x -> ScanWithdrawRecord.toScanWithdrawRecord(x));
        mr.setData(records);
        return mr;
    }

    /**
     * 当日已申请数量
     * @return
     */
    @GetMapping("todayWithdrawSum")
    public MessageResult todayWithdrawSum(@SessionAttribute(SESSION_MEMBER) AuthMember user,String symbol){
        if(StringUtils.isEmpty(symbol)){
            return MessageResult.error("symbol is not null");
        }
        Coin coin=coinService.findByUnit(symbol);
        if(coin==null){
            return MessageResult.error("coin has not found");
        }
        Double withAmountSum=sumDailyWithdraw(coin);
        MessageResult result=MessageResult.success();
        result.setData(withAmountSum);
        return result;
    }

    private Double sumDailyWithdraw(Coin coin){
        Date endTime=new Date();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE,-1);
        Date startTime=calendar.getTime();
        Double withAmountSum=withdrawApplyService.countWithdrawAmountByTimeAndMemberIdAndCoin(startTime,endTime,coin);
        if(withAmountSum==null){
            withAmountSum=0.0;
        }
        return withAmountSum;
    }

}
