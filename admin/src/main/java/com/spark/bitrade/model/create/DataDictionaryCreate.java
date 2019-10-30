package com.spark.bitrade.model.create;

import com.spark.bitrade.ability.CreateAbility;
import com.spark.bitrade.entity.DataDictionary;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:24
 */
@Data
public class DataDictionaryCreate implements CreateAbility<DataDictionary> {
    @NotBlank
    private String bond;
    @NotBlank
    private String value;
    private String comment;

    @Override
    public DataDictionary transformation() {
        return new DataDictionary(bond, value, comment);
    }
}
