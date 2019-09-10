package com.cwp3.single.algorithm.cwp.modal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2017/2/15 16:15.
 * Explain:
 */
public class DPResult implements Serializable {

    private int dpFeatureCode;
    private Long dpWorkTime;
    private Double dpDistance;
    private List<DPPair> dpTraceBack;
    private List<DPPair> dpCranePosition;

    public DPResult() {
        dpFeatureCode = 0;
        dpWorkTime = 0L;
        dpDistance = Double.MAX_VALUE;
        dpTraceBack = new ArrayList<>();
        dpCranePosition = new ArrayList<>();
    }

    public int getDpFeatureCode() {
        return dpFeatureCode;
    }

    public void setDpFeatureCode(int dpFeatureCode) {
        this.dpFeatureCode = dpFeatureCode;
    }

    public Long getDpWorkTime() {
        return dpWorkTime;
    }

    public void setDpWorkTime(Long dpWorkTime) {
        this.dpWorkTime = dpWorkTime;
    }

    public Double getDpDistance() {
        return dpDistance;
    }

    public void setDpDistance(Double dpDistance) {
        this.dpDistance = dpDistance;
    }

    public List<DPPair> getDpTraceBack() {
        return dpTraceBack;
    }

    public void setDpTraceBack(List<DPPair> dpTraceBack) {
        this.dpTraceBack = dpTraceBack;
    }

    public List<DPPair> getDpCranePosition() {
        return dpCranePosition;
    }

    public void setDpCranePosition(List<DPPair> dpCranePosition) {
        this.dpCranePosition = dpCranePosition;
    }

    public DPResult deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            return (DPResult) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}


