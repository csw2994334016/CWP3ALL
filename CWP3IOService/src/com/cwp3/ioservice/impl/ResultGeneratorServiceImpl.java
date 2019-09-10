package com.cwp3.ioservice.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.ioservice.ResultGeneratorService;
import com.cwp3.ioservice.method.CraneOrderMethod;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.CraneEfficiency;
import com.cwp3.model.work.CranePlan;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.single.data.MoveData;
import com.cwp3.utils.BeanCopyUtil;
import com.shbtos.biz.smart.cwp.pojo.Results.*;
import com.shbtos.biz.smart.cwp.service.SmartCraneAllocationResults;
import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;
import com.shbtos.biz.smart.cwp.service.SmartCwpResults;

import java.util.*;

/**
 * Created by csw on 2018/6/7.
 * Description:
 */
public class ResultGeneratorServiceImpl implements ResultGeneratorService {

    @Override
    public void generateMoveResult(MoveData moveData, WorkingData workingData) {
        moveDataToWorkingData(moveData.getDiscWorkMoveMap(), workingData);
        moveDataToWorkingData(moveData.getLoadWorkMoveMap(), workingData);
    }

    private void moveDataToWorkingData(Map<String, WorkMove> workMoveMap, WorkingData workingData) {
        for (Map.Entry<String, WorkMove> entry : workMoveMap.entrySet()) {
            for (VMSlot vmSlot : entry.getValue().getVmSlotSet()) {
                if (entry.getValue().getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                    workingData.getVMContainerByVMSlot(vmSlot, entry.getValue().getDlType()).setMoveOrder(entry.getValue().getMoveOrder());
                }
            }
        }
    }

    @Override
    public void generateCwpResult(WorkingData workingData, SmartCwp3Results smartCwp3Results) {
        Map<String, SmartReCwpModalInfo> smartReCwpModalInfoMap = new HashMap<>();
        for (String key : workingData.getWorkMoveMap().keySet()) {
            SmartReCwpModalInfo smartReCwpModalInfo = new SmartReCwpModalInfo();
            smartReCwpModalInfo.setModalName(key);
            smartReCwpModalInfo.setBerthId(workingData.getVmSchedule().getBerthId());
            smartReCwpModalInfoMap.put(key, smartReCwpModalInfo);
        }

        putCwpModalInfoMap(workingData, smartReCwpModalInfoMap);

        //重排需要对指令进行重新处理
        analyzeRecycleCntOrder(workingData, smartReCwpModalInfoMap);

        smartCwp3Results.getSmartReCwpModalInfoList().addAll(smartReCwpModalInfoMap.values());
    }

