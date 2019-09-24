package com.cwp3.single.method;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.crane.CMCrane;
import com.cwp3.model.crane.CMCraneAddOrDelete;
import com.cwp3.model.crane.CMCraneMoveRange;
import com.cwp3.model.crane.CMCranePool;
import com.cwp3.model.log.Logger;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMHatch;
import com.cwp3.model.vessel.VMMachine;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.CraneEfficiency;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.algorithm.move.MoveCalculator;
import com.cwp3.single.data.CwpData;
import com.cwp3.single.data.MoveData;
import com.cwp3.single.data.MoveResults;
import com.cwp3.utils.*;

import java.util.*;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class CwpDataMethod {

    private MoveCalculator moveCalculator;
    private MoveDataMethod moveDataMethod;

    public CwpDataMethod() {
        moveCalculator = new MoveCalculator();
        moveDataMethod = new MoveDataMethod();
    }

    public CwpData initCwpData(Long berthId, AllRuntimeData allRuntimeData) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        StructureData structureData = allRuntimeData.getStructDataByVesselCode(workingData.getVmSchedule().getVesselCode());
        CwpData cwpData = new CwpData(workingData, structureData);
        Logger logger = workingData.getLogger();
        try {
            logger.logInfo("The cwpData of bay(machines) is being initialized.");
            this.initBayInfo(cwpData);
            logger.logInfo("The cwpData of crane is being initialized.");
            this.initCraneInfo(allRuntimeData, cwpData);
            logger.logInfo("The cwpData of add or delete crane info is being initialized.");
            this.initCraneAddOrDelete(cwpData);
            logger.logInfo("The cwpData of startTime is being initialized.");
            this.initCwpDataTime(cwpData);
            logger.logInfo("The cwpData of moveData is being initialized.");
            this.initCwpMove(cwpData, workingData);
        } catch (Exception e) {
            logger.logError("初始化算法数据(CwpData)过程中发生异常！");
            e.printStackTrace();
        }
        return cwpData;
    }

    private void initBayInfo(CwpData cwpData) {
        Logger logger = cwpData.getWorkingData().getLogger();
        //真实舱信息
        List<Long> hatchIds = cwpData.getStructureData().getAllHatchIdList();
        for (Long hatchId : hatchIds) {
            VMHatch vmHatch = cwpData.getStructureData().getVMHatchByHatchId(hatchId);
            try {
                //bay
                List<Integer> bayNos = vmHatch.getAllBayNos();
                for (Integer bayNo : bayNos) {
                    Double bayHatchPo = vmHatch.getVMBayPosition(bayNo);
                    double bayPo = cwpData.getWorkingData().getBayPosition(bayHatchPo);
                    CWPBay cwpBay = new CWPBay(bayNo, hatchId, bayPo);
                    cwpData.addCWPBay(cwpBay);
                }
            } catch (Exception e) {
                logger.logError("计算舱(hatchId:" + hatchId + ")内每个倍位作业位置的方法发生(空指针)异常！");
                e.printStackTrace();
                break;
            }
        }
        //船舶机械模拟成一个舱信息
        List<VMMachine> vmMachineList = cwpData.getStructureData().getAllVMMachineList();
        for (int i = 0; i < vmMachineList.size(); i++) {
            VMMachine vmMachine = vmMachineList.get(i);
            try {
                Integer bayNo = -(i + 1);
                Double bayHatchPo = vmMachine.getMachinePosition();
                double bayPo = cwpData.getWorkingData().getBayPosition(bayHatchPo);
                CWPBay cwpBay = new CWPBay(bayNo, (long) bayNo, bayPo);
                cwpBay.setBayType(CWPDomain.BAY_TYPE_VIRTUAL);
                cwpData.addMachineBay(cwpBay);
            } catch (Exception e) {
                logger.logError("计算船舶器械(machineNo:" + vmMachine.getMachineNo() + ")位置坐标的方法发生(空指针)异常！");
                e.printStackTrace();
                break;
            }
        }
    }

    private void initCraneInfo(AllRuntimeData allRuntimeData, CwpData cwpData) {
        for (CMCranePool cmCranePool : cwpData.getWorkingData().getAllCMCranePools()) {
            CMCrane cmCrane = allRuntimeData.getMachineData().getCMCraneByCraneNo(cmCranePool.getCraneNo());
            cwpData.getWorkingData().getLogger().logError("根据桥机号(craneNo: " + cmCranePool.getCraneNo() + ")找不到相应桥机信息！", ValidatorUtil.isNull(cmCrane));
            CWPCrane cwpCrane = new CWPCrane(cmCranePool.getCraneNo());
            cwpCrane = (CWPCrane) BeanCopyUtil.copyBean(cmCrane, cwpCrane);
            //桥机当前位置
            Integer bayNo = cmCranePool.getFirstWorkBayNo();
            Double position = cwpCrane.getCurrentCranePosition();
            if (CWPDomain.CWP_TYPE_WORK.equals(cwpData.getWorkingData().getCwpType()) && bayNo == null) {
                if (cmCrane.getWorkVesselBay() != null) {
                    bayNo = Integer.valueOf(cmCrane.getWorkVesselBay());
                }
            }
            if (CWPDomain.CWP_TYPE_WORK.equals(cwpData.getWorkingData().getCwpType()) && bayNo != null) {
                position = cwpData.getCWPBayByBayNo(bayNo).getWorkPosition();
            }
            if (bayNo != null) {
                cwpCrane.setFirstWorkBayNo(bayNo);
            }
            cwpCrane.setDpCurrentWorkPosition(position);
            // 桥机物理移动范围，用倍位号限制
            CMCraneMoveRange cmCraneMoveRange = allRuntimeData.getMachineData().getCmCraneMoveRangeByBerthIdAndCraneNo(cwpData.getWorkingData().getVmSchedule().getBerthId(), cmCranePool.getCraneNo());
            if (cmCraneMoveRange != null) {
                if (StringUtil.isNotBlank(cmCraneMoveRange.getStartBayNo())) {
                    cwpCrane.setMoveRangeBayNoFrom(Integer.valueOf(cmCraneMoveRange.getStartBayNo()));
                }
                if (StringUtil.isNotBlank(cmCraneMoveRange.getEndBayNo())) {
                    cwpCrane.setMoveRangeBayNoTo(Integer.valueOf(cmCraneMoveRange.getEndBayNo()));
                }
            }
            cwpData.addCWPCrane(cwpCrane);
        }
        //由配置参数对象控制开路倍位
        String craneAdviceWorkBayNos = cwpData.getWorkingData().getCwpConfig().getCraneAdviceWorkBayNos();
        if (StringUtil.isNotBlank(craneAdviceWorkBayNos)) {
            String[] bayNos = craneAdviceWorkBayNos.split(","); //格式："82,54,30"
            if (bayNos.length == cwpData.getAllCWPCranes().size()) {
                for (int i = 0; i < cwpData.getAllCWPCranes().size(); i++) {
                    CWPCrane cwpCrane = cwpData.getAllCWPCranes().get(i);
                    if (cwpCrane.getFirstWorkBayNo() == null && Integer.valueOf(bayNos[i]) > 0) {
                        cwpCrane.setFirstWorkBayNo(Integer.valueOf(bayNos[i]));
                    }
                }
            } else {
                cwpData.getWorkingData().getLogger().logError("算法配置参数中设置了桥机开路倍位，但是倍位数量(" + bayNos.length + ")与桥机池中的桥机数量(" + cwpData.getAllCWPCranes().size() + ")不匹配！");
            }
        }
    }

    private void initCraneAddOrDelete(CwpData cwpData) {
        try {
            List<CMCraneAddOrDelete> cmCraneAddOrDeleteList = cwpData.getWorkingData().getCmCraneAddOrDeleteList();
            Collections.sort(cmCraneAddOrDeleteList, new Comparator<CMCraneAddOrDelete>() {
                @Override
                public int compare(CMCraneAddOrDelete o1, CMCraneAddOrDelete o2) {
                    return o1.getAddOrDelDate().compareTo(o2.getAddOrDelDate());
                }
            });
            List<CMCranePool> cmCranePoolList = cwpData.getWorkingData().getAllCMCranePools();
            Collections.sort(cmCranePoolList, new Comparator<CMCranePool>() {
                @Override
                public int compare(CMCranePool o1, CMCranePool o2) {
                    return o1.getCraneNo().compareTo(o2.getCraneNo());
                }
            });
            int l = -1, r = -1;
            for (int i = 0; i < cmCranePoolList.size(); i++) {
                if (cmCranePoolList.get(i).getFirstCraneFlag()) {
                    l = i;
                    break;
                }
            }
            for (int i = 0; i < cmCranePoolList.size(); i++) {
                if (cmCranePoolList.get(i).getFirstCraneFlag()) {
                    r = i;
                }
            }
            for (CMCraneAddOrDelete cmCraneAddOrDelete : cmCraneAddOrDeleteList) {
                int leftCraneNum = cmCraneAddOrDelete.getLeftCraneNum();
                int rightCraneNum = cmCraneAddOrDelete.getRightCraneNum();
                if (CWPDomain.ADD_CRANE.equals(cmCraneAddOrDelete.getAddOrDelFlag())) {
                    if (leftCraneNum > 0) {
                        for (int i = 0; i < leftCraneNum; i++) {
                            l = l - 1;
                            CWPCraneWork cwpCraneWork = new CWPCraneWork(cmCranePoolList.get(l).getCraneNo(), CWPDomain.ADD_CRANE, cmCraneAddOrDelete.getAddOrDelDate());
                            cwpData.getCWPCraneByCraneNo(cwpCraneWork.getCraneNo()).getCwpCraneWorkList().add(cwpCraneWork);
                        }
                    }
                    if (rightCraneNum > 0) {
                        for (int i = 0; i < rightCraneNum; i++) {
                            r = r + 1;
                            CWPCraneWork cwpCraneWork = new CWPCraneWork(cmCranePoolList.get(r).getCraneNo(), CWPDomain.ADD_CRANE, cmCraneAddOrDelete.getAddOrDelDate());
                            cwpData.getCWPCraneByCraneNo(cwpCraneWork.getCraneNo()).getCwpCraneWorkList().add(cwpCraneWork);
                        }
                    }
                } else {
                    if (leftCraneNum > 0) {
                        for (int i = 0; i < leftCraneNum; i++) {
                            CWPCraneWork cwpCraneWork = new CWPCraneWork(cmCranePoolList.get(l).getCraneNo(), CWPDomain.DELETE_CRANE, cmCraneAddOrDelete.getAddOrDelDate());
                            cwpData.getCWPCraneByCraneNo(cwpCraneWork.getCraneNo()).getCwpCraneWorkList().add(cwpCraneWork);
                            l = l + 1;
                        }
                    }
                    if (rightCraneNum > 0) {
                        for (int i = 0; i < rightCraneNum; i++) {
                            CWPCraneWork cwpCraneWork = new CWPCraneWork(cmCranePoolList.get(r).getCraneNo(), CWPDomain.DELETE_CRANE, cmCraneAddOrDelete.getAddOrDelDate());
                            cwpData.getCWPCraneByCraneNo(cwpCraneWork.getCraneNo()).getCwpCraneWorkList().add(cwpCraneWork);
                            r = r - 1;
                        }
                    }
                }
            }
        } catch (Exception e1) {
            cwpData.getWorkingData().getLogger().logError("分析加减桥机信息过程中发生异常！请检查第一次使用桥机、加减桥机数目等设置信息！");
            e1.printStackTrace();
        }
    }

    private void initCwpDataTime(CwpData cwpData) {
        long startTime = cwpData.getWorkingData().getVmSchedule().getPlanBeginWorkTime().getTime() / 1000;
        if (cwpData.getWorkingData().getCwpType().equals(CWPDomain.CWP_TYPE_WORK)) {
            startTime = new Date().getTime() / 1000;
//            startTime = DateUtil.getSecondTimeByFormatStr("2019-08-08 09:00:00");
        }
        cwpData.setCwpStartTime(startTime);
        cwpData.setDpCurrentTime(startTime);
    }

    private void initCwpMove(CwpData cwpData, WorkingData workingData) {
        MoveData moveData = moveDataMethod.initMoveData(workingData);
        cwpData.setMoveData(moveData);
    }

    public void computeCurrentWorkTime(DPResult dpResult, CwpData cwpData) {
        if (cwpData.getDpResult().getDpTraceBack().size() == 0) {
            List<Long> hatchIdList = cwpData.getStructureData().getAllHatchIdList();
            for (Long hatchId : hatchIdList) {
                computeWorkTimeByHatchId(hatchId, cwpData);
            }
        } else {
            for (DPPair dpPair : dpResult.getDpTraceBack()) {
                Long hatchId = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond()).getHatchId();
                computeWorkTimeByHatchId(hatchId, cwpData);
            }
        }
    }

    private void computeWorkTimeByHatchId(Long hatchId, CwpData cwpData) {
        // 初始化顶层
        long st = System.currentTimeMillis();
        moveDataMethod.initCurTopWorkMoveByHatchId(hatchId, cwpData.getMoveData(), cwpData.getStructureData(), cwpData.getWorkingData());
        long et = System.currentTimeMillis();
        // 计算总量（先计算）
        long st1 = System.currentTimeMillis();
        moveCalculator.calculateTotalMove(hatchId, cwpData.getMoveData(), cwpData.getMoveResults(), cwpData.getStructureData());
        long et1 = System.currentTimeMillis();
        // 计算可作业量
        long st2 = System.currentTimeMillis();
        moveCalculator.calculateAvailableMove(hatchId, cwpData.getMoveData(), cwpData.getMoveResults(), cwpData.getWorkingData(), cwpData.getStructureData());
        long et2 = System.currentTimeMillis();
//        System.out.println("倍位：" + cwpData.getStructureData().getVMHatchByHatchId(hatchId).getBayNoD() + "，初始化顶层时间：" + (et - st) + "ms" + "，计算总量时间：" + (et1 - st1) + "ms" + "，可作业量时间：" + (et2 - st2) + "ms");
    }

    public CwpData copyCwpData(CwpData cwpData) {
        CwpData cwpDataCopy = new CwpData(cwpData.getWorkingData(), cwpData.getStructureData());
        Logger logger = cwpData.getWorkingData().getLogger().deepCopy();
        cwpDataCopy.getWorkingData().setLogger(logger);
        for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
            cwpDataCopy.addCWPBay(cwpBay.deepCopy());
        }
        for (CWPBay cwpBay : cwpData.getAllMachineBays()) {
            cwpDataCopy.addMachineBay(cwpBay);
        }
        for (CWPCrane cwpCrane : cwpData.getAllCWPCranes()) {
            cwpDataCopy.addCWPCrane(cwpCrane.deepCopy());
        }
        cwpDataCopy.setCwpStartTime(cwpData.getCwpStartTime());
        cwpDataCopy.setMoveData(moveDataMethod.copyMoveData(cwpData.getMoveData()));
        cwpDataCopy.setMoveResults(cwpData.getMoveResults().deepCopy());
        cwpDataCopy.setFirstDoCwp(cwpData.getFirstDoCwp());
        cwpDataCopy.setDpCurrentTime(cwpData.getDpCurrentTime());
        cwpDataCopy.setDpResult(cwpData.getDpResult().deepCopy());
        for (CWPCrane cwpCrane : cwpData.getDpCwpCraneList()) {
            cwpDataCopy.getDpCwpCraneList().add(cwpCrane.deepCopy());
        }
        cwpDataCopy.setDpCraneSelectBays(cwpData.getDpCraneSelectBays());
        cwpDataCopy.setDpStrategyType(cwpData.getDpStrategyType());
        cwpDataCopy.setDpMoveNumber(cwpData.getDpMoveNumber());
        cwpDataCopy.setDpInvalidateBranch(cwpData.getDpInvalidateBranch());
        cwpDataCopy.setDpExceptionBranch(cwpData.getDpExceptionBranch());
        // cwpHatchBayMap、dpFirstCwpCraneMap、evaluateTime只是中间变量，不需要复制
        return cwpDataCopy;
    }

    public MoveData copyMoveData(MoveData moveData) {
        return moveDataMethod.copyMoveData(moveData);
    }

    public long computeUnLockTime(CWPBay cwpBay, CwpData cwpData) {
        List<WorkMove> workMoveList = getFirstWorkMoveList(cwpBay, cwpData.getMoveResults());
        int count = 0;
        for (WorkMove workMove : workMoveList) {
            if (workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
                if (cwpData.getStructureData().isLockVMSot(workMove.getOneVMSlot())) {
                    count += 1;
                }
            }
        }
        return count * cwpData.getWorkingData().getCwpConfig().getUnlockTwistTime();
    }

    public long doProcessOrder(CWPCrane cwpCrane, CWPBay cwpBay, String selectReason, CwpData cwpData) {
        long wt = 0;
        Long craneStartTime = cwpCrane.getDpCurrentTime();
        long realWorkTime = cwpCrane.getDpEndWorkTime() - craneStartTime;

        List<WorkMove> workMoveList = getFirstWorkMoveList(cwpBay, cwpData.getMoveResults());

        // 如果桥机不支持双吊具，则需要将双吊具的move拆成单吊具，拆多少要看realWorkTime的大小
        List<WorkMove> apartWorkMoveList = new ArrayList<>();
        if (!"Y".equals(cwpCrane.getTandemFlag())) {
            List<WorkMove> newWorkMoveList = new ArrayList<>();
            long allWt = 0;
            for (WorkMove workMove : workMoveList) {
                if (CWPCraneDomain.CT_DUAL40.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_QUAD20.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_TWIN20.equals(workMove.getWorkFlow())) {
                    // 排号rowNo相同slot形成一个单吊具的move
                    Map<Integer, Set<VMSlot>> rowNoVMSlotMap = new HashMap<>();
                    for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                        if (rowNoVMSlotMap.get(vmSlot.getVmPosition().getRowNo()) == null) {
                            rowNoVMSlotMap.put(vmSlot.getVmPosition().getRowNo(), new HashSet<VMSlot>());
                        }
                        rowNoVMSlotMap.get(vmSlot.getVmPosition().getRowNo()).add(vmSlot);
                    }
                    // todo: rowNo需要一个顺序
                    List<Integer> rowNoList = new ArrayList<>(rowNoVMSlotMap.keySet());
                    String oddOrEven = cwpData.getWorkingData().getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_LAND_SEA);
                    final List<Integer> rowSeqList = cwpData.getStructureData().getRowSeqListBySeaOrLand(cwpBay.getHatchId(), oddOrEven);
                    Collections.sort(rowNoList, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            return Integer.compare(rowSeqList.indexOf(o1), rowSeqList.indexOf(o2));
                        }
                    });
                    for (Integer rowNo : rowNoList) {
                        Set<VMSlot> vmSlotSet = rowNoVMSlotMap.get(rowNo);
                        String workflow = CWPCraneDomain.CT_SINGLE40;
                        long wt1 = cwpData.getWorkingData().getCwpConfig().getSingle40TimeBD();
                        if (vmSlotSet.size() == 1) { // T2
                            workflow = CWPCraneDomain.CT_SINGLE20;
                            wt1 = cwpData.getWorkingData().getCwpConfig().getSingle20FootPadTimeD();
                        } else { // D4、Q2
                            for (VMSlot vmSlot : vmSlotSet) {
                                VMContainer vmContainer = cwpData.getWorkingData().getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                                if (vmContainer != null && vmContainer.getSize().startsWith("2")) { // 确定双吊具拆成20尺双箱吊
                                    workflow = CWPCraneDomain.CT_DUAL20;
                                    wt1 = cwpData.getWorkingData().getCwpConfig().getDouble20TimeBD();
                                }
                            }
                        }
                        WorkMove workMove1 = new WorkMove(workMove.getDlType(), CWPDomain.MOVE_TYPE_CNT);
                        workMove1.setWorkFlow(workflow);
                        workMove1.setWorkTime(wt1);
                        workMove1.setHatchId(workMove.getHatchId());
                        workMove1.setRowNo(rowNo);
                        workMove1.setVmSlotSet(vmSlotSet);
                        workMove1.setBayNo(workMove.getBayNo());
                        workMove1.setTierNo(workMove.getTierNo());
                        workMove1.setHcSeq(workMove.getHcSeq());
                        workMove1.setMoveOrder(workMove.getMoveOrder());
                        apartWorkMoveList.add(workMove1);
                        newWorkMoveList.add(workMove1);
                        allWt += wt1;
                    }
                } else { // 非双吊具工艺
                    newWorkMoveList.add(workMove);
                    allWt += workMove.getWorkTime();
                }
                if (allWt >= realWorkTime) {
                    break;
                }
            }
            workMoveList = newWorkMoveList;
        }
        // 根据拆分开来的move，查找到对应MoveData中的move，进行相应的拆分
        if (apartWorkMoveList.size() > 0) {
            for (WorkMove workMove : apartWorkMoveList) {
                // 从卸船的discWorkMoveMap中去掉双吊具的move
                WorkMove workMove1 = cwpData.getMoveData().getWorkMoveByVMSlot(workMove.getOneVMSlot(), workMove.getDlType());
                if (workMove1 != null && (CWPCraneDomain.CT_DUAL40.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_QUAD20.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_TWIN20.equals(workMove.getWorkFlow()))) {
                    cwpData.getMoveData().getDiscWorkMoveMap().remove(workMove.getOneVMSlot().getVmPosition().getVLocation());
                }
                WorkMove workMoveFirstCopy = workMove.baseCopy();
                workMoveFirstCopy.setMoveOrder(null);
                for (VMSlot vmSlot : workMoveFirstCopy.getVmSlotSet()) {
                    cwpData.getMoveData().getDiscWorkMoveMap().put(vmSlot.getVmPosition().getVLocation(), workMoveFirstCopy);
                }
            }
        }

        long allWt = 0, moreCost = 0;
        for (WorkMove workMove : workMoveList) {
            allWt += workMove.getWorkTime();
        }
        if (allWt < realWorkTime) { // 倍位剩余作业时长小于桥机该次作业时间，则需要同比例延长
            moreCost = realWorkTime / workMoveList.size(); // todo: 按作业工艺统计，区分不同工艺的作业时长
        }

        if (workMoveList.size() > 0) {
            long firstOrder = workMoveList.get(0).getMoveOrder();
            for (WorkMove workMove : workMoveList) {
                WorkMove workMove1 = cwpData.getMoveData().getWorkMoveByVMSlot(workMove.getOneVMSlot(), workMove.getDlType());
                if (wt < realWorkTime) {
                    long curCost = workMove1.getWorkTime();
                    curCost = moreCost > curCost ? moreCost : curCost;
                    wt += curCost;
                    if (wt > realWorkTime) { //一般只会发生在最后一关箱子上，让最后一关箱子少做几十秒
                        curCost = curCost - (wt - realWorkTime);
                    }
                    workMove1.setPlanStartTime(new Date(craneStartTime * 1000));
                    workMove1.setPlanEndTime(new Date((craneStartTime + curCost) * 1000));
                    craneStartTime += curCost;
                    workMove1.setCraneNo(cwpCrane.getCraneNo());
                    workMove1.setWorkPosition(cwpBay.getWorkPosition());
                    workMove1.setSelectReason(selectReason);
                    workMove1.setMoveOrder(firstOrder++);
                    cwpData.getMoveData().setCurMoveOrder(cwpBay.getHatchId(), firstOrder);
                    cwpData.getMoveData().setCurWorkFlow(cwpBay.getHatchId(), workMove.getWorkFlow());
                    cwpData.getMoveData().setCurWorkFlow1(cwpBay.getBayNo(), workMove.getWorkFlow());
                    if (CWPDomain.MOVE_TYPE_CNT.equals(workMove1.getMoveType())) {
                        cwpCrane.setDpWorkCntAmount(cwpCrane.getDpWorkCntAmount() + 1);
                    }
                } else { // 超过最小作业时间
                    break;
                }
            }
        }
        return realWorkTime;
    }

    private List<WorkMove> getFirstWorkMoveList(CWPBay cwpBay, MoveResults moveResults) {
        List<WorkMove> workMoveList = new ArrayList<>();
        Map<Integer, List<WorkMove>> availableWorkMoveMap = moveResults.getAvailableWorkMoveMapByBayNo(cwpBay.getBayNo());
        for (Map.Entry<Integer, List<WorkMove>> entry : availableWorkMoveMap.entrySet()) {
            workMoveList.addAll(entry.getValue());
        }
        PublicMethod.sortWorkMoveListByMoveOrder(workMoveList);
        return workMoveList;
    }

    public void generateResult(List<CwpData> cwpDataResultList) {
        for (int i = 0; i < cwpDataResultList.size(); i++) {
            String key = String.valueOf(i);
            CwpData cwpData = cwpDataResultList.get(i);
            Set<WorkMove> workMoveSet = new HashSet<>();
            workMoveSet.addAll(cwpData.getMoveData().getDiscWorkMoveMap().values());
            workMoveSet.addAll(cwpData.getMoveData().getLoadWorkMoveMap().values());
            cwpData.getWorkingData().getWorkMoveMap().put(key, new ArrayList<>(workMoveSet));
            List<WorkBlock> workBlockList = generateWorkBlock(cwpData.getWorkingData().getWorkMoveMap().get(key));
            cwpData.getWorkingData().getWorkBlockMap().put(key, workBlockList);
            List<CraneEfficiency> craneEfficiencyList = generateCraneEfficiency(cwpData.getWorkingData().getWorkMoveMap().get(key), cwpData);
            cwpData.getWorkingData().getCraneEfficiencyMap().put(key, craneEfficiencyList);
        }
    }

    private List<WorkBlock> generateWorkBlock(List<WorkMove> workMoveList) {
        long blockId = 1;
        List<WorkBlock> workBlockList = new ArrayList<>();
        Map<String, List<WorkMove>> allMap = new HashMap<>();
        for (WorkMove workMove : workMoveList) {
            if (workMove.getWorkPosition() != null) { //有作业位置
                String cKey = StringUtil.getKey(workMove.getBayNo(), workMove.getCraneNo());     //倍位号.桥机号
                //将数据按key为倍位号.桥机号，保存在Map里，其中value为是以开始时间为key的Map，目的是为了后面以时间顺序组成大作业块
                if (allMap.get(cKey) == null) {
                    allMap.put(cKey, new ArrayList<WorkMove>());
                }
                allMap.get(cKey).add(workMove);
            }
        }
        for (Map.Entry<String, List<WorkMove>> entry : allMap.entrySet()) {
            PublicMethod.sortWorkMoveListByPlanStartTime(entry.getValue());
            WorkMove workMoveLast = new WorkMove();
            for (int i = 0; i < entry.getValue().size(); i++) {
                WorkMove workMoveCur = entry.getValue().get(i);
                workMoveCur.setBlockId(blockId);
                if (workMoveLast.getDlType() == null) {
                    copyWorkMove(workMoveCur, workMoveLast);
                } else {
                    long cur_last_time = workMoveCur.getPlanStartTime().getTime() / 1000 - workMoveLast.getPlanEndTime().getTime() / 1000;
                    if (cur_last_time == 0 && workMoveCur.getMoveType().equals(workMoveLast.getMoveType())) {
                        workMoveLast.setPlanEndTime(workMoveCur.getPlanEndTime());
                        workMoveLast.setMoveOrder(workMoveLast.getMoveOrder() + 1);
                    } else { //不是连续时间片
                        WorkBlock workBlock = new WorkBlock();
                        createWorkBlock(workBlock, workMoveLast);
                        blockId++;
                        workBlockList.add(workBlock);
                        copyWorkMove(workMoveCur, workMoveLast);
                    }
                }
            }
            if (workMoveLast.getDlType() != null) {
                WorkBlock workBlock = new WorkBlock();
                createWorkBlock(workBlock, workMoveLast);
                blockId++;
                workBlockList.add(workBlock);
            }
        }
        return workBlockList;
    }

    private void copyWorkMove(WorkMove workMoveCur, WorkMove workMoveLast) {
        workMoveLast.setDlType(workMoveCur.getDlType());
        workMoveLast.setCraneNo(workMoveCur.getCraneNo());
        workMoveLast.setBayNo(workMoveCur.getBayNo());
        workMoveLast.setMoveOrder(1L);
        workMoveLast.setMoveType(workMoveCur.getMoveType());
        workMoveLast.setHatchId(workMoveCur.getHatchId());
        workMoveLast.setWorkPosition(workMoveCur.getWorkPosition());
        workMoveLast.setPlanStartTime(workMoveCur.getPlanStartTime());
        workMoveLast.setPlanEndTime(workMoveCur.getPlanEndTime());
        workMoveLast.setSelectReason(workMoveCur.getSelectReason());
        workMoveLast.setBlockId(workMoveCur.getBlockId());
    }

    private void createWorkBlock(WorkBlock workBlock, WorkMove workMove) {
        workBlock.setCraneNo(workMove.getCraneNo());
        workBlock.setBayNo(String.format("%02d", workMove.getBayNo()));
        workBlock.setHatchId(workMove.getHatchId());
        workBlock.setPlanAmount(workMove.getMoveOrder());
        workBlock.setCranePosition(workMove.getWorkPosition());
        workBlock.setLduldfg(workMove.getDlType());
        workBlock.setWorkingStartTime(workMove.getPlanStartTime());
        workBlock.setWorkingEndTime(workMove.getPlanEndTime());
        workBlock.setSelectReason(workMove.getSelectReason());
        workBlock.setBlockType(workMove.getMoveType());
        workBlock.setBlockId(workMove.getBlockId());
    }

    private List<CraneEfficiency> generateCraneEfficiency(List<WorkMove> workMoveList, CwpData cwpData) {
        List<CraneEfficiency> craneEfficiencyList = new ArrayList<>();
        Map<String, List<WorkMove>> workMoveMap = new HashMap<>();
        for (WorkMove workMove : workMoveList) {
            if (workMove.getCraneNo() != null) {
                if (workMoveMap.get(workMove.getCraneNo()) == null) {
                    workMoveMap.put(workMove.getCraneNo(), new ArrayList<WorkMove>());
                }
                workMoveMap.get(workMove.getCraneNo()).add(workMove);
            }
        }
        for (Map.Entry<String, List<WorkMove>> entry : workMoveMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                Collections.sort(entry.getValue(), new Comparator<WorkMove>() {
                    @Override
                    public int compare(WorkMove o1, WorkMove o2) {
                        return o1.getPlanStartTime().compareTo(o2.getPlanStartTime());
                    }
                });
                CraneEfficiency craneEfficiencyL = new CraneEfficiency();
                craneEfficiencyL.setCraneNo(entry.getKey());
                craneEfficiencyL.setDlType(CWPDomain.DL_TYPE_LOAD);
                createCraneEfficiency(entry.getValue(), craneEfficiencyL, cwpData);
                CraneEfficiency craneEfficiencyD = new CraneEfficiency();
                craneEfficiencyD.setCraneNo(entry.getKey());
                craneEfficiencyD.setDlType(CWPDomain.DL_TYPE_DISC);
                createCraneEfficiency(entry.getValue(), craneEfficiencyD, cwpData);
                craneEfficiencyList.add(craneEfficiencyL);
                craneEfficiencyList.add(craneEfficiencyD);
            }
        }
        return craneEfficiencyList;
    }

    private void createCraneEfficiency(List<WorkMove> workMoveList, CraneEfficiency craneEfficiency, CwpData cwpData) {
        craneEfficiency.setBerthId(cwpData.getWorkingData().getVmSchedule().getBerthId());
        for (WorkMove workMove : workMoveList) {
            if (craneEfficiency.getDlType().equals(workMove.getDlType())) {
                if (workMove.getMoveType().equals(CWPDomain.MOVE_TYPE_CNT)) {
                    Set<VMContainer> vmContainerSet = new HashSet<>();
                    for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                        VMContainer vmContainer = cwpData.getWorkingData().getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                        vmContainerSet.add(vmContainer);
                    }
                    craneEfficiency.addPlanWorkTime(workMove.getWorkTime().doubleValue());
                    for (VMContainer vmContainer : vmContainerSet) {
                        craneEfficiency.addPlanWorkCntNumber(1);
                        if (StringUtil.isNotBlank(vmContainer.getDgCd()) && !CWPDomain.NO.equals(vmContainer.getDgCd())) {
                            craneEfficiency.addDangerCntNumber(1);
                        }
                        if (CWPDomain.YES.equals(vmContainer.getOverrunCd())) {
                            craneEfficiency.addOverLimitCntNumber(1);
                        }
                    }
                }
            }
        }
        if (craneEfficiency.getPlanWorkTime() > 0) {
            craneEfficiency.setPlanWorkTime(CalculateUtil.div(craneEfficiency.getPlanWorkTime(), 3600, 2));
            craneEfficiency.setPlanWorkEfficiency(CalculateUtil.div(craneEfficiency.getPlanWorkCntNumber().doubleValue(), craneEfficiency.getPlanWorkTime(), 2));
        }
        if (craneEfficiency.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
            craneEfficiency.setDiscCntNumber(craneEfficiency.getPlanWorkCntNumber());
        }
        if (craneEfficiency.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
            craneEfficiency.setLoadCntNumber(craneEfficiency.getPlanWorkCntNumber());
        }
    }

}
