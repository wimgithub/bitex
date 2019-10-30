package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.AirdropDao;
import com.spark.bitrade.entity.Airdrop;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class AirdropService extends BaseService {
    @Autowired
    private AirdropDao airdropDao;

    public Airdrop save(Airdrop airdrop){
        return airdropDao.save(airdrop);
    }

    public Airdrop findById(Long id){
        return airdropDao.findById(id);
    }

    public Page<Airdrop> findAll(Predicate predicate, PageModel pageModel){
        return airdropDao.findAll(predicate,pageModel.getPageable());
    }
}
