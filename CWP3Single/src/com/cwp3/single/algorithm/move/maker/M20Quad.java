package com.cwp3.single.algorithm.move.maker;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMContainerSlot;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.utils.StringUtil;

import java.util.*;

/**
 * Created by csw on 2017/9/20.
 * Description:
 */
public class M20Quad extends AbstractMaker {

    @Override
    public boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData) {
        boolean canDo = false;
        VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
        if (!CWPDomain.SEPARATED_SLOT.equals(vmContainerSlot.getSize())) { //不是全隔槽的slot
            Map<String, VMContainer> vmContainerMap = new HashMap<>();
            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
            if (vmContainer != null && vmContainer.getSize().startsWith(getSize())) {
                vmContainerMap.put(vmSlot.getVmPosition().getVLocation(), vmContainer);
                this.getVmSlotSet().add(vmSlot);
                // 对面slot
                VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot);
                VMContainer vmContainerPair = workingData.getVMContainerByVMSlot(vmSlotPair, dlType);
                if (vmContainerPair != null && PublicMethod.hasNoneWorkFlow(vmContainerPair.getWorkFlow())) { //对面集装箱没有编写作业工艺
                    vmContainerMap.put(vmSlotPair.getVmPosition().getVLocation(), vmContainerPair);
                    this.getVmSlotSet().add(vmSlotPair);
                    // 旁边slot
                    String direction = dlType.equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.ROW_SEQ_LAND_SEA : CWPDomain.ROW_SEQ_SEA_LAND;
                    String oddOrEven = workingData.getOddOrEvenBySeaOrLand(direction);
                    VMSlot vmSlotSide = structureData.getSideVMSlot(vmSlot, oddOrEven);
                    VMContainer vmContainerSide = workingData.getVMContainerByVMSlot(vmSlotSide, dlType);
                    if (vmContainerSide != null && PublicMethod.hasNoneWorkFlow(vmContainerSide.getWorkFlow())) {
                        vmContainerMap.put(vmSlotSide.getVmPosition().getVLocation(), vmContainerSide);
                        this.getVmSlotSet().add(vmSlotSide);
                        // 旁边slot对应着对面slot的集装箱
                        VMSlot vmSlotSidePair = structureData.getPairVMSlot(vmSlotSide);
                        VMContainer vmContainerSidePair = workingData.getVMContainerByVMSlot(vmSlotSidePair, dlType);
                        if (vmContainerSidePair != null && PublicMethod.hasNoneWorkFlow(vmContainerSidePair.getWorkFlow())) {
                            vmContainerMap.put(vmSlotSidePair.getVmPosition().getVLocation(), vmContainerSidePair);
                            this.getVmSlotSet().add(vmSlotSidePair);
                        }
                    }
                }
            }
            if (vmContainerMap.size() == 4 && compareFourContainer(vmContainerMap, dlType, workingData, structureData)) {
                for (Map.Entry<String, VMContainer> entry : vmContainerMap.entrySet()) {
                    entry.getValue().setWorkFlow(getWorkFlow());
                }
                canDo = true;
            }
        }
        return canDo;
    }

    @Override
    public String getWorkFlow() {
        return CWPCraneDomain.CT_QUAD20;
    }

    @Override
    public String getSize() {
        return "2";
    }

    private boolean compareFourContainer(Map<String, VMContainer> vmContainerMap, String dlType, WorkingData workingData, StructureData structureData) {
        boolean condition = false;
        //尺寸
        boolean sizeCondition = true;
        for (Map.Entry<String, VMContainer> entry : vmContainerMap.entrySet()) {
            if (!entry.getValue().getSize().startsWith(getSize())) {
                sizeCondition = false;
                break;
            }
        }
        if (sizeCondition) {
            // 判断是否全是空箱
            boolean emptyCondition = true;
            for (Map.Entry<String, VMContainer> entry : vmContainerMap.entrySet()) {
                if (!"E".equals(entry.getValue().getEfFlag())) {
                    emptyCondition = false;
                    break;
                }
            }
            if (emptyCondition) {
                // 箱型限制，适用箱型："GP,HC,OT"
                boolean typeCondition = true;
                for (Map.Entry<String, VMContainer> entry : vmContainerMap.entrySet()) {
                    if (workingData.getCwpConfig().getTandemContainerType().contains(entry.getValue().getType())) {
                        if ("OT".equals(entry.getValue().getType()) && PublicMethod.isOverrunCnt(entry.getValue())) { // OT箱型带超限值的不能双吊具
                            typeCondition = false;
                            break;
                        }
                        if (StringUtil.isNotBlank(entry.getValue().getDgCd()) && !"N".equals(entry.getValue().getDgCd())) {
                            boolean flag = false;
                            Set<String> dangerCntSet = StringUtil.getSetBySplit(workingData.getCwpConfig().getTandemDangerCnt(), ",");
                            for (String danger : dangerCntSet) {
                                if (entry.getValue().getDgCd().startsWith(danger)) {
                                    flag = true;
                                }
                            }
                            if (!flag) {  // 危险品箱只针对（3,6,8,9）类可适用
                                typeCondition = false;
                                break;
                            }
                        }
                    } else {
                        typeCondition = false;
                        break;
                    }
                }
                if (typeCondition) {
                    condition = true;
                }
            }
        }
        // 超限箱作业工艺OH/OW/OL/O统一为单吊作业

        return condition;
    }

    private double getHeight(List<VMContainer> vmContainerList) {
        double height = 0;
        for (VMContainer vmContainer : vmContainerList) {
            if (CWPDomain.YES.equals(vmContainer.getIsHeight())) {
                height += 9.6;
            } else if (CWPDomain.NO.equals(vmContainer.getIsHeight())) {
                height += 8.6;
            } else {
                height = 0.0;
                break;
            }
        }
        return height;
    }

    private List<VMContainer> getBelowVMContainerList(VMContainer vmContainer, WorkingData workingData, StructureData structureData) {
        List<VMContainer> vmContainerList = new ArrayList<>();
        VMSlot vmSlot = structureData.getVMSlotByVLocation(vmContainer.getvLocation());
        while (true) {
            if (vmContainer == null) {
                break;
            }
            if (vmContainer.getSize().startsWith("4")) {
                break;
            }
            vmContainerList.add(vmContainer);
            vmSlot = structureData.getBelowVMSlot(vmSlot);
            if (structureData.isSteppingVMSlot(vmSlot)) {
                break;
            }
            vmContainer = workingData.getVMContainerByVMSlot(vmSlot, CWPDomain.DL_TYPE_DISC);
            // 还需要取过境箱子
            if (vmContainer == null) {
                vmContainer = workingData.getThroughVMContainerByVMSlot(vmSlot);
            }
        }
        return vmContainerList;
    }

}
