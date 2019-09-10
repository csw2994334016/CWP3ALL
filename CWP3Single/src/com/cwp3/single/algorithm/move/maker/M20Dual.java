package com.cwp3.single.algorithm.move.maker;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMContainerSlot;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.method.PublicMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2017/9/20.
 * Description:
 */
public class M20Dual extends AbstractMaker {

    @Override
    public boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData) {
        boolean canDo = false;
        VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
        if (!CWPDomain.SEPARATED_SLOT.equals(vmContainerSlot.getSize())) { //不是全隔槽的slot
            VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
            if (vmContainer != null && vmContainer.getSize().startsWith(getSize())) {
                VMSlot vmSlotPair = structureData.getPairVMSlot(vmSlot); //对面slot
                if (vmSlotPair != null) {
                    VMContainer vmContainerPair = workingData.getVMContainerByVMSlot(vmSlotPair, dlType);
                    if (vmContainerPair != null && PublicMethod.hasNoneWorkFlow(vmContainerPair.getWorkFlow())) { //对面集装箱没有编写作业工艺
                        if (compareTwoContainer(vmContainer, vmContainerPair, dlType, workingData, structureData)) {
                            canDo = true;
                        }
                    }
                }
            }
        }
        return canDo;
    }

    @Override
    public String getWorkFlow() {
        return CWPCraneDomain.CT_DUAL20;
    }

    @Override
    public String getSize() {
        return "2";
    }

    private boolean compareTwoContainer(VMContainer vmContainer, VMContainer vmContainerPair, String dlType, WorkingData workingData, StructureData structureData) {
//        boolean base = super.compareTwoContainer(vmContainer, vmContainerPair);
        //尺寸
        boolean sizeFlag = vmContainer.getSize().equals(vmContainerPair.getSize()); //尺寸
        //重量差
        boolean weightFlag = true;
        if (vmContainer.getWeightKg() != null && vmContainerPair.getWeightKg() != null) {
            weightFlag = Math.abs(vmContainer.getWeightKg() - vmContainerPair.getWeightKg()) < workingData.getCwpConfig().getTwinWeightDiff();
        }
        //特殊箱型
//        boolean type = !CWPDomain.CNT_TYPE_TK.equals(vmContainer.getType()) && !CWPDomain.CNT_TYPE_FR.equals(vmContainer.getType());

        //箱高
        boolean heightFlag = vmContainer.getIsHeight().equals(vmContainerPair.getIsHeight());
        if (CWPDomain.DL_TYPE_DISC.equals(dlType)) {
            List<VMContainer> vmContainerList = getBelowVMContainerList(vmContainer, workingData, structureData);
            List<VMContainer> vmContainerPairList = getBelowVMContainerList(vmContainerPair, workingData, structureData);
            if (vmContainerList.size() == vmContainerPairList.size()) {
                double height = getHeight(vmContainerList);
                double heightPair = getHeight(vmContainerPairList);
                if (height != heightPair) {
                    heightFlag = false;
                }
            }
        }

        // 超限箱作业工艺OH/OW/OL/O统一为单吊作业
        boolean overrunFlag = !PublicMethod.isOverrunCnt(vmContainer) && !PublicMethod.isOverrunCnt(vmContainerPair);

        return sizeFlag && weightFlag && heightFlag && overrunFlag;
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
