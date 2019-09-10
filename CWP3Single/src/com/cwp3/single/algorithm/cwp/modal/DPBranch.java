package com.cwp3.single.algorithm.cwp.modal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2018/6/13.
 * Description:
 */
public class DPBranch implements Serializable {

    private String dpStrategyType;
    private List<CWPCrane> dpCwpCraneList;
    private List<DPCraneSelectBay> dpCraneSelectBays;

    public DPBranch() {
        dpCwpCraneList = new ArrayList<>();
        dpCraneSelectBays = new ArrayList<>();
    }

    public String getDpStrategyType() {
        return dpStrategyType;
    }

    public void setDpStrategyType(String dpStrategyType) {
        this.dpStrategyType = dpStrategyType;
    }

    public List<CWPCrane> getDpCwpCraneList() {
        return dpCwpCraneList;
    }

    public void setDpCwpCraneList(List<CWPCrane> dpCwpCraneList) {
        this.dpCwpCraneList = dpCwpCraneList;
    }

    public List<DPCraneSelectBay> getDpCraneSelectBays() {
        return dpCraneSelectBays;
    }

    public void setDpCraneSelectBays(List<DPCraneSelectBay> dpCraneSelectBays) {
        this.dpCraneSelectBays = dpCraneSelectBays;
    }

    public DPBranch deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            return (DPBranch) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
