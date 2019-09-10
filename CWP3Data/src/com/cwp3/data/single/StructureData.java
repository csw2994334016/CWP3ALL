package com.cwp3.data.single;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.*;
import com.cwp3.utils.CalculateUtil;
import com.cwp3.utils.StringUtil;

import java.util.*;


public class StructureData {

    private String vesselCode; //船舶代码，是船舶结构信息的主键

    //船舶结构信息
    private Map<Long, VMHatch> vmHatchMap; //<hatchId, VMHatch>
    private Map<String, VMBay> vmBayKeyMap; //<bayKey, VMBay>
    private Map<Long, VMBay> vmBayIdMap;//<bayId, VMBay>
    private Map<String, VMSlot> vmSlotMap; //记录vLocation船箱位与VMSlot的关系

    private Map<String, VMHatchCover> vmHatchCoverMap; //<vLocation, VMHatchCover>
    private Map<String, VMMachine> vmMachineMap; //船舶机械信息：<machineNo, VMMachine>

    public StructureData(String vesselCode) {
        this.vesselCode = vesselCode;
        vmHatchMap = new HashMap<>();
        vmBayKeyMap = new HashMap<>();
        vmBayIdMap = new HashMap<>();
        vmSlotMap = new HashMap<>();
        vmHatchCoverMap = new HashMap<>();
        vmMachineMap = new HashMap<>();
    }

    public String getVesselCode() {
        return vesselCode;
    }

    public void addVMHatch(VMHatch vmHatch) {
        this.vmHatchMap.put(vmHatch.getHatchId(), vmHatch);
    }

    public VMHatch getVMHatchByHatchId(Long hatchId) {
        return this.vmHatchMap.get(hatchId);
    }

    public List<VMHatch> getAllVMHatchs() {
        List<VMHatch> vmHatchList = new ArrayList<>(vmHatchMap.values());
        Collections.sort(vmHatchList, new Comparator<VMHatch>() {
            @Override
            public int compare(VMHatch o1, VMHatch o2) {
                return o1.getHatchId().compareTo(o2.getHatchId());
            }
        });
        return vmHatchList;
    }

    public List<Long> getAllHatchIdList() {
        List<Long> hatchIdList = new ArrayList<>(vmHatchMap.keySet());
        Collections.sort(hatchIdList);
        return hatchIdList;
    }

    public void addVMBay(VMBay vmBay) {
        this.vmBayKeyMap.put(vmBay.getBayKey(), vmBay);
        this.vmBayIdMap.put(vmBay.getBayId(), vmBay);
    }

    public VMBay getVMBayByBayKey(String BayKey) {
        return this.vmBayKeyMap.get(BayKey);
    }

    public VMBay getVMBayByBayId(Long bayId) {
        return this.vmBayIdMap.get(bayId);
    }

    public void addVMSlot(VMSlot vmSlot) {
        this.vmSlotMap.put(vmSlot.getVmPosition().getVLocation(), vmSlot);
    }

    public VMSlot getVMSlotByVLocation(String vLocation) {
        return this.vmSlotMap.get(vLocation);
    }

    public void addVMHatchCover(VMHatchCover vmHatchCover) {
        this.vmHatchCoverMap.put(vmHatchCover.getvLocation(), vmHatchCover);
    }

    public void addVMMachine(VMMachine vmMachine) {
        this.vmMachineMap.put(vmMachine.getMachineNo(), vmMachine);
    }

    public VMMachine getVMMachineByMachineNo(String machineNo) {
        return this.vmMachineMap.get(machineNo);
    }

    public List<VMMachine> getAllVMMachineList() {
        return new ArrayList<>(vmMachineMap.values());
    }

    public List<VMBay> getAllVMBayList() {
        return new ArrayList<>(vmBayIdMap.values());
    }

