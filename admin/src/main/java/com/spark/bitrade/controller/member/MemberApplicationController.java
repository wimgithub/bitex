package com.spark.bitrade.controller.member;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.AuditStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.model.screen.MemberApplicationScreen;
import com.spark.bitrade.entity.MemberApplication;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberApplicationService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.entity.QMemberApplication.memberApplication;
import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 实名审核单
 * @date 2017/12/26 15:05
 */
@RestController
@RequestMapping("member/member-application")
public class MemberApplicationController extends BaseAdminController {

    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("member:member-application:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.MEMBER, operation = "所有会员MemberApplication认证信息")
    public MessageResult all() {
        List<MemberApplication> all = memberApplicationService.findAll();
        if (all != null && all.size() > 0)
            return success(all);
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("member:member-application:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证信息详情")
    public MessageResult detail(@RequestParam("id") Long id) {
        MemberApplication memberApplication = memberApplicationService.findOne(id);
        notNull(memberApplication, "validate id!");
        return success(memberApplication);
    }

    @RequiresPermissions("member:member-application:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.MEMBER, operation = "分页查找会员MemberApplication认证信息")
    public MessageResult queryPage(PageModel pageModel, MemberApplicationScreen screen) {
        List<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getAuditStatus() != null)
            booleanExpressions.add(memberApplication.auditStatus.eq(screen.getAuditStatus()));
        if (!StringUtils.isEmpty(screen.getAccount()))
            booleanExpressions.add(memberApplication.member.username.like("%" + screen.getAccount() + "%")
                    .or(memberApplication.realName.like("%" + screen.getAccount() + "%")));
        if(!StringUtils.isEmpty(screen.getCardNo()))
            booleanExpressions.add(memberApplication.member.idNumber.like("%" + screen.getCardNo() + "%"));

        if(screen.getApproveStartTime()!=null){
            booleanExpressions.add(memberApplication.createTime.goe(screen.getApproveStartTime()));
        }
        if(screen.getApproveEndTime()!=null){
            booleanExpressions.add(memberApplication.createTime.loe(screen.getApproveEndTime()));
        }
        if(screen.getAuditStartTime()!=null){
            booleanExpressions.add(memberApplication.updateTime.goe(screen.getAuditStartTime()));
        }
        if(screen.getAuditEndTime()!=null){
            booleanExpressions.add(memberApplication.updateTime.loe(screen.getAuditEndTime()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<MemberApplication> all = memberApplicationService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("member:member-application:pass")
    @PatchMapping("{id}/pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证通过审核")
    public MessageResult pass(@PathVariable("id") Long id) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
        Assert.isTrue(application.getAuditStatus()== AuditStatus.AUDIT_ING,"该项审核已经被处理过，请刷新.....");
        //业务
        memberApplicationService.auditPass(application);
        //返回
        return success();
    }

    @RequiresPermissions("member:member-application:no-pass")
    @PatchMapping("{id}/no-pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证不通过审核")
    public MessageResult noPass(
            @PathVariable("id") Long id,
            @RequestParam(value = "rejectReason", required = false) String rejectReason) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
        Assert.isTrue(application.getAuditStatus()== AuditStatus.AUDIT_ING,"该项审核已经被处理过，请刷新.....");
        //业务
        application.setRejectReason(rejectReason);//拒绝原因
        memberApplicationService.auditNotPass(application);
        //返回
        return success();
    }
}
