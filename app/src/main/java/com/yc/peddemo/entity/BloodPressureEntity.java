package com.yc.peddemo.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * desc:
 * author: lei
 * date: 2022/3/17
 **/
public class BloodPressureEntity {
    private String lowBloodPressure;
    private String hightBloodPressure;
    private String time;

    public BloodPressureEntity(String lowBloodPressure, String hightBloodPressure, String time) {
        this.lowBloodPressure = lowBloodPressure;
        this.hightBloodPressure = hightBloodPressure;
        this.time = time;
    }

    public String getLowBloodPressure() {
        return lowBloodPressure;
    }

    public void setLowBloodPressure(String lowBloodPressure) {
        this.lowBloodPressure = lowBloodPressure;
    }

    public String getHightBloodPressure() {
        return hightBloodPressure;
    }

    public void setHightBloodPressure(String hightBloodPressure) {
        this.hightBloodPressure = hightBloodPressure;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
