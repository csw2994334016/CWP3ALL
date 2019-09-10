package com.cwp3.model.crane;

import java.util.Date;

/**
 * Created by csw on 2018/6/26.
 * Description:
 */
public class CMCraneAddOrDelete {

    private Long berthId; //靠泊Id
    private String addOrDelFlag; //加/减桥机:"A"表示增加桥机，"D"表示减桥机
    private Date addOrDelDate; //加/减桥机的时间
    private Integer leftCraneNum; //左边桥机数
    private Integer rightCraneNum; //右边桥机数
    private Integer targetCraneNum; //目标桥机数

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public String getAddOrDelFlag() {
        return addOrDelFlag;
    }

    public void setAddOrDelFlag(String addOrDelFlag) {
        this.addOrDelFlag = addOrDelFlag;
    }

    public Date getAddOrDelDate() {
        return addOrDelDate;
    }

    public void setAddOrDelDate(Date addOrDelDate) {
        this.addOrDelDate = addOrDelDate;
    }

    public Integer getLeftCraneNum() {
        return leftCraneNum;
    }

    public void setLeftCraneNum(Integer leftCraneNum) {
        this.leftCraneNum = leftCraneNum;
    }

    public Integer getRightCraneNum() {
        return rightCraneNum;
    }

    public void setRightCraneNum(Integer rightCraneNum) {
        this.rightCraneNum = rightCraneNum;
    }

    public Integer getTargetCraneNum() {
        return targetCraneNum;
    }

    public void setTargetCraneNum(Integer targetCraneNum) {
        this.targetCraneNum = targetCraneNum;
    }
}
