package com.cwp3.multiple.model;

import com.cwp3.model.vessel.VMSchedule;

/**
 * Created by csw on 2018/11/12.
 * Description: schedule是对VMSchedule的二次封装
 */
public class Schedule {

    private VMSchedule vmSchedule;

    public Schedule(VMSchedule vmSchedule) {
        this.vmSchedule = vmSchedule;
    }

    public VMSchedule getVmSchedule() {
        return vmSchedule;
    }
}
