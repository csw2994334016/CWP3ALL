package com.cwp3.single.algorithm.move.maker;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMContainerSlot;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.utils.CalculateUtil;
import com.cwp3.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by csw on 2018/5/30.
 * Description: 20尺单倍双吊具:绿色20、40；紫色20、40、45；红色：20
 */
public class M20Twin extends AbstractMaker {

    @Override
    public boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData) {
        boolean canDo = false;
        String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
        VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
        if (CWPDomain.SEPARATED_SLOT.equals(vmContainerSlot.getSize())) {
            VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
            // 对面有slot且是SEPARATED_SLOT，才能算是全隔槽；或者该舱只有一个小倍位
            boolean separatedSlot = false;
            if (vmSlotPair != null) {
                VMContainerSlot vmContainerPairSlot = (VMContainerSlot) vmSlotPair;
                if (CWPDomain.SEPARATED_SLOT.equals(vmContainerPairSlot.getSize())) {
                    separatedSlot = true;
                }
            }
            if (vmSlotPair == null && structureData.getVMHatchByHatchId(vmContainerSlot.getVmBay().getHatchId()).getBayNos().size() == 1) {
                separatedSlot = true;
            }
            if (separatedSlot) {
                VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
                if (vmContainer != null && vmContainer.getSize().startsWith(getSize())) {
                    VMSlot vmSlotSide = structureData.getSideVMSlot(vmSlot, oddOrEven);
                    if (vmSlotSide != null) {
                        VMContainer vmContainerSide = workingData.getVMContainerByVMSlot(vmSlotSide, dlType);
                        if (vmContainerSide != null && PublicMethod.hasNoneWorkFlow(vmContainerSide.getWorkFlow())) {
                            if (compareTwoContainer(vmSlot, vmSlotSide, vmContainer, vmContainerSide, dlType, workingData, structureData)) {
                                canDo = true;
                            }
                        }
                    }
                }
            }
        }
        return canDo;
    }

    private boolean compareTwoContainer(VMSlot vmSlot, VMSlot vmSlotSide, VMContainer vmContainer, VMContainer vmContainerSide, String dlType, WorkingData workingData, StructureData structureData) {
        boolean condition = false;
        if (vmContainer.getSize().equals(vmContainerSide.getSize())) { // 尺寸
            // 合计重量限制、两箱重量差限制
            boolean weightCondition = true;
            if (vmContainer.getWeightKg() != null && vmContainerSide.getWeightKg() != null) {
                weightCondition = CalculateUtil.add(vmContainer.getWeightKg(), vmContainerSide.getWeightKg()) <= workingData.getCwpConfig().getAllContainerWeight();
                if (weightCondition) {
                    weightCondition = Math.abs(CalculateUtil.sub(vmContainer.getWeightKg(), vmContainerSide.getWeightKg())) <= workingData.getCwpConfig().getTandemWeightDiff();
                }
            }
            if (weightCondition) {
                // 箱型限制，适用箱型："GP,HC,OT"
                if (workingData.getCwpConfig().getTandemContainerType().contains(vmContainer.getType()) && workingData.getCwpConfig().getTandemContainerType().contains(vmContainerSide.getType())) {
                    boolean typeOT = true; // OT箱型带超限值的不能双吊具
                    if ("OT".equals(vmContainer.getType()) && PublicMethod.isOverrunCnt(vmContainer)) {
                        typeOT = false;
                    }
                    if ("OT".equals(vmContainerSide.getType()) && PublicMethod.isOverrunCnt(vmContainerSide)) {
                        typeOT = false;
                    }
                    boolean dangerCnt = true; // 危险品箱只针对（3,6,8,9）类可适用
                    if (StringUtil.isNotBlank(vmContainer.getDgCd()) && !"N".equals(vmContainer.getDgCd())) {
                        boolean flag = false;
                        Set<String> dangerCntSet = StringUtil.getSetBySplit(workingData.getCwpConfig().getTandemDangerCnt(), ",");
                        for (String danger : dangerCntSet) {
                            if (vmContainer.getDgCd().startsWith(danger)) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            dangerCnt = false;
                        }
                    }
                    if (StringUtil.isNotBlank(vmContainerSide.getDgCd()) && !"N".equals(vmContainerSide.getDgCd())) {
                        boolean flag = false;
                        Set<String> dangerCntSet = StringUtil.getSetBySplit(workingData.getCwpConfig().getTandemDangerCnt(), ",");
                        for (String danger : dangerCntSet) {
                            if (vmContainerSide.getDgCd().startsWith(danger)) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            dangerCnt = false;
                        }
                    }
                    if (typeOT && dangerCnt) {
                        // 两箱高度差限制
                        if (CWPDomain.DL_TYPE_DISC.equals(dlType)) { // 只处理卸船的情况
                            List<VMContainer> vmContainerList = getBelowVMContainerList(vmSlot, vmContainer, workingData, structureData); // 包括自己
                            List<VMContainer> vmContainerNextList = getBelowVMContainerList(vmSlotSide, vmContainerSide, workingData, structureData);
                            double height = getHeight(vmContainerList);
                            double heightNext = getHeight(vmContainerNextList);
                            List<VMContainer> vmContainerBottomList = new ArrayList<>();
                            int n = vmContainerList.size() > vmContainerNextList.size() ? vmContainerList.size() : vmContainerNextList.size();
                            for (int i = 0; i < n; i++) {
                                if (i < vmContainerList.size() && i >= vmContainerNextList.size()) {
                                    vmContainerBottomList.add(vmContainerList.get(i));
                                }
                                if (i < vmContainerNextList.size() && i >= vmContainerList.size()) {
                                    vmContainerBottomList.add(vmContainerNextList.get(i));
                                }
                            }
                            double h = CalculateUtil.mul(262.128, vmContainerBottomList.size()); // 按平箱高度补充
                            if (Math.abs(CalculateUtil.sub(height, heightNext)) <= workingData.getCwpConfig().getTandemHeightDiff() + h) {
                                condition = true;
                            }
                        }
                    }
                }
            }
        }
        return condition;
    }

    @Override
    public String getWorkFlow() {
        return CWPCraneDomain.CT_TWIN20;
    }

    @Override
    public String getSize() {
        return "2";
    }

    private List<VMContainer> getBelowVMContainerList(VMSlot vmSlot1, VMContainer vmContainer, WorkingData workingData, StructureData structureData) {
        List<VMContainer> vmContainerList = new ArrayList<>();
        VMSlot vmSlot = vmSlot1;
        while (vmContainer != null) {
            vmContainerList.add(vmContainer);
            vmSlot = structureData.getBelowVMSlot(vmSlot);
            vmContainer = workingData.getVMContainerByVMSlot(vmSlot, CWPDomain.DL_TYPE_DISC);
            // 还需要取过境箱子
            if (vmContainer == null) {
                vmContainer = workingData.getThroughVMContainerByVMSlot(vmSlot);
            }
        }
        return vmContainerList;
    }

    private double getHeight(List<VMContainer> vmContainerList) {
        double height = 0;
        for (VMContainer vmContainer : vmContainerList) {
            if (getHeightByVMContainer(vmContainer) == 0.0) {
                height = 0.0;
                break;
            } else {
                height = CalculateUtil.add(height, getHeightByVMContainer(vmContainer));
            }
        }
        return height;
    }

    private double getHeightByVMContainer(VMContainer vmContainer) {
        double height;
        if (CWPDomain.YES.equals(vmContainer.getIsHeight())) {
            height = 292.608; // 9.6
        } else if (CWPDomain.NO.equals(vmContainer.getIsHeight())) {
            height = 262.128; // 8.6
        } else {
            height = 0.0;
        }
        return height;
    }
}
