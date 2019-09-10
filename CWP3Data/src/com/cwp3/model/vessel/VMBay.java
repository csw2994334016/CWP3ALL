package com.cwp3.model.vessel;

import java.util.*;

/**
 * Created by csw on 2017/4/20 11:38.
 * Explain: 倍位作业信息(一般分三个倍位置作业)
 */
public class VMBay {

    private String bayKey;
    private Long bayId; //解析数据时使用
    private Integer bayNo; //倍位号
    private String aboveOrBelow; //甲板、舱内
    private Long hatchId;
    private Map<Integer, VMRow> vmRowMap; //<rowNo, VMRow>

    public VMBay(Long bayId, String bayKey, Integer bayNo, String aboveOrBelow, Long hatchId) {
        this.bayId = bayId;
        this.bayKey = bayKey;
        this.bayNo = bayNo;
        this.aboveOrBelow = aboveOrBelow;
        this.hatchId = hatchId;
        this.vmRowMap = new HashMap<>();
    }

    public Long getBayId() {
        return bayId;
    }

    public Integer getBayNo() {
        return bayNo;
    }

    public String getBayKey() {
        return bayKey;
    }

    public String getAboveOrBelow() {
        return aboveOrBelow;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void addVMRow(VMRow vmRow) {
        vmRowMap.put(vmRow.getRowNo(), vmRow);
    }

    public List<Integer> getRowNoList() {
        return new ArrayList<>(vmRowMap.keySet());
    }

    public VMRow getVMRowByRowNo(Integer rowNo) {
        return vmRowMap.get(rowNo);
    }

    public int getMaxTier() {
        int maxTier = -1;
        for (VMRow moRow : vmRowMap.values()) {
            maxTier = maxTier > moRow.getTopTierNo() ? maxTier : moRow.getTopTierNo();
        }
        return maxTier;
    }

    public int getMinTier() {
        int minTier = 1000;
        for (VMRow moRow : vmRowMap.values()) {
            minTier = minTier < moRow.getBottomTierNo() ? minTier : moRow.getBottomTierNo();
        }
        return minTier;
    }
}
