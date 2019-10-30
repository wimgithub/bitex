package com.spark.bitrade.controller.finance;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.QMember;
import com.spark.bitrade.entity.QMemberTransaction;
import com.spark.bitrade.model.screen.MemberTransactionScreen;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberTransactionService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.MemberTransactionExcelVO;
import com.spark.bitrade.vo.MemberTransactionVO;
import com.spark.bitrade.vo.OtcOrderVO;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 交易记录
 * @date 2018/1/17 17:07
 */
@RestController
@RequestMapping("/finance/member-transaction")
public class MemberTransactionController extends BaseAdminController {

    @Autowired
    private EntityManager entityManager;

    //查询工厂实体
    private JPAQueryFactory queryFactory;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @RequiresPermissions("finance:member-transaction:all")
    @PostMapping("/all")
    @AccessLog(module = AdminModule.FINANCE, operation = "所有交易记录MemberTransaction")
    public MessageResult all() {
        List<MemberTransaction> memberTransactionList = memberTransactionService.findAll();
        if (memberTransactionList != null && memberTransactionList.size() > 0)
            return success(memberTransactionList);
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("finance:member-transaction:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.FINANCE, operation = "交易记录MemberTransaction 详情")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        MemberTransaction memberTransaction = memberTransactionService.findOne(id);
        notNull(memberTransaction, "validate id!");
        return success(memberTransaction);
    }

    @RequiresPermissions(value = {"finance:member-transaction:page-query", "finance:member-transaction:page-query:recharge",
            "finance:member-transaction:page-query:check", "finance:member-transaction:page-query:fee"}, logical = Logical.OR)
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "分页查找交易记录MemberTransaction")
    public MessageResult pageQuery(
            PageModel pageModel,
            MemberTransactionScreen screen) {
        List<Predicate> predicates =new ArrayList<>();
        predicates.add(madePredicate(screen));
        Page<MemberTransactionVO> results = memberTransactionService.joinFind(predicates, pageModel);
        return success(results);
    }

    @RequiresPermissions("finance:member-transaction:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.FINANCE, operation = "导出交易记录MemberTransaction Excel")
    public void outExcel(PageModel pageModel, MemberTransactionScreen screen,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Predicate> predicates =new ArrayList<>();
        predicates.add(madePredicate(screen));
        List<MemberTransactionExcelVO> list = memberTransactionService.joinFindAll(predicates, pageModel);
        if(list.isEmpty()){
            return ;
        }
        ExcelUtil.listToExcel(list, MemberTransactionExcelVO.class.getDeclaredFields(),response.getOutputStream());
    }

    // 获得条件
    private List<BooleanExpression> getBooleanExpressionList(
            Date startTime, Date endTime, TransactionType type, Long memberId) {
        QMemberTransaction qEntity = QMemberTransaction.memberTransaction;
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        if (startTime != null)
            booleanExpressionList.add(qEntity.createTime.gt(startTime));
        if (endTime != null)
            booleanExpressionList.add(qEntity.createTime.lt(endTime));
        if (type != null)
            booleanExpressionList.add(qEntity.type.eq(type));
        if (memberId != null)
            booleanExpressionList.add(qEntity.memberId.eq(memberId));
        return booleanExpressionList;
    }

    private Predicate madePredicate(MemberTransactionScreen screen){
        List<BooleanExpression> booleanExpressionList=new ArrayList<>();
        if(screen.getMemberId()!=null)
            booleanExpressionList.add((QMember.member.id.eq(screen.getMemberId())));
        if (!StringUtils.isEmpty(screen.getAccount()))
            booleanExpressionList.add(QMember.member.username.like("%"+screen.getAccount()+"%")
                    .or(QMember.member.realName.like("%"+screen.getAccount()+"%")));
        if (screen.getStartTime() != null)
            booleanExpressionList.add(QMemberTransaction.memberTransaction.createTime.goe(screen.getStartTime()));
        if (screen.getEndTime() != null){
            booleanExpressionList.add(QMemberTransaction.memberTransaction.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }
        if (screen.getType() != null)
            booleanExpressionList.add(QMemberTransaction.memberTransaction.type.eq(screen.getType()));

        if(screen.getMinMoney()!=null)
            booleanExpressionList.add(QMemberTransaction.memberTransaction.amount.goe(screen.getMinMoney()));

        if(screen.getMaxMoney()!=null)
            booleanExpressionList.add(QMemberTransaction.memberTransaction.amount.loe(screen.getMaxMoney()));

        if(screen.getMinFee()!=null)
            booleanExpressionList.add(QMemberTransaction.memberTransaction.fee.goe(screen.getMinFee()));

        if(screen.getMaxFee()!=null)
            booleanExpressionList.add(QMemberTransaction.memberTransaction.fee.loe(screen.getMaxFee()));

        if(screen.getSymbol()!=null&&!screen.getSymbol().equalsIgnoreCase("")){
            booleanExpressionList.add(QMemberTransaction.memberTransaction.symbol.eq(screen.getSymbol()));
        }
        if(screen.getOnlyFee()!=null&&screen.getOnlyFee()){
            booleanExpressionList.add(QMemberTransaction.memberTransaction.fee.gt(BigDecimal.ZERO));
        }
        Predicate predicate= PredicateUtils.getPredicate(booleanExpressionList);
        return predicate;
    }

    private void madeData(List<MemberTransaction> memberTransactions){
        for(MemberTransaction memberTransaction:memberTransactions){
            memberTransaction.setTypeCN(memberTransaction.getType().getCnName());
        }
    }
}
