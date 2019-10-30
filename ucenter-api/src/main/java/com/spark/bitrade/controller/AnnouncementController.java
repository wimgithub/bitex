package com.spark.bitrade.controller;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.entity.Announcement;
import com.spark.bitrade.entity.QAnnouncement;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.AnnouncementService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("announcement")
public class AnnouncementController extends BaseController {
    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("page")
    public MessageResult page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sysLanguage",required = false,defaultValue = "zh") String sysLanguage
    ) {
        //条件
        ArrayList<Predicate> predicates = new ArrayList<>();
        predicates.add(QAnnouncement.announcement.isShow.eq(true));
        predicates.add(QAnnouncement.announcement.sysLanguage.eq(sysLanguage));
        //排序
        ArrayList<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        orderSpecifiers.add(QAnnouncement.announcement.sort.desc());
        //查
        PageResult<Announcement> pageResult = announcementService.queryDsl(pageNo, pageSize, predicates, QAnnouncement.announcement, orderSpecifiers);
        return success(pageResult);
    }

    @GetMapping("{id}")
    public MessageResult detail(@PathVariable("id") Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        return success(announcement);
    }


}
