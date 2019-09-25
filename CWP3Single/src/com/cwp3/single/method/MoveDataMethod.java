package com.cwp3.single.method;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.*;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.single.data.MoveData;
import com.cwp3.utils.StringUtil;

import java.util.*;

/**
 * Created by csw on 2018/5/31.
 * Description:
 */
public class MoveDataMethod {

    public MoveData initMoveData(WorkingData workingData) {
        MoveData moveData = new MoveData();
        moveData.setDiscWorkMoveMap(this.copyWorkMoveMap(workingData.getDiscWorkMoveMap()));
        moveData.setLoadWorkMoveMap(this.copyWorkMoveMap(workingData.getLoadWorkMoveMap()));
        return moveData;
    }

    public void initCurTopWorkMove(MoveData moveData, WorkingData workingData, StructureData structureData) {
        List<Long> hatchIdList = structureData.getAllHatchIdList();
        for (Long hatchId : hatchIdList) {
            initCurTopWorkMoveByHatchId(hatchId, moveData, structureData, workingData);
        }
    }

    void initCurTopWorkMoveByHatchId(Long hatchId, MoveData moveData, StructureData structureData, WorkingData workingData) {
        String dlType;
        VMSlot vmSlot;
        WorkMove workMove;
        moveData.clearCurTopMoveByHatchId(hatchId); //清空该舱的顶层
        List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, CWPDomain.ROW_SEQ_ODD_EVEN);
        for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
            VMBay vmBayA = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_ABOVE));
            VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_BELOW));
            // 处理甲板上没有船舶结构的情况
            VMBay vmBay = vmBayA;
            if (vmBayA.getRowNoList().size() == 0) {
                vmBay = vmBayB;
            }
            for (Integer rowNo : rowNoSeqList) {
                dlType = CWPDomain.DL_TYPE_DISC;
                if (vmBay.getVMRowByRowNo(rowNo) != null) {
                    int topTierNo = vmBay.getVMRowByRowNo(rowNo).getTopTierNo(); // 顶层层号有可能为-1，表示该槽没有船箱位
                    if (vmBayA.getRowNoList().size() == 0) {
                        topTierNo = 50;
                    }
                    vmSlot = structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, topTierNo).getVLocation());
                    workMove = moveData.getWorkMoveByVMSlot(vmSlot, dlType);
                    if (workMove != null && workMove.getMoveOrder() == null) {
                        if (isTopWorkMove(workMove, moveData, structureData, workingData)) {
                            moveData.putCurTopWorkMove(hatchId, workMove);
                        }
                        continue;
                    }
                    int bottom = 50;
                    if (vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null && vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).hasVMSlot()) {
                        bottom = vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getBottomTierNo();
                    }
                    while (structureData.hasNextVMSlot(vmSlot, dlType)) {
                        if (CWPDomain.DL_TYPE_DISC.equals(dlType) && vmSlot.getVmPosition().getTierNo() == bottom) {
                            dlType = CWPDomain.DL_TYPE_LOAD;
                            workMove = moveData.getWorkMoveByVMSlot(vmSlot, dlType);
                            if (workMove != null && workMove.getMoveOrder() == null) {
                                if (isTopWorkMove(workMove, moveData, structureData, workingData)) {
                                    moveData.putCurTopWorkMove(hatchId, workMove);
                                }
                                break;
                            }
                        }
                        vmSlot = structureData.getNextVMSlot(vmSlot, dlType);
                        workMove = moveData.getWorkMoveByVMSlot(vmSlot, dlType);
                        if (workMove != null && workMove.getMoveOrder() == null) {
                            if (isTopWorkMove(workMove, moveData, structureData, workingData)) {
                                moveData.putCurTopWorkMove(hatchId, workMove);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean isTopWorkMove(WorkMove workMove, MoveData moveData, StructureData structureData, WorkingData workingData) {
        if (workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
            WorkMove workMoveD = moveData.getWorkMoveByVMSlot(workMove.getOneVMSlot(), CWPDomain.DL_TYPE_DISC);
            if (workMoveD != null && workMoveD.getMoveOrder() == null) { //该slot对应卸船的move没有做， 装船的move就不能做
                return false;
            }
        }
        // 如果是舱盖板Move改用独立的方法判断
        // 卸船舱盖板判断是否可以卸，对应甲板上卸船的最低层集装箱move是否卸完；装船舱盖板判断是否可以装，对应甲板下装船的最高层集装箱move是否装完，如果没有装船则判断甲板下最低层卸船
        if (CWPDomain.MOVE_TYPE_HC.equals(workMove.getMoveType())) {
            for (VMSlot vmSlot : workMove.getHcVMSlotList()) {
                WorkMove workMoveFront = moveData.getWorkMoveByVMSlot(vmSlot, workMove.getDlType());
                if (workMoveFront != null && workMoveFront.getMoveOrder() == null) {
                    return false;
                }
            }
            for (VMSlot vmSlot : workMove.getHcVMSlotListD()) {
                WorkMove workMoveFrontD = moveData.getWorkMoveByVMSlot(vmSlot, CWPDomain.DL_TYPE_DISC);
                if (workMoveFrontD != null && workMoveFrontD.getMoveOrder() == null) {
                    return false;
                }
            }
        } else {
            // 集装箱Move所有VMSlot上面/下面的VMSlot对应的Move是空或者已经编序
            for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                VMSlot vmSlotFront = vmSlot;
                String dlType = workMove.getDlType();
                VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_BELOW));
                int bottom = 50;
                if (vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null && vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).hasVMSlot()) {
                    bottom = vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getBottomTierNo();
                }
                while (structureData.hasFrontVMSlot(vmSlotFront, dlType)) {
                    if (CWPDomain.DL_TYPE_LOAD.equals(dlType) && vmSlotFront.getVmPosition().getTierNo() == bottom) { //由装变成卸
                        dlType = CWPDomain.DL_TYPE_DISC;
                        WorkMove workMoveFront = moveData.getWorkMoveByVMSlot(vmSlotFront, dlType);
                        if (workMoveFront != null && workMoveFront.getMoveOrder() == null) { // move没有编序，则说明该槽还有箱子没有作业，结果判断为不是顶层
                            return false;
                        }
                    }
                    // 如果该倍位两边槽的上一层有三超箱、超宽箱，则不是顶层
                    if (aboveIsOverrunCnt(vmSlotFront, dlType, moveData, structureData, workingData)) {
                        return false;
                    }
                    vmSlotFront = structureData.getFrontVMSlot(vmSlotFront, dlType);
                    WorkMove workMoveFront = moveData.getWorkMoveByVMSlot(vmSlotFront, dlType);
                    if (workMoveFront != null && workMoveFront.getMoveOrder() == null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Set<WorkMove> getNextTopWorkMoveSet(WorkMove workMove, MoveData moveData, StructureData structureData, WorkingData workingData) {
        Set<WorkMove> workMoveSet = new LinkedHashSet<>();
        List<VMSlot> tempVmSlotList = new ArrayList<>(workMove.getVmSlotSet());
        // 如果当前move是三超箱、超宽箱，则还需要判断旁边两根槽的下一个move是不是顶层
        if (CWPDomain.DL_TYPE_DISC.equals(workMove.getDlType()) && CWPDomain.MOVE_TYPE_CNT.equals(workMove.getMoveType())) {
            for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                if (vmContainer != null && PublicMethod.isOverrunWidthCnt(vmContainer.getOverrunCd())) {
                    VMSlot vmSlotR = structureData.getSideVMSlot(vmSlot, CWPDomain.ROW_SEQ_ODD_EVEN);
                    if (vmSlotR != null) {
                        tempVmSlotList.add(vmSlotR);
                    }
                    VMSlot vmSlotL = structureData.getSideVMSlot(vmSlot, CWPDomain.ROW_SEQ_EVEN_ODD);
                    if (vmSlotL != null) {
                        tempVmSlotList.add(vmSlotL);
                    }
                }
            }
        }
        // 按排号进行查找下一个顶层move：当是大倍位时，则需要判断对面slot对应的move是不是相同；小倍位时，逻辑不变
        Set<VMSlot> vmSlotSet = new HashSet<>();
        if (workMove.getBayNo() % 2 == 0) {
            Map<Integer, List<VMSlot>> rowNoVMSlotMap = new HashMap<>(); // 同排同层只取一个倍位的slot，并且slot对应甲板下的槽层号取小的
            for (VMSlot vmSlot : tempVmSlotList) {
                if (rowNoVMSlotMap.get(vmSlot.getVmPosition().getRowNo()) == null) {
                    rowNoVMSlotMap.put(vmSlot.getVmPosition().getRowNo(), new ArrayList<VMSlot>());
                }
                rowNoVMSlotMap.get(vmSlot.getVmPosition().getRowNo()).add(vmSlot);
            }
            for (Map.Entry<Integer, List<VMSlot>> entry : rowNoVMSlotMap.entrySet()) {
                // 比较同排同层slot对应甲板下的槽栈底层号小
                if (entry.getValue().size() == 2) {
                    int bottom = 50;
                    VMSlot vmSlot = entry.getValue().get(0);
                    VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_BELOW));
                    if (vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null && vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).hasVMSlot()) {
                        bottom = vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getBottomTierNo();
                    }
                    int bottom1 = 50;
                    VMSlot vmSlot1 = entry.getValue().get(1);
                    VMBay vmBayB1 = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot1.getVmPosition().getBayNo(), CWPDomain.BOARD_BELOW));
                    if (vmBayB1.getVMRowByRowNo(vmSlot1.getVmPosition().getRowNo()) != null && vmBayB1.getVMRowByRowNo(vmSlot1.getVmPosition().getRowNo()).hasVMSlot()) {
                        bottom1 = vmBayB1.getVMRowByRowNo(vmSlot1.getVmPosition().getRowNo()).getBottomTierNo();
                    }
                    if (bottom <= bottom1) {
                        vmSlotSet.add(vmSlot);
                    } else {
                        vmSlotSet.add(vmSlot1);
                    }
                } else {
                    vmSlotSet.addAll(entry.getValue());
                }
            }
        } else {
            vmSlotSet = new HashSet<>(tempVmSlotList);
        }
        for (VMSlot vmSlot : vmSlotSet) {
            VMSlot vmSlotNext = vmSlot;
            String dlType = workMove.getDlType();
            VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_BELOW));
            int bottom = 50;
            if (vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null && vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).hasVMSlot()) {
                bottom = vmBayB.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getBottomTierNo();
            }
            while (structureData.hasNextVMSlot(vmSlotNext, dlType)) {
                if (CWPDomain.DL_TYPE_DISC.equals(dlType) && vmSlotNext.getVmPosition().getTierNo() == bottom) { //从卸船变成装船
                    dlType = CWPDomain.DL_TYPE_LOAD;
                    WorkMove workMoveNext = moveData.getWorkMoveByVMSlot(vmSlotNext, dlType);
                    if (vmSlotNext instanceof VMContainerSlot) { // todo: 是否要加大倍位前提条件
                        VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlotNext);
                        WorkMove workMovePair = moveData.getWorkMoveByVMSlot(vmSlotPair, dlType);
                        if ((workMoveNext == null && workMovePair != null && workMovePair.getMoveOrder() == null)) { // 当前slot对应的move为空，对面slot对应的move不为空且没有编序，则跳出该层slot
                            break;
                        }
                    }
                    if (workMoveNext != null && workMoveNext.getMoveOrder() == null) {
                        if (workMoveNext.getBayNo().equals(workMove.getBayNo())) {
                            if (workMoveNext.getRowNoNumber() == 1) { // 当前move只有一根槽的时候，默认为该move是集装箱move，则肯定是顶层；
                                workMoveSet.add(workMoveNext);
                            } else {
                                if (isTopWorkMove(workMoveNext, moveData, structureData, workingData)) {
                                    workMoveSet.add(workMoveNext);
                                }
                            }
                        }
                        break;
                    }
                }
                vmSlotNext = structureData.getNextVMSlot(vmSlotNext, dlType);
                WorkMove workMoveNext = moveData.getWorkMoveByVMSlot(vmSlotNext, dlType);
                if (vmSlotNext instanceof VMContainerSlot) { // todo: 是否要加大倍位前提条件
                    VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlotNext);
                    WorkMove workMovePair = moveData.getWorkMoveByVMSlot(vmSlotPair, dlType);
                    if ((workMoveNext == null && workMovePair != null && workMovePair.getMoveOrder() == null)) { // 当前slot对应的move为空，对面slot对应的move不为空且没有编序，则跳出该层slot
                        break;
                    }
                }
                if (workMoveNext != null && workMoveNext.getMoveOrder() == null) {
                    if (workMoveNext.getBayNo().equals(workMove.getBayNo())) {
                        if (workMoveNext.getRowNoNumber() == 1) {
                            if (!aboveIsOverrunCnt(vmSlotNext, dlType, moveData, structureData, workingData)) { // move旁边两个槽上一层箱子是否是超限箱
                                workMoveSet.add(workMoveNext);
                            }
                        } else {
                            if (isTopWorkMove(workMoveNext, moveData, structureData, workingData)) {
                                workMoveSet.add(workMoveNext);
                            }
                        }
                    }
                    break;
                }
            }
        }

        return workMoveSet;
    }

    private boolean aboveIsOverrunCnt(VMSlot vmSlot, String dlType, MoveData moveData, StructureData structureData, WorkingData workingData) {
        boolean overrunFlagR = false;
        boolean overrunFlagL = false;
        if (CWPDomain.DL_TYPE_DISC.equals(dlType)) {
            if (vmSlot instanceof VMContainerSlot) {
                VMSlot vmSlotR = structureData.getSideVMSlot(vmSlot, CWPDomain.ROW_SEQ_ODD_EVEN);
                VMSlot vmSlotL = structureData.getSideVMSlot(vmSlot, CWPDomain.ROW_SEQ_EVEN_ODD);
                overrunFlagR = overrunWidthFlag(vmSlotR, moveData, structureData, workingData);
                overrunFlagL = overrunWidthFlag(vmSlotL, moveData, structureData, workingData);
            }
            return overrunFlagR || overrunFlagL;
        }
        return false;
    }

    private boolean overrunWidthFlag(VMSlot vmSlotR, MoveData moveData, StructureData structureData, WorkingData workingData) {
        if (vmSlotR != null) {
            VMSlot vmSlotRA = structureData.getAboveVMSlot(vmSlotR);
            if (vmSlotRA != null) {
                VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlotRA, CWPDomain.DL_TYPE_DISC);
                WorkMove workMove = moveData.getWorkMoveByVMSlot(vmSlotRA, CWPDomain.DL_TYPE_DISC);
                if (vmContainer == null || workMove == null) {
                    return false;
                }
                if (workMove.getMoveOrder() != null) { // 说明箱子已经编序作业了，认为旁边两个槽没有三超箱、超宽箱
                    return false;
                }
                return PublicMethod.isOverrunWidthCnt(vmContainer.getOverrunCd()); // 三超箱、超宽箱
            }
        }
        return false;
    }

    private Map<String, WorkMove> copyWorkMoveMap(Map<String, WorkMove> workMoveMap) {
        Map<String, WorkMove> moveMap = new HashMap<>();
        Set<WorkMove> workMoveSet = new HashSet<>(workMoveMap.values());
        for (WorkMove workMove : workMoveSet) {
            WorkMove workMoveCopy = new WorkMove(workMove.getDlType(), workMove.getWorkFlow(), workMove.getWorkTime(), workMove.getMoveType());
            workMoveCopy.setHatchId(workMove.getHatchId());
            for (VMSlot vmSlot : workMove.getVmSlotSet()) { //VMSlot不需要深复制
                workMoveCopy.getVmSlotSet().add(vmSlot);
            }
            workMoveCopy.setHcRowNoList(workMove.getHcRowNoList()); //舱盖板排号不需要深复制
            workMoveCopy.setHcVMSlotList(workMove.getHcVMSlotList()); // 用于判断舱盖板顶层条件的slot不需要深复制
            workMoveCopy.setHcVMSlotListD(workMove.getHcVMSlotListD()); // 用于判断装船舱盖板是否可以装，但是舱下没有装船箱子时，需要判断卸是否卸完
            for (VMSlot vmSlot : workMoveCopy.getVmSlotSet()) {
                moveMap.put(vmSlot.getVmPosition().getVLocation(), workMoveCopy);
            }
            workMoveCopy.setBayNo(workMove.getBayNo());
            workMoveCopy.setRowNo(workMove.getRowNo());
            workMoveCopy.setTierNo(workMove.getTierNo());
            workMoveCopy.setHcSeq(workMove.getHcSeq());
            workMoveCopy.setWorkFirst(workMove.getWorkFirst());
            workMoveCopy.setWorkFirstOrder(workMove.getWorkFirstOrder());
            workMoveCopy.setHcWorkFlow(workMove.getHcWorkFlow());
            workMoveCopy.setCwoCraneNo(workMove.getCwoCraneNo());
            workMoveCopy.setMoveOrder(workMove.getMoveOrder());
            workMoveCopy.setPlanStartTime(workMove.getPlanStartTime());
            workMoveCopy.setPlanEndTime(workMove.getPlanEndTime());
            workMoveCopy.setSelectReason(workMove.getSelectReason());
            workMoveCopy.setCraneNo(workMove.getCraneNo());
            workMoveCopy.setWorkPosition(workMove.getWorkPosition());
        }
        return moveMap;
    }

    MoveData copyMoveData(MoveData moveData) {
        MoveData moveDataCopy = new MoveData();
        moveDataCopy.setDiscWorkMoveMap(this.copyWorkMoveMap(moveData.getDiscWorkMoveMap()));
        moveDataCopy.setLoadWorkMoveMap(this.copyWorkMoveMap(moveData.getLoadWorkMoveMap()));
        for (Map.Entry<Long, Long> entry : moveData.getCurMoveOrderMap().entrySet()) {
            moveDataCopy.setCurMoveOrder(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Long, String> entry : moveData.getCurWorkFlowMap().entrySet()) {
            moveDataCopy.setCurWorkFlow(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, String> entry : moveData.getCurWorkFlowMap1().entrySet()) {
            moveDataCopy.setCurWorkFlow1(entry.getKey(), entry.getValue());
        }
        return moveDataCopy;
    }
}
