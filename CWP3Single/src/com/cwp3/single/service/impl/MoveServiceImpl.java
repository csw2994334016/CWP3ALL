package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.single.algorithm.move.MoveCalculator;
import com.cwp3.single.algorithm.move.MoveMaker;
import com.cwp3.single.data.MoveData;
import com.cwp3.single.data.MoveResults;
import com.cwp3.single.service.MoveService;

import java.util.List;

/**
 * Created by csw on 2018/5/29.
 * Description:
 */
public class MoveServiceImpl implements MoveService {

    private MoveMaker moveMaker;

    public MoveServiceImpl() {
        moveMaker = new MoveMaker();
    }

    @Override
    public void makeWorkFlow(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        StructureData structureData = allRuntimeData.getStructDataByVesselCode(workingData.getVmSchedule().getVesselCode());
        List<Long> hatchIdList = structureData.getAllHatchIdList();
        workingData.getLogger().logInfo("调用生成作业工艺的算法，对船舶(berthId:" + berthId + ")进行编写作业工艺。");
        for (Long hatchId : hatchIdList) {
            makeWorkFlowByHatchId(hatchId, CWPDomain.DL_TYPE_DISC, workingData, structureData);
            makeWorkFlowByHatchId(hatchId, CWPDomain.DL_TYPE_LOAD, workingData, structureData);
        }
        workingData.getLogger().logInfo("船舶(berthId:" + berthId + ")作业工艺编写结束。");
    }

    private void makeWorkFlowByHatchId(Long hatchId, String dlType, WorkingData workingData, StructureData structureData) {
        try {
            moveMaker.makeMove(hatchId, dlType, workingData, structureData);
        } catch (Exception e) {
            workingData.getLogger().logError("在对舱(" + hatchId + ")进行编写作业工艺过程中发生异常！");
            e.printStackTrace();
        }
    }

    @Override
    public void calculateMoves(AllRuntimeData allRuntimeData, Long berthId, MoveData moveData) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        StructureData structureData = allRuntimeData.getStructDataByVesselCode(workingData.getVmSchedule().getVesselCode());
        List<Long> hatchIdList = structureData.getAllHatchIdList();
        workingData.getLogger().logInfo("调用生成作业顺序的算法，对船舶(berthId:" + berthId + ")进行编写作业顺序。");
        //得到当前顶层
        MoveResults moveResults = new MoveResults();
        for (Long hatchId : hatchIdList) {
            calculateMoveByHatchId(hatchId, moveData, moveResults, workingData, structureData);
        }
        workingData.getLogger().logInfo("船舶(berthId:" + berthId + ")作业顺序编写结束。");
    }

    private void calculateMoveByHatchId(Long hatchId, MoveData moveData, MoveResults moveResults, WorkingData workingData, StructureData structureData) {
        workingData.getLogger().logDebug("对舱(hatchId:" + hatchId + ")进行编写作业顺序。");
        try {
            MoveCalculator moveCalculator = new MoveCalculator();
            moveCalculator.calculateAvailableMove(hatchId, moveData, moveResults, workingData, structureData);
        } catch (Exception e) {
            workingData.getLogger().logError("在对舱(" + hatchId + ")进行编写作业顺序过程中发生异常！");
            e.printStackTrace();
        }
    }
}
