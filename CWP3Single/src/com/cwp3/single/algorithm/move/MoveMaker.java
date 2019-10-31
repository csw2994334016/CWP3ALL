package com.cwp3.single.algorithm.move;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.crane.CMCraneWorkFlow;
import com.cwp3.model.vessel.*;
import com.cwp3.model.work.HatchBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.maker.AbstractMaker;
import com.cwp3.single.algorithm.move.maker.M20Dual;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.utils.StringUtil;

import java.util.*;

/**
 * Created by csw on 2017/9/19.
 * Description:
 */
public class MoveMaker {

    public void makeMove(Long hatchId, String dlType, WorkingData workingData, StructureData structureData) {
        String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
        List<VMBay> vmBayList = structureData.getVMBayListByHatchId(hatchId);
        List<Integer> rowSeqList;
        // 1、集装箱move，只是单吊具、双箱吊
        List<WorkMove> singleWorkMoveList = new ArrayList<>();
        Map<String, WorkMove> singleWorkMoveMap = new HashMap<>();
        List<String> vLocationList = new ArrayList<>(); // 没有找到排号对应的舱盖板
        for (VMBay vmBay : vmBayList) {
            int maxTier = vmBay.getMaxTier();
            int minTier = vmBay.getMinTier();
            for (int tierNo = maxTier; tierNo >= minTier; ) {
                //根据bayId, 装卸方向，得到该倍位块所有排号
                rowSeqList = structureData.getRowSeqListByOddOrEven(vmBay.getBayId(), oddOrEven);
                for (Integer rowNo : rowSeqList) {
                    VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(vmBay.getBayNo(), rowNo, tierNo).getVLocation());
                    if (vmSlot != null) {
                        VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
                        if (vmContainer != null) {
                            Set<VMSlot> vmSlotSet = new HashSet<>();
                            if (PublicMethod.hasNoneWorkFlow(vmContainer.getWorkFlow())) { //没编写作业工艺
                                String key = StringUtil.getKey(StringUtil.getKey(hatchId, vmBay.getAboveOrBelow()), dlType); // 舱、甲板上下、装卸类型（"hatchId@A/B@L/D"）
                                CMCraneWorkFlow cmCraneWorkFlow = workingData.getCMCraneWorkFlowByKey(key);
                                List<AbstractMaker> ptSeqList = PublicMethod.getSinglePTSeqListByCMCraneWorkFlow(cmCraneWorkFlow);
                                for (AbstractMaker maker : ptSeqList) {
                                    boolean canDo = maker.canDo(vmSlot, dlType, workingData, structureData);
                                    if (canDo) {
                                        vmContainer.setWorkFlow(maker.getWorkFlow());
                                        vmSlotSet = maker.getVmSlotSet();
                                    }
                                    if (maker instanceof M20Dual) { // 如果是20尺箱子，双箱吊不能作业，则一定不能4小箱双吊具
                                        if (!canDo && vmContainer.getSize().startsWith("2")) {
                                            break;
                                        }
                                    }
                                }
                            }
                            //集装箱Move
                            if (singleWorkMoveMap.get(vmSlot.getVmPosition().getVLocation()) == null) { // VMSlot对应的WorkMove不存在，才生成move；如果存在则不用管
                                WorkMove workMove = createWorkMove(hatchId, dlType, vmSlot, vmContainer, vmSlotSet, vLocationList, workingData, structureData);
                                if (workMove != null) {
                                    addWorkMoveToMap(workMove, singleWorkMoveMap);
                                    singleWorkMoveList.add(workMove);
                                }
                            }
                        }
                    }
                }
                tierNo -= 2;
            }
        }
        if (vLocationList.size() > 0) {
            workingData.getLogger().logError("船箱位的排号找不到对应的舱盖板，不支持该舱盖板结构，船箱位有：" + vLocationList.toString());
        }
        // 2、舱盖板move
        String[] dlTypes = new String[]{CWPDomain.DL_TYPE_DISC, CWPDomain.DL_TYPE_LOAD};
        HatchBlock hatchBlock = workingData.getHatchBlockByHatchId(hatchId);
        long workTime = dlType.equals(CWPDomain.DL_TYPE_DISC) ? workingData.getCwpConfig().getHatchCoverTimeD() : workingData.getCwpConfig().getHatchCoverTimeL();
        Map<Integer, List<Integer>> aboveBlockMap = hatchBlock.getAboveBlockMap();
        VMHatchCoverSlot vmHatchCoverSlot;
        List<WorkMove> hcWorkMoveList = new ArrayList<>();
        String hcWF = CWPCraneDomain.CT_DUAL40;
        for (Integer hcSeq : aboveBlockMap.keySet()) {
            WorkMove workMove = new WorkMove(dlType, CWPCraneDomain.CT_HATCH_COVER, workTime, CWPDomain.MOVE_TYPE_HC);
            workMove.setHcRowNoList(aboveBlockMap.get(hcSeq));
            //舱盖板下面有箱子与否，决定舱盖板move是否存在
            boolean haveCntBelow = false;
            if (!CWPDomain.CWP_TYPE_WORK.equals(workingData.getCwpType())) { // 重排认为舱盖板下面没有箱子，没有舱盖板Move
                BayNo:
                for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                    for (Integer rowNo : workMove.getHcRowNoList()) {
                        for (int tierNo = 50; tierNo >= 0; ) {
                            VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, tierNo).getVLocation());
                            for (String dlType1 : dlTypes) {
                                VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType1);
                                if (vmContainer != null) {
                                    haveCntBelow = true;
                                    break BayNo;
                                }
                            }
                            tierNo -= 2;
                        }
                    }
                }
            }
            if (haveCntBelow) {
                //添加vmHatchCoverSlot
                for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                    VMBay vmBayA = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_ABOVE));
                    for (Integer rowNo : workMove.getHcRowNoList()) {
                        if (vmBayA.getVMRowByRowNo(rowNo) != null) { //船舶结构没有的槽，舱盖板slot不初始化
                            vmHatchCoverSlot = (VMHatchCoverSlot) structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, 50).getVLocation());
                            workMove.addVmSlot(vmHatchCoverSlot);
                            workMove.setRowNo(rowNo);
                        }
                    }
                }
                workMove.setHatchId(hatchId);
                workMove.setBayNo(structureData.getVMHatchByHatchId(hatchId).getBayNoD());
                workMove.setTierNo(50);
                //move属于哪个档
                workMove.setHcSeq(hcSeq);
                // 判断舱盖板move是否优先作业
                int t = CWPDomain.DL_TYPE_DISC.equals(dlType) ? -2 : 2;
                int endTierNo = CWPDomain.DL_TYPE_DISC.equals(dlType) ? 0 : 104;
                BayNo:
                for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                    RowNo:
                    for (Integer rowNo : workMove.getHcRowNoList()) {
                        int tierNo = 50;
                        boolean condition = true;
                        for (; condition; ) {
                            VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, tierNo).getVLocation());
                            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
                            if (vmContainer != null) {
                                if (CWPDomain.YES.equals(vmContainer.getWorkFirst())) {
                                    workMove.setWorkFirst(CWPDomain.YES);
                                    workMove.setWorkFirstOrder(0L);
                                    break BayNo;
                                } else {
                                    continue RowNo;
                                }
                            }
                            tierNo += t;
                            condition = CWPDomain.DL_TYPE_DISC.equals(dlType) ? tierNo >= endTierNo : tierNo <= endTierNo;
                        }
                    }
                }
                // 舱盖板判断为顶层可以作业，必须提前作业的箱子有哪些，加快顶层move的判断
                t = CWPDomain.DL_TYPE_DISC.equals(dlType) ? 2 : -2;
                endTierNo = CWPDomain.DL_TYPE_DISC.equals(dlType) ? 104 : 0;
                for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                    for (Integer rowNo : workMove.getHcRowNoList()) {
                        int tierNo = 50;
                        boolean condition = true;
                        for (; condition; ) { // 每排逐层遍历，最后一个作业箱子的slot
                            VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, tierNo).getVLocation());
                            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
                            if (vmContainer != null) {
                                workMove.getHcVMSlotList().add(vmSlot);
                                break;
                            }
                            tierNo += t;
                            condition = CWPDomain.DL_TYPE_DISC.equals(dlType) ? tierNo <= endTierNo : tierNo >= endTierNo;
                        }
                    }
                }
                // 如果装船舱盖板move舱下没有最高层装船集装箱move，则需要获取舱下最低层卸船集装箱move
                if (CWPDomain.DL_TYPE_LOAD.equals(workMove.getDlType()) && workMove.getHcVMSlotList().size() == 0) {
                    for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getBayNos()) {
                        VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_BELOW));
                        for (Integer rowNo : workMove.getHcRowNoList()) {
                            if (vmBayB.getVMRowByRowNo(rowNo) != null && vmBayB.getVMRowByRowNo(rowNo).hasVMSlot()) {
                                int tierNo = vmBayB.getVMRowByRowNo(rowNo).getBottomTierNo();
                                VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(bayNo, rowNo, tierNo).getVLocation());
                                VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, CWPDomain.DL_TYPE_DISC);
                                if (vmContainer != null) {
                                    workMove.getHcVMSlotListD().add(vmSlot);
                                }
                            }
                        }
                    }
                }
                // 设置舱盖板是否可以使用双吊具作业
                workMove.setHcWorkFlow(CWPCraneDomain.CT_SINGLE40);
                VMHatchCover vmHatchCover = hatchBlock.getVMHatchCoverBySeq(hcSeq);
                if (vmHatchCover != null) {
                    if (vmHatchCover.getWeight() != null && vmHatchCover.getWeight() <= 35000) { // 舱盖板重量<=35吨(35000kg)
                        workMove.setHcWorkFlow(CWPCraneDomain.CT_DUAL40);
                    } else {
                        if (hatchBlock.getBelowBlockMap().get(hcSeq) != null && hatchBlock.getBelowBlockMap().get(hcSeq).size() <= 4) { // 舱盖板覆盖的槽小于4根，则可以使用双吊具
                            workMove.setHcWorkFlow(CWPCraneDomain.CT_DUAL40);
                        }
                    }
                }
                if (CWPCraneDomain.CT_SINGLE40.equals(workMove.getHcWorkFlow())) {
                    hcWF = CWPCraneDomain.CT_SINGLE40;
                }
                workingData.addWorkMove(workMove);
                hcWorkMoveList.add(workMove);
            }
        }
        // 如果有一块舱盖板是单吊具作业，则所有舱盖板使用单吊具作业
        if (CWPCraneDomain.CT_SINGLE40.equals(hcWF)) {
            for (WorkMove workMove : hcWorkMoveList) {
                workMove.setHcWorkFlow(CWPCraneDomain.CT_SINGLE40);
            }
        }
        // 需要将非人工设置的集装箱作业工艺设置为null，因为后面还要重新设置工艺
        clearVMContainerWorkFlow(singleWorkMoveList, workingData);
        // 2、双吊具move（卸船），判断是否使用双吊具卸船，则进行下面的逻辑，否则不需要处理
        boolean tandemFlag = false;
        CMCraneWorkFlow cmCraneWorkFlow = workingData.getCMCraneWorkFlowByKey(StringUtil.getKey(StringUtil.getKey(hatchId, CWPDomain.BOARD_ABOVE), dlType));
        if (cmCraneWorkFlow != null && cmCraneWorkFlow.getTandem()) {
            tandemFlag = true;
        }
        cmCraneWorkFlow = workingData.getCMCraneWorkFlowByKey(StringUtil.getKey(StringUtil.getKey(hatchId, CWPDomain.BOARD_BELOW), dlType));
        if (cmCraneWorkFlow != null && cmCraneWorkFlow.getTandem()) {
            tandemFlag = true;
        }
        if (CWPDomain.DL_TYPE_DISC.equals(dlType) && tandemFlag) { // 卸船、该舱需要安排双吊具
            Map<String, WorkMove> discDoubleWorkMoveMapA = new HashMap<>(); // 默认的双吊具map，甲板上
            Map<String, WorkMove> discDoubleWorkMoveMapB = new HashMap<>(); // 默认的双吊具map，甲板下
            List<VMBay> vmBayListA = new ArrayList<>();
            List<VMBay> vmBayListB = new ArrayList<>();
            for (VMBay vmBay : vmBayList) {
                if (CWPDomain.BOARD_ABOVE.equals(vmBay.getAboveOrBelow())) {
                    vmBayListA.add(vmBay);
                } else {
                    vmBayListB.add(vmBay);
                }
            }
            // 分甲板上/下生成双吊具卸船move
            createDoubleWorkMoveMap(hatchId, dlType, vmBayListA, vLocationList, discDoubleWorkMoveMapA, workingData, structureData);
            createDoubleWorkMoveMap(hatchId, dlType, vmBayListB, vLocationList, discDoubleWorkMoveMapB, workingData, structureData);
            if (discDoubleWorkMoveMapA.size() > 0) {
                // 分析是否可以将单吊具调整到独立做单吊的一个列上，少数单吊调到多数单吊列上
                analyzeDiscDoubleWorkMove(hatchId, dlType, vmBayListA, vLocationList, discDoubleWorkMoveMapA, workingData, structureData);
            }
            if (discDoubleWorkMoveMapB.size() > 0) {
                // 分析是否可以将单吊具调整到独立做单吊的一个列上，少数单吊调到多数单吊列上
                analyzeDiscDoubleWorkMove(hatchId, dlType, vmBayListB, vLocationList, discDoubleWorkMoveMapB, workingData, structureData);
            }
            if (discDoubleWorkMoveMapA.size() > 0) {
                // 将move放入WorkingData中
                for (Map.Entry<String, WorkMove> entry : discDoubleWorkMoveMapA.entrySet()) {
                    workingData.getDiscWorkMoveMap().put(entry.getKey(), entry.getValue());
                }
            }
            if (discDoubleWorkMoveMapB.size() > 0) {
                // 拆分甲板下少于10关的双吊具
                apartBelowDiscDoubleWorkMove(hatchId, dlType, discDoubleWorkMoveMapB, workingData, structureData);
                // 将move放入WorkingData中
                for (Map.Entry<String, WorkMove> entry : discDoubleWorkMoveMapB.entrySet()) {
                    workingData.getDiscWorkMoveMap().put(entry.getKey(), entry.getValue());
                }
            }
            if (discDoubleWorkMoveMapA.size() > 0) {
                // 拆分甲板上少于10关的双吊具
                apartAboveDiscDoubleWorkMove(hatchId, dlType, discDoubleWorkMoveMapA, workingData, structureData);
                // 将move放入WorkingData中
                for (Map.Entry<String, WorkMove> entry : discDoubleWorkMoveMapA.entrySet()) {
                    workingData.getDiscWorkMoveMap().put(entry.getKey(), entry.getValue());
                }
            }
            // WorkingData对象存储的卸船双吊具工艺的map需要添加：小倍位作业的move
            for (WorkMove workMove : singleWorkMoveList) {
                if (CWPDomain.DL_TYPE_DISC.equals(workMove.getDlType()) && (CWPCraneDomain.CT_SINGLE20.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_TWIN20.equals(workMove.getWorkFlow()))) { // 卸船、20尺单吊具
                    workingData.addWorkMove(workMove);
                }
            }
        } else { // 装船、不能安排双吊具，则直接按singleWorkMoveMap保存结果
            for (WorkMove workMove : singleWorkMoveList) {
                workingData.addWorkMove(workMove);
            }
        }
    }

    private void analyzeDiscDoubleWorkMove(Long hatchId, String dlType, List<VMBay> vmBayList, List<String> vLocationList, Map<String, WorkMove> discDoubleWorkMoveMap, WorkingData workingData, StructureData structureData) {
        Map<Integer, Set<WorkMove>> rowNoWorkMoveMap = new HashMap<>();
        Map<Integer, Set<WorkMove>> tireNoWorkMoveMap = new HashMap<>();
        Map<Integer, Set<WorkMove>> tireNoWorkMoveMapAll = new HashMap<>();
        for (Map.Entry<String, WorkMove> entry : discDoubleWorkMoveMap.entrySet()) {
            WorkMove workMove = entry.getValue();
            if (CWPCraneDomain.CT_SINGLE40.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_DUAL20.equals(workMove.getWorkFlow())) {
                if (rowNoWorkMoveMap.get(workMove.getRowNo()) == null) {
                    rowNoWorkMoveMap.put(workMove.getRowNo(), new HashSet<WorkMove>());
                }
                rowNoWorkMoveMap.get(workMove.getRowNo()).add(workMove);
                if (workMove.getTierNo() < 50) { // 横向只考虑甲板下
                    if (tireNoWorkMoveMap.get(workMove.getTierNo()) == null) {
                        tireNoWorkMoveMap.put(workMove.getTierNo(), new HashSet<WorkMove>());
                    }
                    tireNoWorkMoveMap.get(workMove.getTierNo()).add(workMove);
                }
            }
            if (tireNoWorkMoveMapAll.get(workMove.getTierNo()) == null) {
                tireNoWorkMoveMapAll.put(workMove.getTierNo(), new HashSet<WorkMove>());
            }
            tireNoWorkMoveMapAll.get(workMove.getTierNo()).add(workMove);
        }
        // 一、先按列进行处理，去掉单吊具从顶层到底层连续的独立列
        int rowNo = -1;
        int num = 0;
        Set<Integer> tireNoSet = new HashSet<>();
        for (Map.Entry<Integer, Set<WorkMove>> entry : rowNoWorkMoveMap.entrySet()) {
            List<WorkMove> workMoveList = new ArrayList<>(entry.getValue());
            Collections.sort(workMoveList, new Comparator<WorkMove>() {
                @Override
                public int compare(WorkMove o1, WorkMove o2) {
                    return o1.getTierNo().compareTo(o2.getTierNo());
                }
            });
            if (workMoveList.size() > 1) {
                int t = (workMoveList.get(workMoveList.size() - 1).getTierNo() - workMoveList.get(0).getTierNo()) / 2;
                if (t != workMoveList.size() - 1) { // 说明该列单吊具不连续
                    for (WorkMove workMove : workMoveList) {
                        tireNoSet.add(workMove.getTierNo());
                    }
                    if (workMoveList.size() > num) {
                        rowNo = entry.getKey();
                        num = workMoveList.size();
                    }
                }
            }
        }
        // 不连续单吊具的列，单吊具个数最大，看能否统一调整到该列上
        if (rowNo > -1) {
            List<Integer> tireNoList = new ArrayList<>(tireNoSet);
            Collections.sort(tireNoList);
            List<Integer> tireNoListNew = new ArrayList<>();
            for (Integer tireNo : tireNoList) {
                tireNoListNew.add(tireNo - tireNoList.get(0) + 2);
            }
            int t = (tireNoListNew.get(tireNoListNew.size() - 1) - tireNoListNew.get(0)) / 2;
            if (t == tireNoListNew.size() - 1) { // 说明能够统一调整到这个列上，则先安排单吊具，然后重新进行双吊具安排
                workingData.getLogger().logInfo("根据逐列(" + rowNo + ")分析后，重新安排舱(" + hatchId + ")卸船双吊具工艺。");
                // 1、清空箱子工艺
                clearVMContainerWorkFlow(new ArrayList<>(discDoubleWorkMoveMap.values()), workingData);
                // 2、清空map
                discDoubleWorkMoveMap.clear();
                // 3、安排单吊具
                for (VMBay vmBay : vmBayList) {
                    int maxTier = tireNoList.get(tireNoList.size() - 1);
                    int minTier = tireNoList.get(0);
                    for (int tierNo = maxTier; tierNo >= minTier; ) {
                        // 得到slot，根据slot对集装箱进行编写工艺生成move
                        VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(vmBay.getBayNo(), rowNo, tierNo).getVLocation());
                        createDSingleWorkMoveMap(hatchId, dlType, vmSlot, vLocationList, discDoubleWorkMoveMap, workingData, structureData);
                        tierNo -= 2;
                    }
                }
                // 4、生成新的双吊具move
                createDoubleWorkMoveMap(hatchId, dlType, vmBayList, vLocationList, discDoubleWorkMoveMap, workingData, structureData);
            } else {
                // 二、按层进行处理，将同层连续遍历，（箱尺寸相同），中途出现的单吊具放到第一个来
                String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
                String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
                final List<Integer> rowNoList = structureData.getRowSeqListBySeaOrLand(hatchId, CWPDomain.BOARD_BELOW, oddOrEven);
                Set<VMSlot> vmSlotSetF = new HashSet<>();
                for (Map.Entry<Integer, Set<WorkMove>> entry : tireNoWorkMoveMap.entrySet()) {
                    List<WorkMove> workMoveList = new ArrayList<>(tireNoWorkMoveMapAll.get(entry.getKey()));
                    Collections.sort(workMoveList, new Comparator<WorkMove>() {
                        @Override
                        public int compare(WorkMove o1, WorkMove o2) {
                            return Integer.compare(rowNoList.indexOf(o1.getRowNo()), rowNoList.indexOf(o2.getRowNo()));
                        }
                    });
                    for (int i = 0; i < workMoveList.size() - 1; i++) {
                        // 连续过来，遇到第一个单吊具，且不是第一个和最后一个，则找到第一个slot，安排成单吊具，结束循环
                        WorkMove workMove = workMoveList.get(i);
                        if (CWPCraneDomain.CT_SINGLE40.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_DUAL20.equals(workMove.getWorkFlow())) {
                            if (i == 0) {
                                break;
                            }
                            WorkMove workMoveF = null;
                            for (int j = i - 1; j >= 0; j--) {
                                WorkMove workMove1 = workMoveList.get(j);
                                if (workMove1.getWorkFlow().substring(1, 2).equals(workMove.getWorkFlow().substring(1, 2))) {
                                    workMoveF = workMove1;
                                }
                            }
                            if (workMoveF != null) {
                                for (VMSlot vmSlot : workMoveF.getVmSlotSet()) { // 一般认为第一个move下面没有slot，则适合使用单吊具
                                    VMSlot vmSlotB = structureData.getBelowVMSlot(vmSlot);
                                    if (vmSlotB == null) {
                                        vmSlotSetF.add(vmSlot);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (vmSlotSetF.size() > 0) {
                    // 1、清空箱子工艺
                    clearVMContainerWorkFlow(new ArrayList<>(discDoubleWorkMoveMap.values()), workingData);
                    // 2、清空map
                    discDoubleWorkMoveMap.clear();
                    // 3、安排单吊具
                    for (VMSlot vmSlot : vmSlotSetF) {
                        workingData.getLogger().logInfo("根据逐层(" + vmSlot.getVmPosition().getVLocation() + ")分析后，重新安排舱(" + hatchId + ")卸船双吊具工艺。");
                        createDSingleWorkMoveMap(hatchId, dlType, vmSlot, vLocationList, discDoubleWorkMoveMap, workingData, structureData);
                    }
                    // 4、生成新的双吊具move
                    createDoubleWorkMoveMap(hatchId, dlType, vmBayList, vLocationList, discDoubleWorkMoveMap, workingData, structureData);
                }
            }
        }
    }

    private void createDSingleWorkMoveMap(Long hatchId, String dlType, VMSlot vmSlot, List<String> vLocationList, Map<String, WorkMove> discDoubleWorkMoveMap, WorkingData workingData, StructureData structureData) {
        if (vmSlot != null) {
            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
            if (vmContainer != null) {
                Set<VMSlot> vmSlotSet = new HashSet<>();
                if (PublicMethod.hasNoneWorkFlow(vmContainer.getWorkFlow())) { //没编写作业工艺
                    List<AbstractMaker> ptSeqList = PublicMethod.getDSinglePTSeqList();
                    for (AbstractMaker maker : ptSeqList) {
                        boolean canDo = maker.canDo(vmSlot, dlType, workingData, structureData);
                        if (canDo) {
                            vmContainer.setWorkFlow(maker.getWorkFlow());
                            vmSlotSet = maker.getVmSlotSet();
                        }
                    }
                }
                if (!CWPCraneDomain.CT_SINGLE20.equals(vmContainer.getWorkFlow())) { // 过滤掉20尺的单吊具箱子
                    if (discDoubleWorkMoveMap.get(vmSlot.getVmPosition().getVLocation()) == null) { // VMSlot对应的WorkMove不存在，才生成move；如果存在则不用管
                        WorkMove workMove = createWorkMove(hatchId, dlType, vmSlot, vmContainer, vmSlotSet, vLocationList, workingData, structureData);
                        if (workMove != null) {
                            addWorkMoveToMap(workMove, discDoubleWorkMoveMap);
                        }
                    }
                }
            }
        }
    }

    private void createDoubleWorkMoveMap(Long hatchId, String dlType, List<VMBay> vmBayList, List<String> vLocationList, Map<String, WorkMove> discDoubleWorkMoveMap, WorkingData workingData, StructureData structureData) {
        String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
        List<Integer> rowSeqList;
        for (VMBay vmBay : vmBayList) {
            String key = StringUtil.getKey(StringUtil.getKey(hatchId, vmBay.getAboveOrBelow()), dlType); // 舱、甲板上下、装卸类型（"hatchId@A/B@L/D"）
            CMCraneWorkFlow cmCraneWorkFlow = workingData.getCMCraneWorkFlowByKey(key);
            if (cmCraneWorkFlow.getTandem()) { // 该倍位可以做双吊具，则用双吊具工艺进行处理
                int maxTier = vmBay.getMaxTier();
                int minTier = vmBay.getMinTier();
                for (int tierNo = maxTier; tierNo >= minTier; ) {
                    //根据bayId, 装卸方向，得到该倍位块所有排号
                    rowSeqList = structureData.getRowSeqListByOddOrEven(vmBay.getBayId(), oddOrEven);
                    for (Integer rowNo : rowSeqList) {
                        VMSlot vmSlot = structureData.getVMSlotByVLocation(new VMPosition(vmBay.getBayNo(), rowNo, tierNo).getVLocation());
                        if (vmSlot != null) {
                            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
                            if (vmContainer != null) {
                                Set<VMSlot> vmSlotSet = new HashSet<>();
                                if (PublicMethod.hasNoneWorkFlow(vmContainer.getWorkFlow())) { //没编写作业工艺
                                    List<AbstractMaker> ptSeqList = PublicMethod.getDoublePTSeqListByCMCraneWorkFlow(cmCraneWorkFlow);
                                    for (AbstractMaker maker : ptSeqList) {
                                        boolean canDo = maker.canDo(vmSlot, dlType, workingData, structureData);
                                        if (canDo) {
                                            vmContainer.setWorkFlow(maker.getWorkFlow());
                                            vmSlotSet = maker.getVmSlotSet();
                                        }
                                        if (maker instanceof M20Dual) { // 如果是20尺箱子，双箱吊不能作业，则一定不能4小箱双吊具
                                            if (!canDo && vmContainer.getSize().startsWith("2")) {
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!CWPCraneDomain.CT_SINGLE20.equals(vmContainer.getWorkFlow())) { // 过滤掉20尺的单吊具箱子
                                    if (discDoubleWorkMoveMap.get(vmSlot.getVmPosition().getVLocation()) == null) { // VMSlot对应的WorkMove不存在，才生成move；如果存在则不用管
                                        WorkMove workMove = createWorkMove(hatchId, dlType, vmSlot, vmContainer, vmSlotSet, vLocationList, workingData, structureData);
                                        if (workMove != null) {
                                            addWorkMoveToMap(workMove, discDoubleWorkMoveMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    tierNo -= 2;
                }
            }
        }
    }

    private void clearVMContainerWorkFlow(List<WorkMove> workMoveList, WorkingData workingData) {
        for (WorkMove workMove : workMoveList) {
            for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                if (vmContainer != null && !CWPDomain.YES.equals(vmContainer.getCwoManualWorkflow())) {
                    vmContainer.setWorkFlow(null);
                }
            }
        }
    }

    private WorkMove createWorkMove(Long hatchId, String dlType, VMSlot vmSlot, VMContainer vmContainer, Set<VMSlot> vmSlotSet, List<String> vLocationList, WorkingData workingData, StructureData structureData) {
        String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
        // 如果是人工干预的作业工艺，则需要生成move对应的slot
        if (CWPDomain.YES.equals(vmContainer.getCwoManualWorkflow()) && CWPCraneDomain.CT_QUAD20.equals(vmContainer.getWorkFlow())) {
            VMSlot vmSlotSide = structureData.getSideVMSlot(vmSlot, oddOrEven);
            VMContainer vmContainerSide = workingData.getVMContainerByVMSlot(vmSlotSide, dlType);
            if (vmContainerSide != null && CWPDomain.YES.equals(vmContainerSide.getCwoManualWorkflow()) && CWPCraneDomain.CT_QUAD20.equals(vmContainerSide.getWorkFlow())) {
                vmSlotSet.add(vmSlot);
                vmSlotSet.add(vmSlotSide);
                VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
                VMSlot vmSlotSidePair = structureData.getPairVMSlot(vmSlotSide);
                vmSlotSet.add(vmSlotPair);
                vmSlotSet.add(vmSlotSidePair);
            }
        }
        // 生成Move默认的字段
        WorkMove workMove = new WorkMove(dlType, CWPDomain.MOVE_TYPE_CNT);
        workMove.setHatchId(hatchId);
        workMove.setRowNo(vmSlot.getVmPosition().getRowNo());
        workMove.setTierNo(vmSlot.getVmPosition().getTierNo());
        //move属于哪个档
        Integer hcSeq = workingData.getHcSeqByWorkMove(hatchId, workMove);
        if (hcSeq != null) {
            workMove.setHcSeq(hcSeq);
        } else {
            vLocationList.add(vmContainer.getvLocation());
        }
        workMove.setWorkFirst(vmContainer.getWorkFirst());
        workMove.setWorkFirstOrder(vmContainer.getMoveOrder());
        workMove.setCwoCraneNo(vmContainer.getCwoCraneNoTemp());
        // 根据工艺设置Move属性
        if (PublicMethod.isSingleWorkFlow(vmContainer.getWorkFlow())) {
            long wt = PublicMethod.getCntWorkTime(vmSlot, vmContainer, workingData.getCwpConfig(), dlType);
            if (structureData.isSteppingVMSlot(vmSlot)) {
                wt = wt > workingData.getCwpConfig().getSingle20FootPadTimeD() ? wt : workingData.getCwpConfig().getSingle20FootPadTimeD();
            }
            workMove.setWorkTime(wt);
            workMove.setWorkFlow(vmContainer.getWorkFlow());
            workMove.addVmSlot(vmSlot);
            if (CWPCraneDomain.CT_SINGLE40.equals(vmContainer.getWorkFlow())) {
                VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
                workMove.addVmSlot(vmSlotPair);
            }
            workMove.setBayNo(new VMPosition(vmContainer.getvLocation()).getBayNo());
            return workMove;
        } else if (CWPCraneDomain.CT_DUAL20.equals(vmContainer.getWorkFlow())) {
            VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot); // 对面slot
            if (vmSlotPair != null) {
                VMContainer vmContainerPair = workingData.getVMContainerByVMSlot(vmSlotPair, dlType);
                if (vmContainerPair != null) {
                    vmContainerPair.setWorkFlow(CWPCraneDomain.CT_DUAL20); // 因为后面可能会排4小箱双吊具，所以在判断双箱吊作业工艺时没有对对面的箱子编写工艺
                    long wt = PublicMethod.getCntWorkTime(vmSlotPair, vmContainerPair, workingData.getCwpConfig(), dlType);
                    workMove.setWorkTime(wt);
                    workMove.setWorkFlow(CWPCraneDomain.CT_DUAL20);
                    workMove.addVmSlot(vmSlot);
                    workMove.addVmSlot(vmSlotPair);
                    workMove.setBayNo((vmSlot.getVmPosition().getBayNo() + vmSlotPair.getVmPosition().getBayNo()) / 2);
                    return workMove;
                }
            }
        } else if (CWPCraneDomain.CT_DUAL40.equals(vmContainer.getWorkFlow())) {
            VMSlot vmSlotSide = structureData.getSideVMSlot(vmSlot, oddOrEven);
            VMContainer vmContainerSide = workingData.getVMContainerByVMSlot(vmSlotSide, dlType);
            if (vmContainerSide != null) {
                vmContainerSide.setWorkFlow(CWPCraneDomain.CT_DUAL40);
                long wt = PublicMethod.getCntWorkTime(vmSlotSide, vmContainerSide, workingData.getCwpConfig(), dlType);
                workMove.setWorkTime(wt);
                workMove.setWorkFlow(CWPCraneDomain.CT_DUAL40);
                workMove.addVmSlot(vmSlot);
                workMove.addVmSlot(vmSlotSide);
                VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
                VMSlot vmSlotSidePair = structureData.getPairVMSlot(vmSlotSide);
                workMove.addVmSlot(vmSlotPair);
                workMove.addVmSlot(vmSlotSidePair);
                workMove.setBayNo((vmSlot.getVmPosition().getBayNo() + vmSlotPair.getVmPosition().getBayNo()) / 2);
                return workMove;
            }
        } else if (CWPCraneDomain.CT_QUAD20.equals(vmContainer.getWorkFlow())) {
            if (vmSlotSet.size() == 4) {
                long wt = PublicMethod.getCntWorkTime(vmSlot, vmContainer, workingData.getCwpConfig(), dlType);
                workMove.setWorkTime(wt);
                workMove.setWorkFlow(CWPCraneDomain.CT_QUAD20);
                workMove.getVmSlotSet().addAll(vmSlotSet);
                VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
                workMove.setBayNo((vmSlot.getVmPosition().getBayNo() + vmSlotPair.getVmPosition().getBayNo()) / 2);
                return workMove;
            }
        } else if (CWPCraneDomain.CT_TWIN20.equals(vmContainer.getWorkFlow())) {
            VMSlot vmSlotSide = structureData.getSideVMSlot(vmSlot, oddOrEven);
            VMContainer vmContainerSide = workingData.getVMContainerByVMSlot(vmSlotSide, dlType);
            if (vmContainerSide != null) {
                vmContainerSide.setWorkFlow(CWPCraneDomain.CT_TWIN20);
                long wt = PublicMethod.getCntWorkTime(vmSlotSide, vmContainerSide, workingData.getCwpConfig(), dlType);
                workMove.setWorkTime(wt);
                workMove.setWorkFlow(CWPCraneDomain.CT_TWIN20);
                workMove.addVmSlot(vmSlot);
                workMove.addVmSlot(vmSlotSide);
                workMove.setBayNo(vmSlot.getVmPosition().getBayNo());
                return workMove;
            }
        } else {
            workingData.getLogger().logError("算法无法生成该箱子作业工艺，暂时不能处理的箱子位置(vLocation: " + vmContainer.getvLocation() + ", workflow: " + vmContainer.getWorkFlow() + ")！");
        }
        return null;
    }

    private void apartBelowDiscDoubleWorkMove(Long hatchId, String dlType, Map<String, WorkMove> discDoubleWorkMoveMap, WorkingData workingData, StructureData structureData) {
        // 1、对该舱大倍位、舱下、卸船、双吊具move重新判断处理：如果舱盖板是单吊具，则连续不满足10关的双吊具拆成单吊具；
        VMHatch vmHatch = structureData.getVMHatchByHatchId(hatchId);
        // 得到舱盖板move，判断舱盖板move是否有单吊具
        List<WorkMove> workMoveList = getAllBelowDiscD4Q2MoveListByBayNo(discDoubleWorkMoveMap);
        Set<WorkMove> workMoveSet = new HashSet<>(workMoveList);
        if (workMoveSet.size() < 10) {
            // 先去除discWorkMoveMap中的move
            for (WorkMove workMove : workMoveSet) {
                for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                    discDoubleWorkMoveMap.remove(vmSlot.getVmPosition().getVLocation());
                }
            }
            // 双吊具move拆成单吊具move
            apartDiscDoubleWorkMove(workMoveSet, dlType, discDoubleWorkMoveMap, workingData);
        }
        // 2、分析MoveData中的双吊具move，哪两个箱子组合成双吊具，作业工艺切换总次数最小、切换次数一样时，双吊具关数最多

    }

    private void apartAboveDiscDoubleWorkMove(Long hatchId, String dlType, Map<String, WorkMove> discDoubleWorkMoveMap, WorkingData workingData, StructureData structureData) {
        // 对该舱大倍位、舱上、卸船、双吊具move重新判断处理：如果连续不满足10关的双吊具拆成单吊具；
        // 得到舱盖板move，判断舱盖板move是否有单吊具
        List<WorkMove> workMoveList = getAllAboveDiscD4Q2MoveListByBayNo(discDoubleWorkMoveMap);
        Set<WorkMove> workMoveSet = new HashSet<>(workMoveList);
        if (workMoveSet.size() < 10) {
            // 先去除discWorkMoveMap中的move
            for (WorkMove workMove : workMoveSet) {
                for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                    discDoubleWorkMoveMap.remove(vmSlot.getVmPosition().getVLocation());
                }
            }
            // 双吊具move拆成单吊具move
            apartDiscDoubleWorkMove(workMoveSet, dlType, discDoubleWorkMoveMap, workingData);
        }
    }

    private List<WorkMove> getAllAboveDiscD4Q2MoveListByBayNo(Map<String, WorkMove> discDoubleWorkMoveMap) {
        List<WorkMove> workMoveList = new ArrayList<>();
        for (WorkMove workMove : discDoubleWorkMoveMap.values()) {
            if (workMove.getTierNo() > 50 && (CWPCraneDomain.CT_DUAL40.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_QUAD20.equals(workMove.getWorkFlow()))) {
                workMoveList.add(workMove);
            }
        }
        return workMoveList;
    }

    private List<WorkMove> getAllBelowDiscD4Q2MoveListByBayNo(Map<String, WorkMove> discDoubleWorkMoveMap) {
        List<WorkMove> workMoveList = new ArrayList<>();
        for (WorkMove workMove : discDoubleWorkMoveMap.values()) {
            if (workMove.getTierNo() < 49 && (CWPCraneDomain.CT_DUAL40.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_QUAD20.equals(workMove.getWorkFlow()))) {
                workMoveList.add(workMove);
            }
        }
        return workMoveList;
    }

    private void apartDiscDoubleWorkMove(Set<WorkMove> workMoveSet, String dlType, Map<String, WorkMove> discDoubleWorkMoveMap, WorkingData workingData) {
        for (WorkMove workMove : workMoveSet) {
            Set<VMSlot> vmSlotSet = workMove.getVmSlotSet();
            // 排号相同slot形成一个单吊具的move
            Map<Integer, Set<VMSlot>> vmSlotMap = new HashMap<>();
            for (VMSlot vmSlot : vmSlotSet) {
                if (vmSlotMap.get(vmSlot.getVmPosition().getRowNo()) == null) {
                    vmSlotMap.put(vmSlot.getVmPosition().getRowNo(), new HashSet<VMSlot>());
                }
                vmSlotMap.get(vmSlot.getVmPosition().getRowNo()).add(vmSlot);
            }
            for (Map.Entry<Integer, Set<VMSlot>> entry : vmSlotMap.entrySet()) {
                String workflow = CWPCraneDomain.CT_SINGLE40;
                long wt = workingData.getCwpConfig().getSingle40TimeBD();
                for (VMSlot vmSlot : entry.getValue()) {
                    VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
                    if (vmContainer != null && vmContainer.getSize().startsWith("2")) { // 确定双吊具拆成20尺双箱吊
                        workflow = CWPCraneDomain.CT_DUAL20;
                        wt = workingData.getCwpConfig().getDouble20TimeBD();
                    }
                }
                WorkMove workMove1 = new WorkMove(workMove.getDlType(), CWPDomain.MOVE_TYPE_CNT);
                workMove1.setWorkFlow(workflow);
                workMove1.setWorkTime(wt);
                workMove1.setHatchId(workMove.getHatchId());
                workMove1.setRowNo(entry.getKey());
                workMove1.setVmSlotSet(entry.getValue());
                workMove1.setBayNo(workMove.getBayNo());
                workMove1.setTierNo(workMove.getTierNo());
                workMove1.setHcSeq(workMove.getHcSeq());
                workMove1.setCwoCraneNo(workMove.getCwoCraneNo());
                addWorkMoveToMap(workMove1, discDoubleWorkMoveMap);
            }
        }
    }

    private void addWorkMoveToMap(WorkMove workMove, Map<String, WorkMove> discDoubleWorkMoveMap) {
        for (VMSlot vmSlot : workMove.getVmSlotSet()) {
            if (workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                discDoubleWorkMoveMap.put(vmSlot.getVmPosition().getVLocation(), workMove);
            }
            if (workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                discDoubleWorkMoveMap.put(vmSlot.getVmPosition().getVLocation(), workMove);
            }
        }
    }
}
