package com.spark.bitrade.entity;

import com.spark.bitrade.constant.MemberLevelEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author Zhang Jinwei
 * @date 2018年01月31日
 */
@Data
@Builder
public class LoginInfo {
    private String username;
    private Location location;
    private MemberLevelEnum memberLevel;
    private String token;
    private String realName;
    private Country country;
    private String avatar;
    private String promotionCode;
    private long id;
    /**
     * 推广地址前缀
     */
    private String promotionPrefix;

    /**
     * 签到能力
     */
    private Boolean signInAbility;

    /**
     * 是否存在签到活动
     */
    private Boolean signInActivity;

    public static LoginInfo getLoginInfo(Member member, String token, Boolean signInActivity, String prefix) {
        return LoginInfo.builder().location(member.getLocation())
                .memberLevel(member.getMemberLevel())
                .username(member.getUsername())
                .token(token)
                .realName(member.getRealName())
                .country(member.getCountry())
                .avatar(member.getAvatar())
                .promotionCode(member.getPromotionCode())
                .id(member.getId())
                .promotionPrefix(prefix)
                .signInAbility(member.getSignInAbility())
                .signInActivity(signInActivity)
                .build();

    }
}
