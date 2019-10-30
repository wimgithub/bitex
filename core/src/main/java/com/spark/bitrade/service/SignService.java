package com.spark.bitrade.service;

import com.spark.bitrade.constant.SignStatus;
import com.spark.bitrade.dao.SignDao;
import com.spark.bitrade.entity.Sign;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author rongyu
 * @Description:
 * @date 2018/5/311:11
 */
@Service
public class SignService extends TopBaseService<Sign, SignDao> {


    @Autowired
    public void setDao(SignDao dao) {
        super.setDao(dao);
    }

    public Sign fetchUnderway() {
        return dao.findByStatus(SignStatus.UNDERWAY);
    }

    /**
     * 提前关闭
     *
     * @param sign 提前关闭
     */
    public void earlyClosing(Sign sign) {
        sign.setStatus(SignStatus.FINISH);
        dao.save(sign);
    }

}
