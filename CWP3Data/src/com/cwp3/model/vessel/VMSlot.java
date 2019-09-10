package com.cwp3.model.vessel;


import java.io.Serializable;

/**
 * Created by csw on 2017/8/14.
 * Description: 一个船箱位对应一个slot
 */
public class VMSlot implements Serializable {

    private VMPosition vmPosition;

    public VMSlot(VMPosition vmPosition) {
        this.vmPosition = vmPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            VMSlot vmSlot = (VMSlot) obj;
            return this.vmPosition.getVLocation().equals(vmSlot.getVmPosition().getVLocation());
        } else {
            return false;
        }
    }

    public VMPosition getVmPosition() {
        return vmPosition;
    }


}
