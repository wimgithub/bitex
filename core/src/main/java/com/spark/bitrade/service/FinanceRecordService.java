package com.spark.bitrade.service;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.dao.FinanceRecordDao;
import com.spark.bitrade.entity.FinanceProductDetail;
import com.spark.bitrade.entity.FinanceRecord;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.sparkframework.sql.DB;
import com.sparkframework.sql.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class FinanceRecordService extends BaseService {

    @Autowired
    private FinanceRecordDao financeRecordDao;

    public FinanceRecord save(FinanceRecord financeRecord) {
        return financeRecordDao.saveAndFlush(financeRecord);
    }


    public Page<FinanceRecord> getAllByProductDetail(FinanceProductDetail financeProductDetail, int pageNo, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Criteria<FinanceRecord> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("financeProductDetail", financeProductDetail,false));

        return financeRecordDao.findAll(criteria, pageRequest);
    }

    public Page<FinanceRecord> getAllByMember(Member member, int pageNo, int pageSize, Long detailId) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Criteria<FinanceRecord> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("financeProductDetail.member", member,false));

        if (detailId != null) {
            criteria.add(Restrictions.eq("financeProductDetail.id", detailId,false));
        }

        return financeRecordDao.findAll(criteria, pageRequest);
    }

    public JSONObject getStatisticsByDetails(Long memberId, String year, String detailIds) throws DataException, SQLException {
        String sql = "select DATE_FORMAT(a.create_time,'%Y-%m-%d') as create_time, SUM(a.amount) as sum_amount, " +
                "AVG(a.rate) as avg_rate, count(*) as count from finance_record a " +
                "left join finance_product_detail b on a.finance_product_detail_id=b.id " +
                "where finance_product_detail_id in ("+detailIds+") and a.create_time like ? and b.member_id=? " +
                "group by DATE_FORMAT(a.create_time,'%Y-%m-%d')";

        String sql1 = "select sum(amount) as total from finance_product_detail where id in ("+detailIds+") and member_id="+memberId;
        String sql2 = "select sum(amount) as total from finance_record where finance_product_detail_id in ("+detailIds+")";
        List<Map<String, String>> content = DB.query(sql, year+"%",memberId);
        List<Map<String, String>> detail_amount = DB.query(sql1);
        List<Map<String, String>> record_amount = DB.query(sql2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content",content);
        jsonObject.put("detail_amount",detail_amount.size()>0 ? detail_amount.get(0).get("total"):null);
        jsonObject.put("record_amount",record_amount.size()>0 ? record_amount.get(0).get("total"):null);
        return jsonObject;
    }
}
