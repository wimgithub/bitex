package com.spark.bitrade.controller;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import com.spark.bitrade.constant.SysHelpClassification;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ancillary")
@Slf4j
public class AideController {
    @Autowired
    private WebsiteInformationService websiteInformationService;

    @Autowired
    private SysAdvertiseService sysAdvertiseService;

    @Autowired
    private SysHelpService sysHelpService;
    @Autowired
    private AppRevisionService appRevisionService;

    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * 站点信息
     *
     * @return
     */
    @RequestMapping("/website/info")
    public MessageResult keyWords() {
        WebsiteInformation websiteInformation = websiteInformationService.fetchOne();
        MessageResult result = MessageResult.success();
        result.setData(websiteInformation);
        return result;
    }

    /**
     * 系统广告
     *
     * @return
     */
    @RequestMapping("/system/advertise")
    public MessageResult sysAdvertise(
            @RequestParam(value = "sysAdvertiseLocation", required = false) SysAdvertiseLocation sysAdvertiseLocation,
            @RequestParam(value = "sysLanguage",required = false,defaultValue = "zh") String sysLanguage) {
        List<SysAdvertise> list = sysAdvertiseService.findAllNormal(sysAdvertiseLocation,sysLanguage);
        MessageResult result = MessageResult.success();
        result.setData(list);
        return result;
    }


    /**
     * 系统帮助
     *
     * @return
     */
    @RequestMapping("/system/help")
    public MessageResult sysHelp(@RequestParam(value = "sysHelpClassification", required = false) SysHelpClassification sysHelpClassification,
                                 @RequestParam(value = "sysLanguage",required = false,defaultValue = "zh") String sysLanguage) {
        List<SysHelp> list = null;
        Sort sort = new Sort(Sort.Direction.DESC,"sort");
        List<BooleanExpression> booleanExpressionList=new ArrayList<>();
        booleanExpressionList.add(QSysHelp.sysHelp.sysLanguage.eq(sysLanguage));
        if (sysHelpClassification == null) {
            Predicate predicate= PredicateUtils.getPredicate(booleanExpressionList);
            list = sysHelpService.findAll(predicate,sort);
        } else {
            booleanExpressionList.add(QSysHelp.sysHelp.sysHelpClassification.eq(sysHelpClassification));
            Predicate predicate=PredicateUtils.getPredicate(booleanExpressionList);
            list=sysHelpService.findAll(predicate,sort);
            //list = sysHelpService.findBySysHelpClassification(sysHelpClassification);
        }
        MessageResult result = MessageResult.success();
        result.setData(list);
        return result;
    }

    /**
     * 系统帮助详情
     *
     * @param id
     * @return
     */
    @RequestMapping("/system/help/{id}")
    public MessageResult sysHelp(@PathVariable(value = "id") long id) {
        //List<SysHelp> list = sysHelpService.findBySysHelpClassification(sysHelpClassification);
        SysHelp sysHelp = sysHelpService.findOne(id);
        MessageResult result = MessageResult.success();
        result.setData(sysHelp);
        return result;
    }

    /**
     * 移动版本号
     *
     * @param platform 0:安卓 1:苹果
     * @return
     */
    @RequestMapping("/system/app/version/{id}")
    public MessageResult sysHelp(@PathVariable(value = "id") Platform platform) {

        AppRevision revision = appRevisionService.findRecentVersion(platform);
        if(revision != null){
            MessageResult result = MessageResult.success();
            result.setData(revision);
            return result;
        }
        else{
            return MessageResult.error(msService.getMessage("NO_UPDATE"));
        }
    }

}