    public List<VMHatchCover> getHatchCoverListByHatchId(Long hatchId) {
        List<VMHatchCover> vmHatchCoverList = new ArrayList<>();
        for (VMHatchCover vmHatchCover : vmHatchCoverMap.values()) {
            if (vmHatchCover.getHatchId().equals(hatchId)) {
                vmHatchCoverList.add(vmHatchCover);
            }
        }
        return vmHatchCoverList;
    }

    public List<VMBay> getVMBayListByHatchId(Long hatchId) {
        List<VMBay> vmBayList = new ArrayList<>();
        for (VMBay vmBay : vmBayIdMap.values()) {
            if (vmBay.getHatchId().equals(hatchId))
                vmBayList.add(vmBay);
        }
        return vmBayList;
    }

    public List<Integer> getRowSeqListByOddOrEven(Long bayId, String oddOrEven) {
        VMBay vmBay = this.getVMBayByBayId(bayId);
        List<Integer> rowSeqListAsc = vmBay.getRowNoList();
        Collections.sort(rowSeqListAsc);
        LinkedList<Integer> rowSeqListEO = new LinkedList<>();
        for (int rowNo : rowSeqListAsc) {
            if (rowNo % 2 == 0) { //偶数排号，添加到前面
                rowSeqListEO.addFirst(rowNo);
            } else {
                rowSeqListEO.addLast(rowNo);
            }
        }
        if (oddOrEven.equals(CWPDomain.ROW_SEQ_EVEN_ODD)) {
            return rowSeqListEO;
        } else {
            Collections.reverse(rowSeqListEO);
            return rowSeqListEO;
        }
    }

    public List<Integer> getRowSeqListBySeaOrLand(Long hatchId, String board, String oddOrEven) {
        LinkedList<Integer> rowNoList = new LinkedList<>();
        List<VMBay> vmBayList = this.getVMBayListByHatchId(hatchId);
        //取出所有bay所有Row
        Set<Integer> rowNoSet = new HashSet<>();
        for (VMBay vmBay : vmBayList) {
            if (StringUtil.getSecondKey(vmBay.getBayKey()).equals(board)) {
                rowNoSet.addAll(vmBay.getRowNoList());
            }
        }
        List<Integer> rowNoListAsc = new ArrayList<>(rowNoSet);
        Collections.sort(rowNoListAsc);
        for (int rowNo : rowNoListAsc) {
            if (rowNo % 2 == 0) {//偶数排号，添加到左边（前面）
                rowNoList.addFirst(rowNo);
            } else {
                rowNoList.addLast(rowNo);
            }
        }
        if (oddOrEven.equals(CWPDomain.ROW_SEQ_EVEN_ODD)) {
            return rowNoList;
        } else {
            Collections.reverse(rowNoList);
            return rowNoList;
        }
    }

    public List<Integer> getRowSeqListBySeaOrLand(Long hatchId, String oddOrEven) {
        LinkedList<Integer> rowNoList = new LinkedList<>();
        List<VMBay> vmBayList = this.getVMBayListByHatchId(hatchId);
        //取出所有bay所有Row
        Set<Integer> rowNoSet = new HashSet<>();
        for (VMBay vmBay : vmBayList) {
            rowNoSet.addAll(vmBay.getRowNoList());
        }
        List<Integer> rowNoListAsc = new ArrayList<>(rowNoSet);
        Collections.sort(rowNoListAsc);
        for (int rowNo : rowNoListAsc) {
            if (rowNo % 2 == 0) {//偶数排号，添加到左边（前面）
                rowNoList.addFirst(rowNo);
            } else {
                rowNoList.addLast(rowNo);
            }
        }
        if (oddOrEven.equals(CWPDomain.ROW_SEQ_EVEN_ODD)) {
            return rowNoList;
        } else {
            Collections.reverse(rowNoList);
            return rowNoList;
        }
    }

    public int getMaxTierNoByHatchId(Long hatchId) {
        int maxTierNo = -1;
        List<VMBay> vmBayList = this.getVMBayListByHatchId(hatchId);
        for (VMBay vmBay : vmBayList) {
            maxTierNo = vmBay.getMaxTier() > maxTierNo ? vmBay.getMaxTier() : maxTierNo;
        }
        return maxTierNo;
    }

