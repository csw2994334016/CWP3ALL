package com.cwp3.single.algorithm.move.maker;


import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;

/**
 * Created by csw on 2017/9/20.
 * Description:
 */
public class M40Single extends AbstractMaker {

    @Override
    public boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData) {
        boolean canDo = false;
        VMContainer vmContainer = workingData.getVMContainerByVMSlot(vmSlot, dlType);
        if (vmContainer != null && vmContainer.getSize().startsWith(getSize())) {
            canDo = true;
        }
        return canDo;
    }

    @Override
    public String getWorkFlow() {
        return CWPCraneDomain.CT_SINGLE40;
    }

    @Override
    public String getSize() {
        return "4";
    }

}
