package com.cwp3.model.crane;

import com.cwp3.utils.StringUtil;

/**
 * Created by csw on 2017/8/29.
 * Description: 桥机作业工艺信息设置，针对每一个舱
 */
public class CMCraneWorkFlow {

    private Long hatchId; //舱ID
    private String deckOrHatch; //D：甲板、H：舱内
    private String dlType; //分装、卸
    private Boolean single; //单吊具
    private Boolean twin; //双箱吊
    private Boolean tandem; //双吊具
    private String ldStrategy; //装卸策略，即边装边卸：BLD、一般装卸：LD，默认驳船不做边装边卸，大船能做边装边卸就做

    public CMCraneWorkFlow(Long hatchId, String aboveOrBelow, String dlType) {
        this.hatchId = hatchId;
        this.deckOrHatch = aboveOrBelow;
        this.dlType = dlType;
    }

    public String getKey() {
        return StringUtil.getKey(StringUtil.getKey(hatchId, deckOrHatch), dlType);
    }

    public String getDlType() {
        return dlType;
    }

    public void setDlType(String dlType) {
        this.dlType = dlType;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public String getDeckOrHatch() {
        return deckOrHatch;
    }

    public Boolean getSingle() {
        return single;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    public Boolean getTwin() {
        return twin;
    }

    public void setTwin(Boolean twin) {
        this.twin = twin;
    }

    public Boolean getTandem() {
        return tandem;
    }

    public void setTandem(Boolean tandem) {
        this.tandem = tandem;
    }

    public String getLdStrategy() {
        return ldStrategy;
    }

    public void setLdStrategy(String ldStrategy) {
        this.ldStrategy = ldStrategy;
    }
}
