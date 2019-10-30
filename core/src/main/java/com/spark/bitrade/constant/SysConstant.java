package com.spark.bitrade.constant;

/**
 * 系统常量
 *
 * @author Zhang Jinwei
 * @date 2017年12月18日
 */
public class SysConstant {
    /**
     * session常量
     */
    public static final String SESSION_ADMIN = "ADMIN_MEMBER";

    public static final String SESSION_MEMBER = "API_MEMBER";

    /**
     * 验证码
     */
    public static final String PHONE_REG_CODE_PREFIX = "PHONE_REG_CODE_";

    public static final String PHONE_RESET_TRANS_CODE_PREFIX = "PHONE_RESET_TRANS_CODE_";

    public static final String PHONE_BIND_CODE_PREFIX = "PHONE_BIND_CODE_";

    public static final String PHONE_UPDATE_PASSWORD_PREFIX = "PHONE_UPDATE_PASSWORD_";

    public static final String PHONE_ADD_ADDRESS_PREFIX = "PHONE_ADD_ADDRESS_";

    public static final String EMAIL_BIND_CODE_PREFIX = "EMAIL_BIND_CODE_";
    /**
     * 解绑邮箱验证码
     */
    public static final String EMAIL_UNTIE_CODE_PREFIX = "EMAIL_UNTIE_CODE_";
    /**
     * 换绑邮箱验证码
     */
    public static final String EMAIL_UPDATE_CODE_PREFIX = "EMAIL_UPDATE_CODE_";

    public static final String ADD_ADDRESS_CODE_PREFIX = "ADD_ADDRESS_CODE_";
    public static final String RESET_PASSWORD_CODE_PREFIX = "RESET_PASSWORD_CODE_";
    public static final String PHONE_CHANGE_CODE_PREFIX = "PHONE_CHANGE_CODE_";

    public static final String ADMIN_LOGIN_PHONE_PREFIX = "ADMIN_LOGIN_PHONE_PREFIX_";

    public static final String ADMIN_COIN_REVISE_PHONE_PREFIX = "ADMIN_COIN_REVISE_PHONE_PREFIX_";
    public static final String ADMIN_COIN_TRANSFER_COLD_PREFIX = "ADMIN_COIN_TRANSFER_COLD_PREFIX_";
    public static final String ADMIN_EXCHANGE_COIN_SET_PREFIX = "ADMIN_EXCHANGE_COIN_SET_PREFIX_";

    /** 防攻击验证 */
    public static final String ANTI_ATTACK_ = "ANTI_ATTACK_";
    /**
     * 空投锁
     */
    public static final String HANDLE_AIRDROP_LOCK="HANDLE_AIRDROP_LOCK_";
    /**
     * 登录锁，连续账号密码错误时启用
     */
    public static final String LOGIN_LOCK="LOGIN_LOCK_";

    public final static String usdt_husd="USDT/HUSD";
}
