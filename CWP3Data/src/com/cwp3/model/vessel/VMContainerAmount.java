package com.cwp3.model.vessel;

/**
 * Created by csw on 2018/9/25.
 * Description:
 */
public class VMContainerAmount {

    private Long berthId;    //靠泊ID
    private String dlType;   //装卸类型
    private String size;    //箱子尺寸
    private Integer containerAmount;	//箱量

    public VMContainerAmount(Long berthId) {
        this.berthId = berthId;
    }

    public VMContainerAmount(Long berthId, String dlType, String size, Integer containerAmount) {
        this.berthId = berthId;
        this.dlType = dlType;
        this.size = size;
        this.containerAmount = containerAmount;
    }

    public Long getBerthId() {
        return berthId;
    }

    public String getDlType() {
        return dlType;
    }

    public void setDlType(String dlType) {
        this.dlType = dlType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getContainerAmount() {
        return containerAmount;
    }

    public void setContainerAmount(Integer containerAmount) {
        this.containerAmount = containerAmount;
    }
}
