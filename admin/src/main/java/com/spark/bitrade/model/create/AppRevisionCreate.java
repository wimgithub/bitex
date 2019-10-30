package com.spark.bitrade.model.create;

import com.spark.bitrade.ability.CreateAbility;
import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.entity.AppRevision;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:33
 */
@Data
public class AppRevisionCreate implements CreateAbility<AppRevision> {

    private String remark;

    @NotBlank
    private String version;

    private String downloadUrl;

    @NotNull
    private Platform platform;

    //转化
    @Override
    public AppRevision transformation() {
        AppRevision appRevision = new AppRevision();
        appRevision.setRemark(this.remark);
        appRevision.setVersion(this.version);
        appRevision.setDownloadUrl(this.downloadUrl);
        appRevision.setPlatform(this.platform);
        return appRevision;
    }
}
