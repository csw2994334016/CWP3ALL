package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCntDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.crane.CMCranePool;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMPosition;
import com.cwp3.single.service.CwpAnalyzerService;

import java.util.*;

/**
 * Created by csw on 2018/10/16.
 * Description:
 */
public class CwpAnalyzerServiceImpl implements CwpAnalyzerService {

    @Override
    public void analyzeCwpVesselContainer(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        workingData.getLogger().logInfo("调用CWP重排算法，对船舶(berthId:" + berthId + ")进行作业指令分析。");
        try {

            analyzeCntWorkState(workingData);

            analyzeCraneWorkBayNo(workingData, allRuntimeData.getStructDataByVesselCode(workingData.getVmSchedule().getVesselCode()));

        } catch (Exception e) {
            workingData.getLogger().logError("对船舶(berthId:" + berthId + ")进行作业指令分析时发生异常！");
            e.printStackTrace();
        }
        workingData.getLogger().logInfo("船舶(berthId:" + berthId + ")作业指令结束。");
    }

    private void analyzeCntWorkState(WorkingData workingData) {
        List<VMContainer> sentContainerList = workingData.getSentContainerList();
        for (VMContainer vmContainer : sentContainerList) {
            String moveStage = vmContainer.getMoveStage();
            String ldFlag = vmContainer.getDlType();
            String dispatchedTask = "Y".equals(vmContainer.getDispatchedTask()) ? "Y" : "N";
            String canRecycleFlag = "N".equals(vmContainer.getCanRecycleFlag()) ? "N" : "Y";
            boolean recycleFlag = false;
            //卸船，没有派发任务、可以回收、在船上，则回收
            if (CWPDomain.DL_TYPE_DISC.equals(ldFlag) && "N".equals(dispatchedTask) && "Y".equals(canRecycleFlag) && CWPCntDomain.VESSEL.equals(moveStage)) {
                recycleFlag = true;
            }
            //装船，没有派发任务、可以回收、在箱区上，则回收
            boolean cntState = CWPCntDomain.YARD.equals(moveStage) || CWPCntDomain.YARDTEMP.equals(moveStage);
            if ("L".equals(ldFlag) && "N".equals(dispatchedTask) && "Y".equals(canRecycleFlag) && cntState) {
                recycleFlag = true;
            }
            //判断成双的箱子，回收回收状态要一样
            if ("2".equals(vmContainer.getPlanWorkFlow()) || "3".equals(vmContainer.getPlanWorkFlow())) {
                VMContainer pairCnt = null;
                for (VMContainer cnt : sentContainerList) {
                    if (!cnt.getvLocation().equals(vmContainer.getvLocation()) && cnt.getHatchId().equals(vmContainer.getHatchId())) {
                        if (cnt.getPlanWorkFlow().equals(vmContainer.getPlanWorkFlow()) && cnt.getPlanMoveOrder().equals(vmContainer.getPlanMoveOrder())) {
                            pairCnt = cnt;
                            break;
                        }
                    }
                }
                if (pairCnt != null) {
                    String dispatchedTask1 = "Y".equals(pairCnt.getDispatchedTask()) ? "Y" : "N";
                    String canRecycleFlag1 = "N".equals(pairCnt.getCanRecycleFlag()) ? "N" : "Y";
                    if ("Y".equals(dispatchedTask1) && "N".equals(canRecycleFlag1)) {
                        recycleFlag = false;
                    }
                }
            }

            if (recycleFlag) { //回收的箱子，重新安排CWP计划
                vmContainer.setRecycleWiFlag("Y");
                workingData.getRecycleCntList().add(vmContainer);
                workingData.putVMContainer(new VMPosition(vmContainer.getvLocation()), vmContainer);
            } else {
                vmContainer.setRecycleWiFlag("N");
                workingData.getContinueCntList().add(vmContainer);
            }
        }
    }

    private void analyzeCraneWorkBayNo(WorkingData workingData, StructureData structureData) {
        Map<String, List<VMContainer>> craneCntListMap = new HashMap<>();
        for (VMContainer vmContainer : workingData.getRecycleCntList()) {
            if (craneCntListMap.get(vmContainer.getCraneNo()) == null) {
                craneCntListMap.put(vmContainer.getCraneNo(), new ArrayList<VMContainer>());
            }
            craneCntListMap.get(vmContainer.getCraneNo()).add(vmContainer);
        }
        for (Map.Entry<String, List<VMContainer>> entry : craneCntListMap.entrySet()) {
            Collections.sort(entry.getValue(), new Comparator<VMContainer>() {
                @Override
                public int compare(VMContainer o1, VMContainer o2) {
                    return o1.getPlanMoveOrder().compareTo(o2.getPlanMoveOrder());
                }
            });
        }

        List<CMCranePool> cmCranePoolList = workingData.getAllCMCranePools();
        for (CMCranePool cmCranePool : cmCranePoolList) {
            if (cmCranePool.getFirstWorkBayNo() == null) {
                List<VMContainer> vmContainerList = craneCntListMap.get(cmCranePool.getCraneNo());
                if (vmContainerList != null && vmContainerList.size() > 0) {
                    VMContainer vmContainer = vmContainerList.get(0);
                    Integer bayNo;
                    if ("2".equals(vmContainer.getPlanWorkFlow())) {
                        bayNo = structureData.getVMHatchByHatchId(vmContainer.getHatchId()).getBayNoD();
                    } else {
                        bayNo = new VMPosition(vmContainer.getvLocation()).getBayNo();
                    }
                    cmCranePool.setFirstWorkBayNo(bayNo);
                }
            }
        }
    }
}