    public VMSlot getPairVMSlot(VMSlot vmSlot) {
        VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
        VMHatch vmHatch = this.getVMHatchByHatchId(vmContainerSlot.getVmBay().getHatchId());
        Integer bayNoPair = vmHatch.getPairBayNo(vmSlot.getVmPosition().getBayNo());
        if (bayNoPair != null) {
            VMPosition vmPosition = new VMPosition(bayNoPair, vmSlot.getVmPosition().getRowNo(), vmSlot.getVmPosition().getTierNo());
            return this.getVMSlotByVLocation(vmPosition.getVLocation());
        } else {
            return null;
        }
    }

    public VMSlot getBelowVMSlot(VMSlot vmSlot) {
        VMPosition vmPosition = new VMPosition(vmSlot.getVmPosition().getBayNo(), vmSlot.getVmPosition().getRowNo(), vmSlot.getVmPosition().getTierNo() - 2);
        return this.getVMSlotByVLocation(vmPosition.getVLocation());
    }

    public VMSlot getAboveVMSlot(VMSlot vmSlot) {
        VMPosition vmPosition = new VMPosition(vmSlot.getVmPosition().getBayNo(), vmSlot.getVmPosition().getRowNo(), vmSlot.getVmPosition().getTierNo() + 2);
        return this.getVMSlotByVLocation(vmPosition.getVLocation());
    }

    public VMSlot getSideVMSlot(VMSlot vmSlot, String oddOrEven) {
        if (vmSlot != null) {
            VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
            List<Integer> rowNoSeqList = this.getRowSeqListByOddOrEven(vmContainerSlot.getVmBay().getBayId(), oddOrEven);
            for (int i = 0; i < rowNoSeqList.size() - 1; i++) {
                if (vmContainerSlot.getVmPosition().getRowNo().equals(rowNoSeqList.get(i))) {
                    VMPosition vmPosition = new VMPosition(vmContainerSlot.getVmPosition().getBayNo(), rowNoSeqList.get(i + 1), vmContainerSlot.getVmPosition().getTierNo());
                    return this.getVMSlotByVLocation(vmPosition.getVLocation());
                }
            }
        }
        return null;
    }

    public boolean hasNextVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null) {
            if (CWPDomain.DL_TYPE_LOAD.equals(dlType)) {
                VMBay vmBayA = this.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_ABOVE));
                int top;
                if (vmBayA.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null) {
                    top = vmBayA.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getTopTierNo();
                } else {
                    top = 50;
                }
                return vmSlot.getVmPosition().getTierNo() + 2 <= top;
//                return vmSlot.getVmPosition().getTierNo() + 2 <= vmBayA.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getTopTierNo();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean hasFrontVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null) {
            if (CWPDomain.DL_TYPE_DISC.equals(dlType)) {
                VMBay vmBayA = this.getVMBayByBayKey(StringUtil.getKey(vmSlot.getVmPosition().getBayNo(), CWPDomain.BOARD_ABOVE));
                int top;
                if (vmBayA.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()) != null) {
                    top = vmBayA.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getTopTierNo();
                } else {
                    top = 50;
                }
                return vmSlot.getVmPosition().getTierNo() + 2 <= top;
