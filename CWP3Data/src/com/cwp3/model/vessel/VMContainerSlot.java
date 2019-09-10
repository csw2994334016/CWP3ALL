package com.cwp3.model.vessel;

/**
 * Created by CarloJones on 2018/4/7.
 */
public class VMContainerSlot extends VMSlot{

    private VMBay vmBay;
    private String size; //全隔槽信息

    public VMContainerSlot(VMPosition vmPosition, VMBay vmBay, String size) {
        super(vmPosition);
        this.vmBay = vmBay;
        this.size = size;
    }

    public VMBay getVmBay() {
        return vmBay;
    }

    public String getSize() {
        return size;
    }
}
