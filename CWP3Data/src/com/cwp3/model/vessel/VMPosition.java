package com.cwp3.model.vessel;

import java.io.Serializable;

/**
 * Created by csw on 2017/5/17 9:25.
 * Explain: 倍排层信息
 */
public class VMPosition implements Serializable {

    private Integer bayNo;
    private Integer rowNo;
    private Integer tierNo;


    public VMPosition(Integer bayNo, Integer rowNo, Integer tierNo) {
        this.bayNo = bayNo;
        this.rowNo = rowNo;
        this.tierNo = tierNo;
    }

    public VMPosition(String vLocation) {
        bayNo = Integer.valueOf(vLocation.substring(0, 2));
        rowNo = Integer.valueOf(vLocation.substring(2, 4));
        tierNo = Integer.valueOf(vLocation.substring(4, vLocation.length()));
    }

    public Integer getBayNo() {
        return bayNo;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public Integer getTierNo() {
        return tierNo;
    }

    public String getVLocation() {
        String bayNoStr = String.format("%02d", bayNo);
        String rowNoStr = String.format("%02d", rowNo);
        String tierNoStr = String.format("%02d", tierNo);
        return bayNoStr + rowNoStr + tierNoStr;
    }
}
