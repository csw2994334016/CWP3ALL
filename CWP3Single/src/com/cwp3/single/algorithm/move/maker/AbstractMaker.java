package com.cwp3.single.algorithm.move.maker;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMSlot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by csw on 2017/9/21.
 * Description:
 */
public abstract class AbstractMaker {

    private Set<VMSlot> vmSlotSet;

    AbstractMaker() {
        vmSlotSet = new HashSet<>();
    }

    public abstract boolean canDo(VMSlot vmSlot, String dlType, WorkingData workingData, StructureData structureData);

    public abstract String getWorkFlow();

    public abstract String getSize();

    public Set<VMSlot> getVmSlotSet() {
        return vmSlotSet;
    }
}
