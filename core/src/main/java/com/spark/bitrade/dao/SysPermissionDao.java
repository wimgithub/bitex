package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Zhang Jinwei
 * @date 2017年12月18日
 */
public interface SysPermissionDao extends BaseDao<SysPermission> {

    @Modifying
    @Query(value="delete from admin_role_permission where rule_id = ?1",nativeQuery = true)
    int deletePermission(long permissionId);
}
