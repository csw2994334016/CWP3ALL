package com.cwp3.data;

import com.cwp3.data.all.MachineData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.log.Logger;
import com.cwp3.model.work.CranePlan;

import java.util.*;

public class AllRuntimeData {

    private Logger logger;
    private MachineData machineData;              //所有的机械设备数据

    //单船数据
    private Map<String, StructureData> structureDataMap; //船舶结构,key: vesselCode
    private Map<Long, WorkingData> workingDataMap; //作业数据,key: berthId

    // 桥机资源策划表结果
    private List<CranePlan> cranePlanList;

    // 数据存储传递对象
    private Map<String, Object> storageMap;

    public AllRuntimeData() {
        this.machineData = new MachineData();
        this.structureDataMap = new HashMap<>();
        this.workingDataMap = new HashMap<>();
        this.storageMap = new HashMap<>();
        cranePlanList = new ArrayList<>();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MachineData getMachineData() {
        return machineData;
    }

    public void addStructData(StructureData structureData) {
        structureDataMap.put(structureData.getVesselCode(), structureData);
    }

    public StructureData getStructDataByVesselCode(String vesselCode) {
        return structureDataMap.get(vesselCode);
    }

    public void addWorkingData(WorkingData workingData) {
        workingDataMap.put(workingData.getVmSchedule().getBerthId(), workingData);
    }

    public WorkingData getWorkingDataByBerthId(Long berthId) {
        return workingDataMap.get(berthId);
    }

    public void putStorage(String key, Object object) {
        storageMap.put(key, object);
    }

    public Object getStorageByKey(String key) {
        return storageMap.get(key);
    }

    public List<StructureData> getAllStructureDataList(){
        return new ArrayList<>(structureDataMap.values());
    }

    public List<WorkingData> getAllWorkingDataList() {
        return new ArrayList<>(workingDataMap.values());
    }

    public Collection<Long> getAllBerthId(){
        return workingDataMap.keySet();
    }

    public List<CranePlan> getCranePlanList() {
        return cranePlanList;
    }

    public void setCranePlanList(List<CranePlan> cranePlanList) {
        this.cranePlanList = cranePlanList;
    }
}
