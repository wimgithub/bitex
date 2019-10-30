package com.spark.bitrade.service;

import com.spark.bitrade.dao.MemberLevelDao;
import com.spark.bitrade.entity.MemberLevel;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author rongyu
 * @description
 * @date 2017/12/26 17:26
 */
@Service
public class MemberLevelService extends BaseService {
    @Autowired
    private MemberLevelDao memberLevelDao;

    public List<MemberLevel> findAll(){
        return memberLevelDao.findAll();
    }

    /**
     * @author rongyu
     * @description id查询一个
     * @date 2017/12/27 10:54
     */
    public MemberLevel findOne(Long id){
        return  memberLevelDao.findOne(id);
    }

    /**
     * @author rongyu
     * @description 查询默认会员的等级
     * @date 2017/12/26 17:58
     */
    public MemberLevel findDefault() {
        return memberLevelDao.findOneByIsDefault(true);
    }

    /**
     * @author rongyu
     * @description 更新状态为false 不包括
     * @date 2017/12/27 11:02
     */
    public int updateDefault() {
       return memberLevelDao.updateDefault();
    }

    public MemberLevel save(MemberLevel memberLevel) {
        return memberLevelDao.save(memberLevel);
    }
}
