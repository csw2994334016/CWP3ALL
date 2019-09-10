package com.cwp3.model.crane;

import java.util.ArrayList;
import java.util.List;

public class CMCraneType {

    private String craneTypeId;        //桥机类型编号
    private Integer maxWeightKg;      //kg

    private List<String> supportPTList; //支持的作业工艺id

    public CMCraneType(String craneTypeId, Integer maxWeightKg) {
        this.craneTypeId = craneTypeId;
        this.maxWeightKg = maxWeightKg;
        this.supportPTList = new ArrayList<>();
    }

    public String getCraneTypeId() {
        return craneTypeId;
    }

    public Integer getMaxWeightKg() {
        return maxWeightKg;
    }

    public List<String> getSupportPTList() {
        return supportPTList;
    }

    public void addSupprtPT(String ptId){
        this.supportPTList.add(ptId);
    }

}
