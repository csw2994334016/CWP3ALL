package com.cwp3.multiple.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.multiple.algorithm.process.CraneAllocationProcess;
import com.cwp3.multiple.algorithm.process.MultipleCwpProcess;
import com.cwp3.multiple.service.MultipleService;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class MultipleServiceImpl implements MultipleService {

    @Override
    public void doCraneAllocation(AllRuntimeData allRuntimeData) {
        allRuntimeData.getLogger().logInfo("调用桥机资源策划算法，对船舶进行桥机资源安排。");
        try {
            CraneAllocationProcess craneAllocationProcess = new CraneAllocationProcess(allRuntimeData);
            craneAllocationProcess.processCraneAllocation();
        } catch (Exception e) {
            allRuntimeData.getLogger().logError("对船舶进行桥机资源安排时发生异常！");
            e.printStackTrace();
        }
        allRuntimeData.getLogger().logInfo("对船舶进行桥机资源安排安排结束。");
    }

    @Override
    public void doMultipleCwp(AllRuntimeData allRuntimeData) {
        allRuntimeData.getLogger().logInfo("调用多船CWP算法，对多条船舶进行CWP安排。");
        try {
            MultipleCwpProcess multipleCwpProcess = new MultipleCwpProcess(allRuntimeData);
            multipleCwpProcess.processMultipleCwp();
        } catch (Exception e) {
            allRuntimeData.getLogger().logError("对多条船舶进行CWP安排时发生异常！");
            e.printStackTrace();
        }
        allRuntimeData.getLogger().logInfo("对多条船舶进行CWP安排结束。");
    }
}
