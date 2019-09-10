package com.cwp3.model.vessel;


/**
 * Created by csw on 2017/8/14.
 * Description: 船舶机械信息：驾驶台、烟囱等
 */
public class VMMachine {

    private String vesselCode; //船舶代码
    private String machineNo; //机械编号
    private String machineType;//机械类型
    private Double machineWidth;//宽度
    private Double  machineHeight;//高度
    private Double machinePosition;//相对于船头的位置

    public String getVesselCode() {
        return vesselCode;
    }

    public void setVesselCode(String vesselCode) {
        this.vesselCode = vesselCode;
    }

    public String getMachineNo() {
        return machineNo;
    }

    public void setMachineNo(String machineNo) {
        this.machineNo = machineNo;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public Double getMachineWidth() {
        return machineWidth;
    }

    public void setMachineWidth(Double machineWidth) {
        this.machineWidth = machineWidth;
    }

    public Double getMachineHeight() {
        return machineHeight;
    }

    public void setMachineHeight(Double machineHeight) {
        this.machineHeight = machineHeight;
    }

    public Double getMachinePosition() {
        return machinePosition;
    }

    public void setMachinePosition(Double machinePosition) {
        this.machinePosition = machinePosition;
    }
}
