package com.spark.bitrade.controller.system;

import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.PreCoin;
import com.spark.bitrade.entity.Vote;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.PreCoinService;
import com.spark.bitrade.service.VoteService;
import com.spark.bitrade.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("system/vote")
public class AdminVoteController extends BaseAdminController {

    @Autowired
    private VoteService voteService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private PreCoinService preCoinService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("system:vote:merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "新增投票")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult merge(@RequestBody Vote vote) {
        Assert.notNull(vote, "vote null");
        //校验币种
        for (PreCoin preCoin : vote.getPreCoins()) {
            Coin coin = coinService.findByUnit(preCoin.getUnit());
            if (coin != null) {
                return error(messageSource.getMessage("PRE_COIN_EXIST"));
            }
        }

        if (vote.getId() != null) {
            //修改 只允许修改最新的一条
            Vote find = voteService.findVote();
            Assert.isTrue(find.getId() == vote.getId(), messageSource.getMessage("MODIFY_LATEST"));
            for (PreCoin preCoin : vote.getPreCoins()) {
                if(preCoin.getId()!=null) {
                    PreCoin preCoin1 = preCoinService.findById(preCoin.getId());
                    preCoin.setVersion(preCoin1.getVersion());
                }
                //关联
                preCoin.setVote(vote);

            }
        } else {
            //新增特殊
            //关闭其他
            voteService.turnOffAllVote();

        }
        vote = voteService.save(vote);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), vote);
    }


    @RequiresPermissions("system:vote:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "投票详情")
    public MessageResult detail(Long id) {
        Vote vote = voteService.findById(id);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), vote);
    }

    @RequiresPermissions("system:vote:page-query")
    @PostMapping("page-query")
    public MessageResult pageQuery(PageModel pageModel) {
        Page<Vote> all = voteService.findAll(null, pageModel.getPageable());
        return success(all);
    }
}
