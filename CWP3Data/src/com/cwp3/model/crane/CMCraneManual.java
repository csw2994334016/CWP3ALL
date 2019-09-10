package com.cwp3.model.crane;

/**
 * Created by csw on 2018/10/10.
 * Description:
 */
public class CMCraneManual {

    private Long berthId;   //靠泊ID
    private Integer adviseCraneNumber;//人工建议开路数
    private String adviseCraneNos;//人工建议使用哪些桥机，用","隔开

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Integer getAdviseCraneNumber() {
        return adviseCraneNumber;
    }

    public void setAdviseCraneNumber(Integer adviseCraneNumber) {
        this.adviseCraneNumber = adviseCraneNumber;
    }

    public String getAdviseCraneNos() {
        return adviseCraneNos;
    }

    public void setAdviseCraneNos(String adviseCraneNos) {
        this.adviseCraneNos = adviseCraneNos;
    }
}
