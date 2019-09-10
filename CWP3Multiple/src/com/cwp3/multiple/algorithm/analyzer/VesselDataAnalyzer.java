package com.cwp3.multiple.algorithm.analyzer;

import com.cwp3.multiple.data.VesselData;
import com.cwp3.utils.CalculateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by csw on 2018/11/12.
 * Description:
 */
public class VesselDataAnalyzer {

    public void sortVesselDataByPosition(List<VesselData> vesselDataList) {
        Collections.sort(vesselDataList, new Comparator<VesselData>() {
            @Override
            public int compare(VesselData o1, VesselData o2) {
                return o1.getVmSchedule().getPlanStartPst().compareTo(o2.getVmSchedule().getPlanStartPst());
            }
        });
    }
    public void sortVesselDataByBeginTimePriority(List<VesselData> vesselDataList) {
        Collections.sort(vesselDataList, new Comparator<VesselData>() {
            @Override
            public int compare(VesselData o1, VesselData o2) {
                if (o1.getVmSchedule().getPlanBeginWorkTime().compareTo(o2.getVmSchedule().getPlanBeginWorkTime()) == 0) {
                    return o1.getVmSchedule().getPlanEndWorkTime().compareTo(o2.getVmSchedule().getPlanEndWorkTime());
                } else {
                    return o1.getVmSchedule().getPlanBeginWorkTime().compareTo(o2.getVmSchedule().getPlanBeginWorkTime());
                }
            }
        });
    }
    public void sortVesselDataByEndTimePriority(List<VesselData> vesselDataList) {
        Collections.sort(vesselDataList, new Comparator<VesselData>() {
            @Override
            public int compare(VesselData o1, VesselData o2) {
                if (o1.getVmSchedule().getPlanEndWorkTime().compareTo(o2.getVmSchedule().getPlanEndWorkTime()) == 0) {
                    return o1.getVmSchedule().getPlanBeginWorkTime().compareTo(o2.getVmSchedule().getPlanBeginWorkTime());
                } else {
                    return o1.getVmSchedule().getPlanEndWorkTime().compareTo(o2.getVmSchedule().getPlanEndWorkTime());
                }
            }
        });
    }
    public void analyzeCurMinCraneNum(Long curTime,List<VesselData> vesselDataList) {
        for (VesselData vesselData : vesselDataList) {
            double minCraneNum = computeCurMinCraneNum(curTime, vesselData);
            if (curTime == null) {
                vesselData.setDpMomMinCraneNum(minCraneNum);
            }
            vesselData.setDpCurMinCraneNum(minCraneNum);
        }
    }

    public double computeCurMinCraneNum(Long curTime, VesselData vesselData) {
        Long vesselTime;
        if (curTime != null) {
            vesselTime = vesselData.getVmSchedule().getPlanEndWorkTime().getTime() / 1000 - curTime;
        } else {
            vesselTime = vesselData.getVmSchedule().getVesselTime();
        }
        return CalculateUtil.div(vesselData.getDpRemainAmount().doubleValue(), vesselTime.doubleValue(), 2);
    }

    public long getStartTime(List<VesselData> vesselDataList) {
        long startTime = Long.MAX_VALUE;
        for (VesselData vesselData : vesselDataList) {
            startTime = Math.min(vesselData.getVmSchedule().getPlanBeginWorkTime().getTime() / 1000, startTime);
        }
        return startTime;
    }

    public List<VesselData> getVesselListByTimeSpan(long startTime, long endTime, List<VesselData> vesselDataList) {
        return null;
    }

    public List<VesselData> getCurVesselDataListByTimeSpan(long st, List<VesselData> vesselDataList) {
        List<VesselData> curVesselDataList = new ArrayList<>();
        for (VesselData vesselData : vesselDataList) {
            if (st >= vesselData.getVmSchedule().getPlanBeginWorkTime().getTime() / 1000 && st < vesselData.getVmSchedule().getPlanEndWorkTime().getTime() / 1000) {
                curVesselDataList.add(vesselData);
            }
        }
        return curVesselDataList;
    }

    public void sortVesselDataByPriority(List<VesselData> vesselDataList) {
        Collections.sort(vesselDataList, new Comparator<VesselData>() {
            @Override
            public int compare(VesselData o1, VesselData o2) {
                if (o1.getVmSchedule().getPlanBeginWorkTime().compareTo(o2.getVmSchedule().getPlanBeginWorkTime()) == 0) {
                    return o1.getVmSchedule().getPlanEndWorkTime().compareTo(o2.getVmSchedule().getPlanEndWorkTime());
                } else {
                    return o1.getVmSchedule().getPlanBeginWorkTime().compareTo(o2.getVmSchedule().getPlanBeginWorkTime());
                }
            }
        });
    }
}
