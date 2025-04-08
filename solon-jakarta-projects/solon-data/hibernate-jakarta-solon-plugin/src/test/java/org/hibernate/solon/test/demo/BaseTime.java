package org.hibernate.solon.test.demo;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.util.Date;

@Data
@MappedSuperclass
public class BaseTime {
    @Column(name = "createTime")
    private Date createTime;
    @Column(name = "updateTime")
    private Date updateTime;

    @PrePersist// 新增时自动修改时间
    public void prePersist() {
        if (createTime == null)
            createTime = new Date();
        if (updateTime == null)
            updateTime = createTime;
    }
}