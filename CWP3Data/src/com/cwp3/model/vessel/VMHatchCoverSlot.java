package com.cwp3.model.vessel;

/**
 * Created by CarloJones on 2018/4/7.
 */
public class VMHatchCoverSlot extends VMSlot {

    private Long hatchId;

    public VMHatchCoverSlot(VMPosition vmPosition, Long hatchId) {
        super(vmPosition);
        this.hatchId = hatchId;
    }

    public Long getHatchId() {
        return hatchId;
    }

}
