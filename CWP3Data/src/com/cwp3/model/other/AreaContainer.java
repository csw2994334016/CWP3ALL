package com.cwp3.model.other;


/**
 * Created by csw on 2017/8/14.
 * Description: 堆场信息，用于桥吊开路，控制箱区的冲突的情况
 */
public class AreaContainer {

    private Long berthId;         //靠泊ID
    private Long groupId;       //属性组
    private Long weightId;	//重量等级ID
    private String areaNo;       //箱区号
    private String areaType;       //箱区类型
    private Long totalNumber;      //总数
    private Long canStowNumber;   //可配数量
    private Long canNotStowNumber;//不可配数量

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getWeightId() {
        return weightId;
    }

    public void setWeightId(Long weightId) {
        this.weightId = weightId;
    }

    public String getAreaNo() {
        return areaNo;
    }

    public void setAreaNo(String areaNo) {
        this.areaNo = areaNo;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public Long getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(Long totalNumber) {
        this.totalNumber = totalNumber;
    }

    public Long getCanStowNumber() {
        return canStowNumber;
    }

    public void setCanStowNumber(Long canStowNumber) {
        this.canStowNumber = canStowNumber;
    }

    public Long getCanNotStowNumber() {
        return canNotStowNumber;
    }

    public void setCanNotStowNumber(Long canNotStowNumber) {
        this.canNotStowNumber = canNotStowNumber;
    }
}