    private void putCwpModalInfoMap(WorkingData workingData, Map<String, SmartReCwpModalInfo> smartReCwpModalInfoMap) {
        for (Map.Entry<String, List<WorkMove>> entry : workingData.getWorkMoveMap().entrySet()) {
            List<SmartReCwpWorkOrderInfo> smartReCwpWorkOrderInfoList = new ArrayList<>();
            for (WorkMove workMove : entry.getValue()) {
                if (workMove.getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                    Set<VMContainer> vmContainerSet = new HashSet<>();
                    for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                        VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                        vmContainer.setMoveOrder(workMove.getMoveOrder());
                        vmContainer.setCntWorkTime(workMove.getWorkTime());
                        vmContainerSet.add(vmContainer);
                    }
                    for (VMContainer vmContainer : vmContainerSet) {
                        SmartReCwpWorkOrderInfo smartReCwpWorkOrderInfo = new SmartReCwpWorkOrderInfo();
                        smartReCwpWorkOrderInfo.setBerthId(vmContainer.getBerthId());
                        smartReCwpWorkOrderInfo.setCraneNo(workMove.getCraneNo());
                        smartReCwpWorkOrderInfo.setCranePosition(workMove.getWorkPosition());
                        smartReCwpWorkOrderInfo.setBayNo(String.format("%02d", workMove.getBayNo()));
                        smartReCwpWorkOrderInfo.setHatchId(vmContainer.getHatchId());
                        smartReCwpWorkOrderInfo.setVesselLocation(vmContainer.getvLocation());
                        smartReCwpWorkOrderInfo.setCszcsizecd(vmContainer.getSize());
                        if ("43".equals(vmContainer.getSize())) {
                            smartReCwpWorkOrderInfo.setCszcsizecd("53");
                        }
                        smartReCwpWorkOrderInfo.setWorkflow(PublicMethod.getWorkFlowStr(workMove.getWorkFlow()));
                        smartReCwpWorkOrderInfo.setCwpwkmovenum(workMove.getMoveOrder());
                        smartReCwpWorkOrderInfo.setLduldfg(vmContainer.getDlType());
                        smartReCwpWorkOrderInfo.setWorkingStartTime(workMove.getPlanStartTime());
                        smartReCwpWorkOrderInfo.setWorkingEndTime(workMove.getPlanEndTime());
                        smartReCwpWorkOrderInfo.setMoveWorkTime(workMove.getWorkTime().intValue());
                        smartReCwpWorkOrderInfo.setQdc(vmContainer.getQdc());
                        smartReCwpWorkOrderInfo.setPlanAmount(1L);
                        smartReCwpWorkOrderInfo.setVpcCntrId(vmContainer.getVpcCntId());
                        smartReCwpWorkOrderInfo.setRecycleWiFlag(vmContainer.getRecycleWiFlag());
                        smartReCwpWorkOrderInfo.setCwpBlockId(workMove.getBlockId());
                        smartReCwpWorkOrderInfoList.add(smartReCwpWorkOrderInfo);
                    }
                }
            }
            smartReCwpModalInfoMap.get(entry.getKey()).getSmartReCwpWorkOrderInfoList().addAll(smartReCwpWorkOrderInfoList);
        }
        for (Map.Entry<String, List<WorkBlock>> entry : workingData.getWorkBlockMap().entrySet()) {
            List<SmartReCwpBlockInfo> smartReCwpBlockInfoList = new ArrayList<>();
            for (WorkBlock workBlock : entry.getValue()) {
                SmartReCwpBlockInfo smartReCwpBlockInfo = new SmartReCwpBlockInfo();
                smartReCwpBlockInfo.setBerthId(workingData.getVmSchedule().getBerthId());
                smartReCwpBlockInfo.setCraneNo(workBlock.getCraneNo());
                smartReCwpBlockInfo.setBayNo(workBlock.getBayNo());
                smartReCwpBlockInfo.setHatchId(workBlock.getHatchId());
                smartReCwpBlockInfo.setPlanAmount(workBlock.getPlanAmount());
                smartReCwpBlockInfo.setLduldfg(workBlock.getLduldfg());
                smartReCwpBlockInfo.setWorkingStartTime(workBlock.getWorkingStartTime());
                smartReCwpBlockInfo.setWorkingEndTime(workBlock.getWorkingEndTime());
                smartReCwpBlockInfo.setSelectReason(workBlock.getSelectReason());
                smartReCwpBlockInfo.setBlockType(workBlock.getBlockType());
                smartReCwpBlockInfo.setBlockId(workBlock.getBlockId());
                smartReCwpBlockInfoList.add(smartReCwpBlockInfo);
            }

            CraneOrderMethod craneOrderMethod = new CraneOrderMethod();
            smartReCwpBlockInfoList = craneOrderMethod.getHatchSeq(smartReCwpBlockInfoList);
            smartReCwpBlockInfoList = craneOrderMethod.getCraneSeq(smartReCwpBlockInfoList);
            smartReCwpModalInfoMap.get(entry.getKey()).getSmartReCwpBlockInfoList().addAll(smartReCwpBlockInfoList);
        }
        for (Map.Entry<String, List<CraneEfficiency>> entry : workingData.getCraneEfficiencyMap().entrySet()) {
            List<SmartReCwpCraneEfficiencyInfo> smartReCwpCraneEfficiencyInfoList = new ArrayList<>();
            for (CraneEfficiency craneEfficiency : entry.getValue()) {
                SmartReCwpCraneEfficiencyInfo smartReCwpCraneEfficiencyInfo = new SmartReCwpCraneEfficiencyInfo();
                smartReCwpCraneEfficiencyInfo = (SmartReCwpCraneEfficiencyInfo) BeanCopyUtil.copyBean(craneEfficiency, smartReCwpCraneEfficiencyInfo);
                smartReCwpCraneEfficiencyInfoList.add(smartReCwpCraneEfficiencyInfo);
            }
            smartReCwpModalInfoMap.get(entry.getKey()).getSmartReCwpCraneEfficiencyInfoList().addAll(smartReCwpCraneEfficiencyInfoList);
        }
    }

    private void analyzeRecycleCntOrder(WorkingData workingData, Map<String, SmartReCwpModalInfo> smartReCwpModalInfoMap) {
        if (CWPDomain.CWP_TYPE_WORK.equals(workingData.getCwpType())) {
            try {
                for (SmartReCwpModalInfo smartReCwpModalInfo : smartReCwpModalInfoMap.values()) {
                    //作业块按桥机存储
                    List<SmartReCwpBlockInfo> smartReCwpBlockInfoList = smartReCwpModalInfo.getSmartReCwpBlockInfoList();
                    Map<String, List<SmartReCwpBlockInfo>> craneBlockMap = new HashMap<>();
                    if (smartReCwpBlockInfoList.size() > 0) {
                        for (SmartReCwpBlockInfo smartReCwpBlockInfo : smartReCwpBlockInfoList) {
                            String craneNo = smartReCwpBlockInfo.getCraneNo();
                            if (craneBlockMap.get(craneNo) == null) {
                                craneBlockMap.put(craneNo, new ArrayList<SmartReCwpBlockInfo>());
                            }
                            craneBlockMap.get(craneNo).add(smartReCwpBlockInfo);
                        }
                        for (Map.Entry<String, List<SmartReCwpBlockInfo>> entry : craneBlockMap.entrySet()) {
                            Collections.sort(entry.getValue(), new Comparator<SmartReCwpBlockInfo>() {
                                @Override
                                public int compare(SmartReCwpBlockInfo o1, SmartReCwpBlockInfo o2) {
                                    return o1.getWorkingStartTime().compareTo(o2.getWorkingStartTime());
                                }
                            });
                        }
                    }

                    List<SmartReCwpWorkOrderInfo> smartReCwpWorkOrderInfoList = smartReCwpModalInfo.getSmartReCwpWorkOrderInfoList();

                    //回收的指令是否需要按原计划执行下去，即不应该回收桥机没有动倍的指令，由重排后桥机的作业位置决定
                    if (smartReCwpBlockInfoList.size() > 0) {
                        for (SmartReCwpBlockInfo smartReCwpBlockInfo : smartReCwpBlockInfoList) {
                            smartReCwpBlockInfo.setAmountTemp(smartReCwpBlockInfo.getPlanAmount().doubleValue());
                        }
                        long endTime = smartReCwpBlockInfoList.get(0).getWorkingStartTime().getTime() + 4 * 3600000; //ms
                        if (CWPDomain.YES.equals(workingData.getCwpConfig().getRecycleCntWiFlag())) {
                            endTime = smartReCwpBlockInfoList.get(0).getWorkingStartTime().getTime() + 900000;
                        }
                        for (SmartReCwpWorkOrderInfo smartReCwpWorkOrderInfo : smartReCwpWorkOrderInfoList) {
                            String craneNo = smartReCwpWorkOrderInfo.getCraneNo();
                            if ("Y".equals(smartReCwpWorkOrderInfo.getRecycleWiFlag())) {
                                if (smartReCwpWorkOrderInfo.getWorkingStartTime().getTime() <= endTime) { //小于4个小时的指令按原计划执行下去
                                    String bayNo = smartReCwpWorkOrderInfo.getBayNo(); //应该是作业倍位
                                    List<SmartReCwpBlockInfo> smartReCwpBlockInfoList1 = craneBlockMap.get(craneNo);
                                    for (SmartReCwpBlockInfo smartReCwpBlockInfo : smartReCwpBlockInfoList1) {
                                        //查找指令所在的作业块
                                        if (smartReCwpBlockInfo.getBayNo().equals(bayNo) && smartReCwpBlockInfo.getBlockId().equals(smartReCwpWorkOrderInfo.getCwpBlockId())) {
                                            smartReCwpWorkOrderInfo.setRecycleWiFlag("N");
                                            //todo:作业块的量相应减掉
                                            if ("2".equals(smartReCwpWorkOrderInfo.getWorkflow())) {
                                                smartReCwpBlockInfo.setAmountTemp(smartReCwpBlockInfo.getAmountTemp() - 0.5);
                                                long st = smartReCwpBlockInfo.getWorkingStartTime().getTime();
                                                st = st + smartReCwpWorkOrderInfo.getMoveWorkTime() * 500;
                                                smartReCwpBlockInfo.setWorkingStartTime(new Date(st));
                                            } else {
                                                smartReCwpBlockInfo.setAmountTemp(smartReCwpBlockInfo.getAmountTemp() - 1);
                                                long st = smartReCwpBlockInfo.getWorkingStartTime().getTime();
                                                st = st + smartReCwpWorkOrderInfo.getMoveWorkTime() * 1000;
                                                smartReCwpBlockInfo.setWorkingStartTime(new Date(st));
                                            }
                                            smartReCwpBlockInfo.setPlanAmount((long) smartReCwpBlockInfo.getAmountTemp());
                                            smartReCwpBlockInfo.setBlockId(smartReCwpWorkOrderInfo.getCwpBlockId());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    List<SmartReCwpBlockInfo> smartReCwpBlockInfoList1 = new ArrayList<>();
                    for (SmartReCwpBlockInfo smartReCwpBlockInfo : smartReCwpBlockInfoList) {
                        if (smartReCwpBlockInfo.getPlanAmount() > 0) {
                            smartReCwpBlockInfoList1.add(smartReCwpBlockInfo);
                        }
                    }
                    smartReCwpModalInfo.getSmartReCwpBlockInfoList().clear();
                    smartReCwpModalInfo.getSmartReCwpBlockInfoList().addAll(smartReCwpBlockInfoList1);

                    //添加按原计划执行的指令，即没有回收的指令
                    for (VMContainer vmContainer : workingData.getContinueCntList()) {
                        SmartReCwpWorkOrderInfo smartReCwpWorkOrderInfo = new SmartReCwpWorkOrderInfo();
                        smartReCwpWorkOrderInfo.setBerthId(vmContainer.getBerthId());
                        smartReCwpWorkOrderInfo.setCraneNo(vmContainer.getCraneNo());
                        smartReCwpWorkOrderInfo.setBayNo(vmContainer.getvLocation().substring(0, 2)); //作业倍位号
                        smartReCwpWorkOrderInfo.setHatchId(vmContainer.getHatchId());
                        smartReCwpWorkOrderInfo.setVesselLocation(vmContainer.getvLocation());
                        smartReCwpWorkOrderInfo.setCszcsizecd(vmContainer.getSize());
                        if ("43".equals(vmContainer.getSize())) {
                            smartReCwpWorkOrderInfo.setCszcsizecd("53");
                        }
                        smartReCwpWorkOrderInfo.setWorkflow(vmContainer.getPlanWorkFlow());
                        smartReCwpWorkOrderInfo.setCwpwkmovenum(vmContainer.getPlanMoveOrder());
                        smartReCwpWorkOrderInfo.setLduldfg(vmContainer.getDlType());
                        smartReCwpWorkOrderInfo.setWorkingStartTime(vmContainer.getWorkingStartTime());
                        smartReCwpWorkOrderInfo.setWorkingEndTime(vmContainer.getWorkingEndTime());
                        smartReCwpWorkOrderInfo.setMoveWorkTime(vmContainer.getCntWorkTime().intValue());
                        smartReCwpWorkOrderInfo.setQdc(0);
                        smartReCwpWorkOrderInfo.setPlanAmount(1L);
                        smartReCwpWorkOrderInfo.setVpcCntrId(vmContainer.getVpcCntId());
                        smartReCwpWorkOrderInfo.setRecycleWiFlag(vmContainer.getRecycleWiFlag());
                        smartReCwpWorkOrderInfo.setCwpBlockId(vmContainer.getCwpBlockId());
                        smartReCwpWorkOrderInfoList.add(smartReCwpWorkOrderInfo);
                        //赋值作业块Id（cwpBlockId）
                        List<SmartReCwpBlockInfo> smartReCwpBlockInfoList2 = craneBlockMap.get(vmContainer.getCraneNo());
                        if (smartReCwpBlockInfoList2 != null && smartReCwpBlockInfoList2.size() > 0) {
                            SmartReCwpBlockInfo smartReCwpBlockInfo = smartReCwpBlockInfoList2.get(0); //只会在一个小时以内的作业中出现
                            String bayNo = smartReCwpWorkOrderInfo.getBayNo();
                            if (smartReCwpBlockInfo.getBayNo().equals(bayNo) && smartReCwpBlockInfo.getBlockId() < 500 && smartReCwpWorkOrderInfo.getCwpBlockId() != null) { //判断为继续原来的倍位作业
                                smartReCwpBlockInfo.setBlockId(smartReCwpWorkOrderInfo.getCwpBlockId());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void generateOldCwpResult(SmartCwp3Results smartCwp3Results, SmartCwpResults smartCwpResults) {
        smartCwpResults.setSmartReMessageInfo(smartCwp3Results.getSmartReMessageInfo());
        if (smartCwp3Results.getSmartReCwpModalInfoList().size() > 0) {
            SmartReCwpModalInfo smartReCwpModalInfo = smartCwp3Results.getSmartReCwpModalInfoList().get(0);
            smartCwpResults.getSmartReCwpWorkOrderInfoList().addAll(smartReCwpModalInfo.getSmartReCwpWorkOrderInfoList());
            smartCwpResults.getSmartReCwpBlockInfoList().addAll(smartReCwpModalInfo.getSmartReCwpBlockInfoList());
        }
    }

    @Override
    public void generateCraneAllocationResult(AllRuntimeData allRuntimeData, SmartCraneAllocationResults smartCraneAllocationResults) {
        for (CranePlan cranePlan : allRuntimeData.getCranePlanList()) {
            SmartReCraneWorkPlanInfo smartReCraneWorkPlanInfo = new SmartReCraneWorkPlanInfo();
            smartReCraneWorkPlanInfo.setBerthId(cranePlan.getBerthId());
            smartReCraneWorkPlanInfo.setCraneNo(cranePlan.getCraneNo());
            smartReCraneWorkPlanInfo.setWorkingStartTime(cranePlan.getWorkingStartTime());
            smartReCraneWorkPlanInfo.setWorkingEndTime(cranePlan.getWorkingEndTime());
            smartCraneAllocationResults.getSmartReCraneWorkPlanInfoList().add(smartReCraneWorkPlanInfo);
        }
    }

    @Override
    public void generateMultipleCwpResult(AllRuntimeData allRuntimeData, SmartCwp3Results smartCwp3Results) {
        Map<String, SmartReCwpModalInfo> smartReCwpModalInfoMap = new HashMap<>();
        for (WorkingData workingData : allRuntimeData.getAllWorkingDataList()) {
            for (String key : workingData.getWorkMoveMap().keySet()) {
                SmartReCwpModalInfo smartReCwpModalInfo = new SmartReCwpModalInfo();
                smartReCwpModalInfo.setModalName(key);
                smartReCwpModalInfoMap.put(key, smartReCwpModalInfo);
            }
        }
        for (WorkingData workingData : allRuntimeData.getAllWorkingDataList()) {
            putCwpModalInfoMap(workingData, smartReCwpModalInfoMap);
            analyzeRecycleCntOrder(workingData, smartReCwpModalInfoMap);
        }
        smartCwp3Results.getSmartReCwpModalInfoList().addAll(smartReCwpModalInfoMap.values());
    }
}
