package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bipay.constant.CoinType;
import com.spark.bipay.entity.Address;
import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.GeneratorUtil;
import com.spark.bitrade.util.MessageResult;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MemberConsumer {
    private Logger logger = LoggerFactory.getLogger(MemberConsumer.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private BiPayService biPayService;
    private ExecutorService executor = Executors.newFixedThreadPool(30);

    /**
     * 重置用户钱包地址
     * @param record
     */
    @KafkaListener(topics = {"reset-member-address"})
    public void resetAddress(ConsumerRecord<String,String> record){
        logger.info("handle member-register,key={},value={}", record.key(),record.value());
        String content = record.value();
        JSONObject json = JSON.parseObject(content);
        Coin coin = coinService.findByUnit(record.key());
        Assert.notNull(coin,"coin null");
        if(coin.getEnableRpc()==BooleanEnum.IS_TRUE){
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(record.key(),json.getLong("uid"));
            Assert.notNull(memberWallet,"wallet null");
            initWalletAddress(coin, memberWallet);
            memberWalletService.save(memberWallet);
        }
    }

    public  void initWalletAddress(Coin coin, MemberWallet wallet){
        if(coin.getIsErcToken() == BooleanEnum.IS_TRUE){
            MemberWallet ethWallet = memberWalletService.findByCoinUnitAndMemberId("ETH",wallet.getMemberId());
            wallet.setAddress(ethWallet.getAddress());
        }
        else if(coin.getName().equalsIgnoreCase("USDT")){
            MemberWallet btcWallet = memberWalletService.findByCoinUnitAndMemberId("BTC",wallet.getMemberId());
            wallet.setAddress(btcWallet.getAddress());
        }
        else if(biPayService.isSupportedCoin(coin.getUnit())){
            Address address = biPayService.createCoinAddress(CoinType.valueOf(coin.getName()));
            wallet.setAddress(address.getAddress());
        }
        else if(StringUtils.isNotEmpty(coin.getMasterAddress())){
            //当使用一个主账户时不取rpc
            wallet.setAddress(coin.getMasterAddress()+":"+wallet.getMemberId());
        }
        else {
            String account = "U" + wallet.getMemberId() + GeneratorUtil.getNonceString(4);
            //远程RPC服务URL,后缀为币种单位
            String serviceName = "SERVICE-RPC-" + coin.getUnit();
            try {
                String url = "http://" + serviceName + "/rpc/address/{account}";
                ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                logger.info("remote call:service={},result={}", serviceName, result);
                if (result.getStatusCode().value() == 200) {
                    MessageResult mr = result.getBody();
                    if (mr.getCode() == 0) {
                        String address = mr.getData().toString();
                        wallet.setAddress(address);
                    }
                }
            } catch (Exception e) {
                logger.error("call {} failed,error={}", serviceName, e.getMessage());
            }
        }
    }

    public MemberWallet initWallet(Long memberId,Coin coin){
        MemberWallet wallet = new MemberWallet();
        wallet.setCoin(coin);
        wallet.setMemberId(memberId);
        wallet.setBalance(new BigDecimal(0));
        wallet.setFrozenBalance(new BigDecimal(0));
        wallet.setAddress("");
        if(coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
            initWalletAddress(coin,wallet);
        }
        //保存
        return memberWalletService.save(wallet);
    }


    /**
     * 客户注册消息
     * @param content
     */
    @KafkaListener(topics = {"member-register"})
    public void handle(String content) {
        logger.info("handle member-register,data={}", content);
        if (StringUtils.isEmpty(content)) return;
        JSONObject json = JSON.parseObject(content);
        if(json == null)return ;
        //获取所有支持的币种
        List<Coin> coins =  coinService.findAll();
        String btcAddress = "";
        String ethAddress = "";
        Iterator<Coin> iterator = coins.iterator();
        while (iterator.hasNext()){
            Coin coin = iterator.next();
            if(coin.getUnit().equalsIgnoreCase("BTC")){
                MemberWallet wallet = initWallet(json.getLong("uid"),coin);
                btcAddress = wallet.getAddress();
                iterator.remove();
            }
            else if(coin.getUnit().equalsIgnoreCase("ETH")){
                MemberWallet wallet = initWallet(json.getLong("uid"),coin);
                ethAddress = wallet.getAddress();
                iterator.remove();
            }
        }
        for(Coin coin:coins) {
            logger.info("memberId:{},unit:{}",json.getLong("uid"),coin.getUnit());
            MemberWallet wallet = new MemberWallet();
            wallet.setCoin(coin);
            wallet.setMemberId(json.getLong("uid"));
            wallet.setBalance(new BigDecimal(0));
            wallet.setFrozenBalance(new BigDecimal(0));
            if(coin.getUnit().equalsIgnoreCase("USDT")){
                wallet.setAddress(btcAddress);
            }
            else if(coin.getIsErcToken() == BooleanEnum.IS_TRUE){
                wallet.setAddress(ethAddress);
            }
            else if(coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
                initWalletAddress(coin,wallet);
            }
            else{
                wallet.setAddress("");
            }
            //保存
            memberWalletService.save(wallet);
        }
        //注册活动奖励
        RewardActivitySetting rewardActivitySetting = rewardActivitySettingService.findByType(ActivityRewardType.REGISTER);
        if (rewardActivitySetting!=null){
            MemberWallet memberWallet=memberWalletService.findByCoinAndMemberId(rewardActivitySetting.getCoin(),json.getLong("uid"));

            BigDecimal amount3=JSONObject.parseObject(rewardActivitySetting.getInfo()).getBigDecimal("amount");
            if (memberWallet==null || amount3.compareTo(BigDecimal.ZERO) <= 0){return;}
            memberWallet.setBalance(BigDecimalUtils.add(memberWallet.getBalance(),amount3));
            memberWalletService.save(memberWallet);
            Member member = memberService.findOne(json.getLong("uid"));
            RewardRecord rewardRecord3 = new RewardRecord();
            rewardRecord3.setAmount(amount3);
            rewardRecord3.setCoin(rewardActivitySetting.getCoin());
            rewardRecord3.setMember(member);
            rewardRecord3.setRemark(rewardActivitySetting.getType().getCnName());
            rewardRecord3.setType(RewardRecordType.ACTIVITY);
            rewardRecordService.save(rewardRecord3);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(amount3);
            memberTransaction.setSymbol(rewardActivitySetting.getCoin().getUnit());
            memberTransaction.setType(TransactionType.ACTIVITY_AWARD);
            memberTransaction.setMemberId(member.getId());
            memberTransactionService.save(memberTransaction);
        }
    }

    public class MemberWalletCreateThread implements Runnable{
        private Coin coin;
        private List<Member> members;

        public MemberWalletCreateThread(List<Member> memberList,Coin coin){
            this.coin = coin;
            this.members = memberList;
        }


        @Override
        public void run() {
            members.forEach(member -> {
                MemberWallet wallet = memberWalletService.findByCoinAndMember(coin, member);
                if (wallet == null) {
                    wallet = new MemberWallet();
                    wallet.setCoin(coin);
                    wallet.setMemberId(member.getId());
                    wallet.setBalance(new BigDecimal(0));
                    wallet.setFrozenBalance(new BigDecimal(0));
                    if(coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
                        initWalletAddress(coin, wallet);
                    }
                    else{
                        wallet.setAddress("");
                    }
                    memberWalletService.save(wallet);
                }
            });
        }
    }


    public class MemberWalletResetThread implements Runnable{
        private Coin coin;
        private List<Member> members;

        public MemberWalletResetThread(List<Member> memberList,Coin coin){
            this.coin = coin;
            this.members = memberList;
        }


        @Override
        public void run() {
            members.forEach(member -> {
                MemberWallet wallet = memberWalletService.findByCoinAndMember(coin, member);
                if(wallet !=  null) {
                    if (coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
                        initWalletAddress(coin, wallet);
                    } else {
                        wallet.setAddress("");
                    }
                    memberWalletService.save(wallet);
                }
            });
        }
    }

    /**
     * 创建新币种会员钱包
     * @param record
     */
    @KafkaListener(topics = {"create-coin"})
    public void createCoin(ConsumerRecord<String,String> record){
        Coin coin = JSON.parseObject(record.value(),Coin.class);
        long count = memberService.count();
        int pageSize = 5000;
        logger.info("=====生成会员钱包，总会员数{}，线程数{}",count,count/pageSize + 1);
        for(int page = 0;page*pageSize < count;page++){
            Page<Member> memberPage = memberService.page(page,pageSize);
            executor.execute(new MemberWalletCreateThread(memberPage.getContent(),coin));
        }
    }

    /**
     * 重置币种钱包地址
     * @param record
     */
    @KafkaListener(topics = {"reset-wallet"})
    public void resetWallet(ConsumerRecord<String,String> record){
        Coin coin = JSON.parseObject(record.value(),Coin.class);
        long count = memberService.count();
        int pageSize = 5000;
        logger.info("=====重置会员钱包地址，总会员数{}，线程数{}",count,count/pageSize + 1);
        for(int page = 0;page*pageSize < count;page++){
            Page<Member> memberPage = memberService.page(page,pageSize);
            executor.execute(new MemberWalletResetThread(memberPage.getContent(),coin));
        }
    }
}
