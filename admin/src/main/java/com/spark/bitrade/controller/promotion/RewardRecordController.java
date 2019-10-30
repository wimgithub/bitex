package com.spark.bitrade.controller.promotion;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.RewardRecord;
import com.spark.bitrade.model.screen.RewardRecordScreen;
import com.spark.bitrade.service.RewardRecordService;
import com.spark.bitrade.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("promotion/reward-record")
public class RewardRecordController extends BaseAdminController{

    @Autowired
    private RewardRecordService rewardRecordService ;

    @PostMapping("page-query")
    @RequiresPermissions("promotion:reward-record:page-query")
    public MessageResult page(PageModel pageModel, RewardRecordScreen screen){
        Predicate predicate = screen.getPredicate();
        Page<RewardRecord> page = rewardRecordService.findAll(predicate,pageModel);
        return success(page);
    }
}
