package com.spark.bitrade.controller;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.event.MemberEvent;
import com.spark.bitrade.exception.AuthenticationException;
import com.spark.bitrade.service.*;
import com.spark.bitrade.system.GeetestLib;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.RequestUtil;
import com.spark.bitrade.util.ValidateUtil;
import com.spark.bitrade.vendor.provider.SMSProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

@RestController
@Slf4j
public class LoginController extends BaseController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberEvent memberEvent;
    @Autowired
    private LocaleMessageSourceService messageSourceService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private GeetestLib gtSdk;
    @Autowired
    private SignService signService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SMSProvider smsProvider;

    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private AccountService accountService;

    public static final String suffix="_address_book";

    @Value("${person.promote.prefix:}")
    private String promotePrefix;
    @Value("${system.ip138.api:}")
    private String ip138ApiUrl;
    @Value("${system.ip138.key:}")
    private String ip138Key;
    @Value("${system.ip138.value:}")
    private String ip138Value;
    @Value("${sms.driver}")
    private String driverName;
    @Value("${system.login.sms:0}")
    private Integer loginSms;

    @RequestMapping(value = "/login")
    public MessageResult login(HttpServletRequest request, String username, String password) {
        Assert.hasText(username, messageSourceService.getMessage("MISSING_USERNAME"));
        Assert.hasText(password, messageSourceService.getMessage("MISSING_PASSWORD"));
        String ip = getRemoteIp(request);
        String challenge = request.getParameter(GeetestLib.fn_geetest_challenge);
        String validate = request.getParameter(GeetestLib.fn_geetest_validate);
        String seccode = request.getParameter(GeetestLib.fn_geetest_seccode);

        String host=RequestUtil.remoteIp(request);
        log.info("host:"+host);
        //RequestUtil.getAreaDetail(ip138ApiUrl+host,ip138Key,ip138Value);
        //兼容没有极验证
        if (challenge == null && validate == null && seccode == null) {
            try {
                LoginInfo loginInfo = getLoginInfo(username, password, ip, request);
                return success(loginInfo);
            } catch (Exception e) {
                return error(e.getMessage());
            }
        }
        //从session中获取gt-server状态
        int gt_server_status_code = (Integer) request.getSession().getAttribute(gtSdk.gtServerStatusSessionKey);
        //从session中获取userid
        String userid = (String) request.getSession().getAttribute("userid");
        //自定义参数,可选择添加
        HashMap<String, String> param = new HashMap<>();
        param.put("user_id", userid); //网站用户id
        param.put("client_type", "web"); //web:电脑上的浏览器；h5:手机上的浏览器，包括移动应用内完全内置的web_view；native：通过原生SDK植入APP应用的方式
        param.put("ip_address", ip); //传输用户请求验证时所携带的IP

        int gtResult = 0;

        if (gt_server_status_code == 1) {
            //gt-server正常，向gt-server进行二次验证
            gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, param);
        } else {
            // gt-server非正常情况下，进行failback模式验证
            log.info("failback:use your own server captcha validate");
            gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
        }
        if (gtResult == 1) {
            // 验证成功
            try {
                LoginInfo loginInfo = getLoginInfo(username, password, ip, request);

                return success(loginInfo);
            } catch (Exception e) {
                return error(e.getMessage());
            }
        } else {
            // 验证失败
            return error(msService.getMessage("GEETEST_FAIL"));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected LoginInfo getLoginInfo(String username, String password, String ip, HttpServletRequest request) throws Exception {
        Member member = memberService.login(username, password);
        if(member==null){
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String key = SysConstant.LOGIN_LOCK + username;
            Object code = valueOperations.get(key);
            if(code==null){
                code=0;
            }
            Integer codeNum=(Integer)code;
            codeNum++;
            if(codeNum<10){
                valueOperations.set(key, codeNum, 3, TimeUnit.MINUTES);
            }else{
                memberService.lock(username);
            }
            throw new AuthenticationException("账号或密码错误");
        }
        //memberEvent.onLoginSuccess(member, ip);
        request.getSession().setAttribute(SysConstant.SESSION_MEMBER, AuthMember.toAuthMember(member));
        String token = request.getHeader("access-auth-token");
        if (!StringUtils.isBlank(token)){
            member.setToken(token);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, 24 * 7);
            member.setTokenExpireTime(calendar.getTime());
        }
        // 签到活动是否进行
        Sign sign = signService.fetchUnderway();
        LoginInfo loginInfo;
        if (sign == null)
            loginInfo = LoginInfo.getLoginInfo(member, request.getSession().getId(), false,promotePrefix);
        else
            loginInfo = LoginInfo.getLoginInfo(member, request.getSession().getId(), true,promotePrefix);

        if(loginSms==1&&member.getMobilePhone()!=null){
            String phone=member.getMobilePhone();
            //253国际短信，可以发国内号码，都要加上区域号
            if(driverName.equalsIgnoreCase("two_five_three")){
                smsProvider.sendLoginMessage(ip,member.getCountry().getAreaCode()+phone);
            }else{
                if (member.getCountry().getAreaCode().equals("86")) {
                    smsProvider.sendLoginMessage(ip,phone);
                } else {
                    smsProvider.sendLoginMessage(ip,member.getCountry().getAreaCode()+phone);
                }
            }
        }
        member.setLastLoginTime(new Date());
        memberService.save(member);
        return loginInfo;
    }

    /**
     * 登出
     *
     * @return
     */
    @RequestMapping(value = "/logout")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult loginOut(HttpServletRequest request, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        request.getSession().removeAttribute(SysConstant.SESSION_MEMBER);
        Member member = memberService.findOne(user.getId());
        member.setToken(null);
        return request.getSession().getAttribute(SysConstant.SESSION_MEMBER) != null ? error(messageSourceService.getMessage("LOGOUT_FAILED")) : success(messageSourceService.getMessage("LOGOUT_SUCCESS"));
    }

    /**
     * 检查是否登录
     *
     * @param request
     * @return
     */
    @RequestMapping("/check/login")
    public MessageResult checkLogin(HttpServletRequest request) {
        AuthMember authMember = (AuthMember) request.getSession().getAttribute(SESSION_MEMBER);
        MessageResult result = MessageResult.success();
        if (authMember != null) {
            result.setData(true);
        } else {
            result.setData(false);
        }
        return result;
    }

    /**
     * 检查mongodb漏记的地址
     * @return
     */
    @PostMapping("checkAddress")
    @ResponseBody
    public MessageResult checkAddress(){
        new Thread(){
            public void run() {
                String filePath = "/data/eth/keystore";
                //String filePath="E:\\workspace\\niubi\\data\\data\\eth\\keystore";
                File dir = new File(filePath);
                List<File> tempList = Arrays.asList(dir.listFiles());
                List<MemberWallet> memberWalletList = memberWalletService.findAll();
                Iterator<MemberWallet> ite = memberWalletList.iterator();
                while (ite.hasNext()) {
                    MemberWallet memberWallet = ite.next();
                    if (!StringUtils.isEmpty(memberWallet.getAddress())) {
                        Account account = accountService.findByAddress(memberWallet.getAddress(), memberWallet.getCoin().getUnit());
                        if (account == null) {
                            String minAddress = memberWallet.getAddress().substring(2);
                            log.info("minAddress={}", minAddress);
                            String fileName = null;
                            for (File file : tempList) {
                                if (file.getName().indexOf(minAddress) > 0) {
                                    fileName = file.getName();
                                }
                            }
                            if (fileName != null) {
                                log.info("memberId={},fileName={},address={},coinUnit={}", memberWallet.getMemberId(), fileName, memberWallet.getAddress(), memberWallet.getCoin().getUnit());
                                accountService.saveOne(memberWallet.getMemberId() + "", fileName, memberWallet.getAddress(), memberWallet.getCoin().getUnit());
                            }
                        }
                    }
                }
            }
        }.start();
        return MessageResult.success();
    }
}