//                return vmSlot.getVmPosition().getTierNo() + 2 <= vmBayA.getVMRowByRowNo(vmSlot.getVmPosition().getRowNo()).getTopTierNo();
            }
            return true;
        } else {
            return false;
        }
    }

    public VMSlot getFrontVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null) {
            int n = CWPDomain.DL_TYPE_DISC.equals(dlType) ? 2 : -2;
            VMPosition vmPosition = vmSlot.getVmPosition();
            VMBay vmBayA = this.getVMBayByBayKey(StringUtil.getKey(vmPosition.getBayNo(), CWPDomain.BOARD_ABOVE));
            int nextTierNo = vmPosition.getTierNo() + n;
            VMSlot vmSlot1 = getVmSlot(n, vmPosition, vmBayA, nextTierNo);
            if (vmSlot1 != null) {
                return vmSlot1;
            }
        }
        return null;
    }

    public VMSlot getNextVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null) {
            int n = CWPDomain.DL_TYPE_DISC.equals(dlType) ? -2 : 2;
            VMPosition vmPosition = vmSlot.getVmPosition();
            VMBay vmBayA = this.getVMBayByBayKey(StringUtil.getKey(vmPosition.getBayNo(), CWPDomain.BOARD_ABOVE));
            int nextTierNo = vmPosition.getTierNo() + n;
            VMSlot vmSlot1 = getVmSlot(n, vmPosition, vmBayA, nextTierNo);
            if (vmSlot1 != null) {
                return vmSlot1;
            }
        }
        return null;
    }

    private VMSlot getVmSlot(int n, VMPosition vmPosition, VMBay vmBayA, int nextTierNo) {
        int top;
        if (vmBayA.getVMRowByRowNo(vmPosition.getRowNo()) != null) {
            top = vmBayA.getVMRowByRowNo(vmPosition.getRowNo()).getTopTierNo();
        } else {
            top = 50;
        }
        while (nextTierNo <= top) {
            VMPosition vmPosition1 = new VMPosition(vmPosition.getBayNo(), vmPosition.getRowNo(), nextTierNo);
            VMSlot vmSlot1 = this.getVMSlotByVLocation(vmPosition1.getVLocation());
            if (vmSlot1 != null) {
                return vmSlot1;
            }
            nextTierNo = nextTierNo + n;
        }
        return null;
    }

    public VMHatch getLeftVMHatch(Long hatchId) {
        List<VMHatch> vmHatchList = this.getAllVMHatchs();
        for (int j = 0; j < vmHatchList.size(); j++) {
            if (hatchId.equals(vmHatchList.get(j).getHatchId())) {
                if (j - 1 >= 0) {
                    return vmHatchList.get(j - 1);
                }
            }
        }
        return null;
    }

    public VMHatch getRightVMHatch(Long hatchId) {
        List<VMHatch> vmHatchList = this.getAllVMHatchs();
        for (int j = 0; j < vmHatchList.size(); j++) {
            if (hatchId.equals(vmHatchList.get(j).getHatchId())) {
                if (j + 1 <= vmHatchList.size() - 1) {
                    return vmHatchList.get(j + 1);
                }
            }
        }
        return null;
    }

    public boolean isSteppingVMSlot(VMSlot vmSlot) {
        if (vmSlot instanceof VMContainerSlot) {
            VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
            if (CWPDomain.BOARD_BELOW.equals(vmContainerSlot.getVmBay().getAboveOrBelow())) { //甲板下的才是垫脚
                return vmContainerSlot.getSize().equals("1");
            }
        }
        return false;
    }

    public boolean isLockVMSot(VMSlot vmSlot) {
        if (vmSlot instanceof VMContainerSlot) {
            VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
            if (CWPDomain.BOARD_ABOVE.equals(vmContainerSlot.getVmBay().getAboveOrBelow())) {
                VMRow vmRow = vmContainerSlot.getVmBay().getVMRowByRowNo(vmContainerSlot.getVmPosition().getRowNo());
                return (vmContainerSlot.getVmPosition().getTierNo() - vmRow.getBottomTierNo()) / 2 > 3;
            }
        }
        return false;
    }

    public double getVMHatchSpan() {
        List<VMHatch> vmHatchList = getAllVMHatchs();
        if (vmHatchList.size() > 2) {
            return Math.abs(CalculateUtil.sub(vmHatchList.get(2).getHatchPosition(), vmHatchList.get(1).getHatchPosition()));
        } else {
            return vmHatchList.get(0).getHatchLength();
        }
    }

}
