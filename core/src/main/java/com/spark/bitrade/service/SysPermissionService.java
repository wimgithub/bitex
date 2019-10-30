package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.dao.SysPermissionDao;
import com.spark.bitrade.entity.SysPermission;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2017年12月20日
 */
@Service
public class SysPermissionService extends BaseService<SysPermission> {

    @Autowired
    public void setDao(SysPermissionDao dao) {
        super.setDao(dao);
    }

    @Autowired
    private SysPermissionDao sysPermissionDao;

    public SysPermission findOne(Long id) {
        return sysPermissionDao.findOne(id);
    }

    public List<SysPermission> findAll() {
        return sysPermissionDao.findAll();
    }

    public SysPermission save(SysPermission sysPermission) {
        return sysPermissionDao.save(sysPermission);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletes(Long[] ids) {
        for (long id : ids) {
            sysPermissionDao.deletePermission(id);
            sysPermissionDao.delete(id);
        }
    }

    public Page<SysPermission> findAll(Predicate predicate, Pageable pageable) {
        return sysPermissionDao.findAll(predicate, pageable);
    }
}
