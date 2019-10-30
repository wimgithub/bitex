package com.spark.bitrade.entity.transform;

import lombok.Data;

import java.util.List;

/**
 *
 * @author Zhang Jinwei
 * @date 2018年01月15日
 */
@Data
public class SpecialPage<E> {

    private List<E> context;
    private int currentPage;
    private int totalPage;
    private int pageNumber;
    private int totalElement;
}
