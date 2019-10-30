package com.spark.bitrade.service;

import com.spark.bitrade.dao.FeedbackDao;
import com.spark.bitrade.entity.Feedback;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zhang Jinwei
 * @date 2018年03月19日
 */
@Service
public class FeedbackService extends BaseService {
    @Autowired
    private FeedbackDao feedbackDao;

    public Feedback save(Feedback feedback){
        return feedbackDao.save(feedback);
    }
}
