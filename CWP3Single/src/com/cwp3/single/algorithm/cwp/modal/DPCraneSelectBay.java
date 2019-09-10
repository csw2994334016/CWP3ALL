package com.cwp3.single.algorithm.cwp.modal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2017/3/8 19:42.
 * Explain:
 */
public class DPCraneSelectBay implements Serializable {

    private DPPair dpPair;
    private Long dpWorkTime;
    private Double dpDistance;
    private DPFeature dpFeature;
    private Boolean troughMachine;

    private List<DPFeature> dpFeatureList;

    public DPCraneSelectBay(DPPair dpPair) {
        this.dpPair = dpPair;
        dpWorkTime = 0L;
        dpDistance = Double.MAX_VALUE;
        troughMachine = Boolean.FALSE;
        dpFeatureList = new ArrayList<>();
    }

    public Long getDpWorkTime() {
        return dpWorkTime;
    }

    public void setDpWorkTime(Long dpWorkTime) {
        this.dpWorkTime = dpWorkTime;
    }

    public void addDpWorkTime(Long dpWorkTime) {
        this.dpWorkTime += dpWorkTime;
    }

    public Double getDpDistance() {
        return dpDistance;
    }

    public void setDpDistance(Double dpDistance) {
        this.dpDistance = dpDistance;
    }

    public DPFeature getDpFeature() {
        return dpFeature;
    }

    public void setDpFeature(DPFeature dpFeature) {
        this.dpFeature = dpFeature;
    }

    public Boolean getTroughMachine() {
        return troughMachine;
    }

    public void setTroughMachine(Boolean troughMachine) {
        this.troughMachine = troughMachine;
    }

    public DPPair getDpPair() {
        return dpPair;
    }

    public List<DPFeature> getDpFeatureList() {
        return dpFeatureList;
    }

    public void setDpFeatureList(List<DPFeature> dpFeatureList) {
        this.dpFeatureList = dpFeatureList;
    }

    public boolean equalsWithPair(Object obj) {
        return this.dpPair.getFirst().equals(((DPPair) obj).getFirst()) && this.dpPair.getSecond().equals(((DPPair) obj).getSecond());
    }

    public static DPCraneSelectBay getDpCraneSelectBayByPair(List<DPCraneSelectBay> dpCraneSelectBays, DPPair DPPair) {
        for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBays) {
            if (dpCraneSelectBay.equalsWithPair(DPPair)) {
                return dpCraneSelectBay;
            }
        }
        return null;
    }

    public static List<DPCraneSelectBay> getDpCraneSelectBaysByCrane(List<DPCraneSelectBay> dpCraneSelectBays, String craneNo) {
        List<DPCraneSelectBay> dpCraneSelectBayList = new ArrayList<>();
        for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBays) {
            if (dpCraneSelectBay.getDpPair().getFirst().equals(craneNo)) {
                dpCraneSelectBayList.add(dpCraneSelectBay);
            }
        }
        return dpCraneSelectBayList;
    }

    public DPCraneSelectBay deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            return (DPCraneSelectBay) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
