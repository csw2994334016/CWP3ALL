package com.cwp3.multiple.data;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.all.MachineData;
import com.cwp3.model.log.Logger;
import com.cwp3.multiple.model.Crane;

import java.util.*;

/**
 * Created by csw on 2018/11/12.
 * Description:
 */
public class AllData {

    private AllRuntimeData allRuntimeData;

    private Map<String, Crane> craneMap; // 自己封装的桥机对象
    private Map<Long, VesselData> vesselDataMap; // 自己封装的单船数据对象

    // 分配桥机过程中产生的结果信息
    private long startTime;
    private Map<String, long[][]> timeCraneMap;

    public AllData(AllRuntimeData allRuntimeData) {
        this.allRuntimeData = allRuntimeData;
        craneMap = new LinkedHashMap<>();
        vesselDataMap = new HashMap<>();
        timeCraneMap = new LinkedHashMap<>();
    }

    public AllRuntimeData getAllRuntimeData() {
        return allRuntimeData;
    }

    public Logger getLogger() {
        return allRuntimeData.getLogger();
    }

    public MachineData getMachineData() {
        return allRuntimeData.getMachineData();
    }

    public void addCrane(Crane crane) {
        craneMap.put(crane.getCraneNo(), crane);
    }

    public Crane getCraneByCraneNo(String craneNo) {
        return craneMap.get(craneNo);
    }

    public List<Crane> getAllCranes() {
        return new ArrayList<>(craneMap.values());
    }

    public void addVesselData(VesselData vesselData) {
        vesselDataMap.put(vesselData.getBerthId(), vesselData);
    }

    public VesselData getVesselDataByBerthId(Long berthId) {
        return vesselDataMap.get(berthId);
    }

    public List<VesselData> getAllVesselDataList() {
        return new ArrayList<>(vesselDataMap.values());
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Map<String, long[][]> getTimeCraneMap() {
        return timeCraneMap;
    }

    public void setTimeCraneMap(Map<String, long[][]> timeCraneMap) {
        this.timeCraneMap = timeCraneMap;
    }
}
