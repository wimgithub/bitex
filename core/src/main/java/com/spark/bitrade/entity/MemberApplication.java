package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.AuditStatus;
import com.spark.bitrade.enums.CredentialsType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author rongyu
 * @description 会员审核信息
 * @date 2017/12/26 14:35
 */
@Entity
@Table(name = "member_application")
@Data
public class MemberApplication {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String realName;
    private String idCard;
    //认证类型，0：身份证认证，1：护照认证，2:驾照认证
    @Column(columnDefinition = "int default 0 comment '认证类型'")
    private CredentialsType type;
    /**
     * 证件 正面
     */
    @NotBlank(message = "证件正面图片不能为空")
    private String identityCardImgFront;
    /**
     * 证件 反面
     */
    @NotBlank(message = "证件反面图片不能为空")
    private String identityCardImgReverse;
    /**
     * 证件 手持
     */
    @NotBlank(message = "手持证件图片不能为空")
    private String identityCardImgInHand;

    /**
     *  审核状态
     */
    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private AuditStatus auditStatus;

    /**
     * 审核信息所有者
     */
    @JoinColumn(name = "member_id",nullable = false)
    @ManyToOne
    private Member member;

    /**
     * 驳回理由
     */
    private String rejectReason;

    /**
     * 创建时间
     */

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 审核时间
     */
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
