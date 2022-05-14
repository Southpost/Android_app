package com.yc.peddemo.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * desc:
 * author: lei
 * date: 2022/3/17
 **/
@Entity
public class BloodOxygenEntity {
    @Id(autoincrement = true)
    private long id;
    private String level;
    private String time;

    public BloodOxygenEntity(String level, String time) {
        this.level = level;
        this.time = time;
    }

    @Generated(hash = 1572891618)
    public BloodOxygenEntity(long id, String level, String time) {
        this.id = id;
        this.level = level;
        this.time = time;
    }

    @Generated(hash = 1015372326)
    public BloodOxygenEntity() {
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
