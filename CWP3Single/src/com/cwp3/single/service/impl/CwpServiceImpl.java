package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.single.algorithm.cwp.CwpProcess1;
import com.cwp3.single.service.*;

/**
 * Created by csw on 2018/6/7.
 * Description:
 */
public class CwpServiceImpl implements CwpService {

    private CwpAnalyzerService cwpAnalyzerService;
    private HatchBlockService hatchBlockService;
    private MoveService moveService;
    private LockWorkBlockService lockWorkBlockService;

    public CwpServiceImpl() {
        cwpAnalyzerService = new CwpAnalyzerServiceImpl();
        hatchBlockService = new HatchBlockServiceImpl();
        moveService = new MoveServiceImpl();
        lockWorkBlockService = new LockWorkBlockServiceImpl();
    }

    @Override
    public void doPlanCwp(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        workingData.setCwpType(CWPDomain.CWP_TYPE_PLAN);
        workingData.getLogger().logInfo("调用单船CWP算法，对船舶(berthId:" + berthId + ")进行CWP计划安排(" + workingData.getCwpType() + ")。");
        hatchBlockService.makeHatchBlock(allRuntimeData, berthId);
        moveService.makeWorkFlow(allRuntimeData, berthId);
        lockWorkBlockService.analyzeLockWorkBlock(allRuntimeData, berthId);
        try {
            CwpProcess1 cwpProcess = new CwpProcess1();
            cwpProcess.processCwp(allRuntimeData, berthId);
        } catch (Exception e) {
            workingData.getLogger().logError("对船舶(berthId:" + berthId + ")进行CWP计划安排时发生异常！");
            e.printStackTrace();
        }
        workingData.getLogger().logInfo("船舶(berthId:" + berthId + ")CWP计划安排结束。");

    }

    @Override
    public void doWorkCwp(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        workingData.setCwpType(CWPDomain.CWP_TYPE_WORK);
        workingData.getLogger().logInfo("调用单船CWP算法，对船舶(berthId:" + berthId + ")进行CWP计划重新安排(" + workingData.getCwpType() + ")。");
        cwpAnalyzerService.analyzeCwpVesselContainer(allRuntimeData, berthId);
        hatchBlockService.makeHatchBlock(allRuntimeData, berthId);
        moveService.makeWorkFlow(allRuntimeData, berthId);
        try {
            CwpProcess1 cwpProcess1 = new CwpProcess1();
            cwpProcess1.processCwp(allRuntimeData, berthId);
        } catch (Exception e) {
            workingData.getLogger().logError("对船舶(berthId:" + berthId + ")进行CWP计划重新安排时发生异常！");
            e.printStackTrace();
        }
        workingData.getLogger().logInfo("船舶(berthId:" + berthId + ")CWP计划重新安排结束。");
    }

    @Override
    public void doMultipleCwp(AllRuntimeData allRuntimeData, Long berthId) {

    }
}
