package com.cwp3.model.crane;

/**
 * Created by csw on 2017/8/14.
 * Description: 船舶桥机池信息，人工分配该船的桥机信息，用一个cranePool来表示
 */
public class CMVesselCranePool {

    private Long berthId;         //靠泊ID
    private Long poolId;         //桥吊池ID

    public CMVesselCranePool(Long berthId, Long poolId) {
        this.berthId = berthId;
        this.poolId = poolId;
    }

    public Long getBerthId() {
        return berthId;
    }

    public Long getPoolId() {
        return poolId;
    }

}
