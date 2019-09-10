package com.cwp3.multiple.algorithm.process;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.model.log.Logger;
import com.cwp3.multiple.algorithm.analyzer.VesselDataAnalyzer;
import com.cwp3.multiple.data.AllData;
import com.cwp3.multiple.data.VesselData;
import com.cwp3.multiple.method.AllDataMethod;
import com.cwp3.multiple.model.DefaultValue;
import com.cwp3.utils.CalculateUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by csw on 2018/11/12.
 * Description:
 */
public class CraneAllocationProcess {

    private AllDataMethod allDataMethod;
    private VesselDataAnalyzer vesselDataAnalyzer;

    public CraneAllocationProcess(AllRuntimeData allRuntimeData) {
        allDataMethod = new AllDataMethod(allRuntimeData);
        vesselDataAnalyzer = new VesselDataAnalyzer();
    }

    public void processCraneAllocation() {
        Logger logger = allDataMethod.getLogger();
        logger.logInfo("开始执行...");
        long st = System.currentTimeMillis();

        AllData allData = allDataMethod.initAllData();

        List<VesselData> vesselDataList = allData.getAllVesselDataList();
        // 1、根据船舶停靠坐标，进行排序
        vesselDataAnalyzer.sortVesselDataByPosition(vesselDataList);

        // 2、分析船舶最少使用桥机数目（该方法会被后面多次调用）
        vesselDataAnalyzer.analyzeCurMinCraneNum(null,vesselDataList);

        // 3、得到船舶作业时间轴，这个时间轴是程序的主要循环控制条件
        long startTime = vesselDataAnalyzer.getStartTime(vesselDataList);
        allData.setStartTime(startTime);
        int T = 24;

        // 4、细化每个小时（半个小时）内，每部桥机应该作业哪条船舶，其中主要判断条件：
        // 桥机资源紧张时，涉及相邻船需要错开桥机，形成上下路计划；
        List<long[][]> timeCraneList = new ArrayList<>();
        for (int t = 0; t < T; t++) {
            timeCraneList = assignCraneWithTimeSpan(T, t, timeCraneList, allData); // 大于等于开始时间，小于结束时间：>= startTime && < endTime
        }

        // 5、保存结果
        for (int i = 0; i < timeCraneList.size(); i++) {
            allData.getTimeCraneMap().put(String.valueOf(i), timeCraneList.get(i));
        }
        allDataMethod.generateResult(timeCraneList.get(0), allData);

        long et = System.currentTimeMillis();
        logger.logInfo("执行结束，执行时间是：" + (et - st) / 1000 + "秒");
    }

    private List<long[][]> singleAssignCraneWithTimeSpan(int T, AllData allData) {

        List<VesselData> vesselDataList = allData.getAllVesselDataList();
        // 1、根据船舶停靠坐标，进行排序
        vesselDataAnalyzer.sortVesselDataByPosition(vesselDataList);

        int C = allData.getAllCranes().size();

        int needCN = 0;
        for (VesselData vesselData : vesselDataList) {
            needCN += vesselData.getDpCurCeilMinCraneNum();
        }

        List<long[][]> curTimeCraneList = new ArrayList<>();
        long[][] timeCrane = new long[T][C];
        if (needCN <= C) {
            int i = 0;
            for (VesselData vesselData : vesselDataList) {
                for (int t = 0; t < T; t++) {
                    long curTime = allData.getStartTime() + DefaultValue.timeSpan * t;
                    if (curTime >= vesselData.getVmSchedule().getPlanBeginWorkTime().getTime() / 1000 && curTime < vesselData.getVmSchedule().getPlanEndWorkTime().getTime() / 1000) {
                        for (int c = i; c < i + vesselData.getDpCurCeilMinCraneNum(); c++) {
                            timeCrane[t][c] = vesselData.getBerthId();
                        }
                    }
                }
                i = i + vesselData.getDpCurCeilMinCraneNum();
            }
        } else {
            allDataMethod.getLogger().logError("桥机资源不够，无法安排资源计划！");
        }
        curTimeCraneList.add(timeCrane);

        return curTimeCraneList;
    }


    private List<long[][]> assignCraneWithTimeSpan(int T, int t, List<long[][]> lastTimeCraneList, AllData allData) {
        allDataMethod.getLogger().logInfo("第" + t + "次分配桥机资源。。。");

        long curTime = allData.getStartTime() + DefaultValue.timeSpan * t;

        // 得到当前时间段内的船舶
        List<VesselData> curVesselDataList = vesselDataAnalyzer.getCurVesselDataListByTimeSpan(curTime, allData.getAllVesselDataList());

        if (curVesselDataList.size() == 0) {
            return lastTimeCraneList;
        }

        // todo: 按优先级排序，注意谁需要加桥机则优先级排在前面考虑
        vesselDataAnalyzer.sortVesselDataByPriority(curVesselDataList);

        // 根据当前时间段内的船舶，去掉上次无用的桥机分配情况，即去除无用的分支
        List<long[][]> availableTimeCraneList = getAvailableTimeCraneList(t, lastTimeCraneList, curVesselDataList, allData);

        // 给当前时间段内的船舶分配桥机，穷举多种可能性，创建新的分支
        List<long[][]> curTimeCraneList = getCurTimeCraneList(t, T, availableTimeCraneList, curVesselDataList, allData);

        return curTimeCraneList;
    }

    private List<long[][]> getAvailableTimeCraneList(int t, List<long[][]> lastTimeCraneList, List<VesselData> curVesselDataList, AllData allData) {
        if (lastTimeCraneList.size() == 0) { // 第一次
            return lastTimeCraneList;
        }
        List<long[][]> availableTimeCraneList = new ArrayList<>();
        // 原则：
        // 1、当前curVesselDataList里面的船舶没有桥机选择，即timeCrane[t-1]是无效的
        for (VesselData vesselData : curVesselDataList) {
            for (long[][] lastTimeCrane : lastTimeCraneList) {

            }
        }
        return lastTimeCraneList;
    }

    private List<long[][]> getCurTimeCraneList(int t, int T, List<long[][]> lastTimeCraneList, List<VesselData> curVesselDataList, AllData allData) {
        long curTime = allData.getStartTime() + DefaultValue.timeSpan * t;
        int C = allData.getAllCranes().size();
        List<long[][]> curTimeCraneList = new ArrayList<>();
        if (lastTimeCraneList.size() == 0) { //说明所有船舶第一次被分配桥机
            // 当前时刻，第一艘船舶
            VesselData vesselData1 = curVesselDataList.get(0);
            Long curRemainAmount1 = vesselData1.getDpRemainAmount(); // 当前时间，船舶剩余总箱量作业时间
            double curMinCraneNum1 = getCurMinCraneNum(curTime, curRemainAmount1, vesselData1);
            int craneNum1 = (int) Math.ceil(curMinCraneNum1);
            for (int i = 0; i <= C - craneNum1; i++) {
                long[][] timeCrane = new long[T][C];
                for (int n = 0; n < craneNum1; n++) {
                    timeCrane[t][i + n] = vesselData1.getBerthId();
                }
                curTimeCraneList.add(timeCrane);
            }
            // 当前时刻，后续的船舶
            curTimeCraneList = getLatterTimeCraneList(t, curVesselDataList, curTimeCraneList, allData);
        } else {
            for (long[][] lastTimeCrane : lastTimeCraneList) { // 上次分配桥机情况的有效分支
                List<long[][]> timeCraneList1 = new ArrayList<>();
                // 当前时刻，第一艘船舶
                VesselData vesselData1 = curVesselDataList.get(0);
                List<List<Integer>> curCraneList1 = getCurCraneList(t, lastTimeCrane, vesselData1, allData);
                timeCraneList1 = addTimeCraneToList(t, curCraneList1, lastTimeCrane, vesselData1, timeCraneList1);
                // 当前时刻，后续的船舶
                timeCraneList1 = getLatterTimeCraneList(t, curVesselDataList, timeCraneList1, allData);
                for (long[][] timeCrane : timeCraneList1) {
                    if (!containTimeCraneToT(t, curTimeCraneList, timeCrane)) {
                        curTimeCraneList.add(timeCrane);
                    }
                }
            }
        }
        return curTimeCraneList;
    }

    private boolean containTimeCraneToT(int T, List<long[][]> curTimeCraneList, long[][] timeCrane) {
        for (long[][] curTimeCrane : curTimeCraneList) {
            boolean same = true;
            for (int t = 0; t <= T; t++) {
                for (int c = 0; c < timeCrane[t].length; c++) {
                    if (timeCrane[t][c] != curTimeCrane[t][c]) {
                        same = false;
                        break;
                    }
                }
            }
            if (same) {
                return true;
            }
        }
        return false;
    }

    private List<long[][]> getLatterTimeCraneList(int t, List<VesselData> curVesselDataList, List<long[][]> timeCraneList1, AllData allData) {
        for (int v = 1; v < curVesselDataList.size(); v++) {
            VesselData vesselData = curVesselDataList.get(v);
            List<long[][]> tempTimeCraneList = new ArrayList<>();
            for (long[][] timeCrane : timeCraneList1) {
                List<List<Integer>> curCraneList = getCurCraneList(t, timeCrane, vesselData, allData);
                tempTimeCraneList = addTimeCraneToList(t, curCraneList, timeCrane, vesselData, tempTimeCraneList);
            }
            if (tempTimeCraneList.size() > 0) {
                timeCraneList1 = tempTimeCraneList;
            } else { // timeCrane是无效的，应该从timeCraneList中去除
                allDataMethod.getLogger().logDebug("当前时刻" + t + "，由于船舶(" + vesselData.getBerthId() + ")没有找到合适的桥机资源，去除" + (t - 1) + "时刻生成的分支！");
                timeCraneList1.clear();
                break; // 当有一条船没有可选的桥机时，则放弃上条船选择的分支
            }
        }
        return timeCraneList1;
    }

    private List<long[][]> addTimeCraneToList(int t, List<List<Integer>> curCraneList, long[][] lastTimeCrane, VesselData vesselData, List<long[][]> curTimeCraneList) {
        if (curCraneList.size() > 0) {
            for (List<Integer> cranes : curCraneList) {
                long[][] timeCrane = copyTimeCrane(lastTimeCrane);
                for (int c : cranes) {
                    timeCrane[t][c] = vesselData.getBerthId();
                }
                if (!containTimeCrane(t, curTimeCraneList, timeCrane)) {
                    curTimeCraneList.add(timeCrane);
                }
            }
        }
        return curTimeCraneList;
    }

    private boolean containTimeCrane(int t, List<long[][]> curTimeCraneList, long[][] timeCrane) {
        for (long[][] curTimeCrane : curTimeCraneList) {
            boolean same = true;
            for (int c = 0; c < timeCrane[t].length; c++) {
                if (timeCrane[t][c] != curTimeCrane[t][c]) {
                    same = false;
                    break;
                }
            }
            if (same) {
                return true;
            }
        }
        return false;
    }

    private List<List<Integer>> getCurCraneList(int t, long[][] curTimeCrane, VesselData vesselData, AllData allData) {
        long curTime = allData.getStartTime() + DefaultValue.timeSpan * t;
        int C = allData.getAllCranes().size();
        int craneTimeNum = 0;
        int lastCraneNum = 0;
        List<Integer> lastCranes = new ArrayList<>();
        if (t > 0) {
            for (int tt = 0; tt <= t - 1; tt++) {
                for (int i = 0; i < curTimeCrane[tt].length; i++) {
                    if (curTimeCrane[tt][i] == vesselData.getBerthId()) {
                        craneTimeNum++;
                    }
                    if (tt == t - 1 && curTimeCrane[tt][i] == vesselData.getBerthId()) {
                        lastCraneNum++;
                        lastCranes.add(i);
                    }
                }
            }
        }
        Long curRemainAmount = vesselData.getAllAmount() - craneTimeNum * DefaultValue.timeSpan; // 当前时间，船舶剩余总箱量作业时间
        double curMinCraneNum = getCurMinCraneNum(curTime, curRemainAmount, vesselData);
        int craneNum = (int) Math.ceil(curMinCraneNum); // todo: 注意左右借桥机的情况，当前时刻的桥机数目是关键条件
        List<List<Integer>> craneList = new ArrayList<>();
        // t时刻船舶选择作业桥机的可能性，决定了当前分支数目，原则：
        // 1、timeCrane[t]为0的才能被选择
        // 2、timeCrane[t]的值（berthId）要有线序性，即符合船舶停靠位置
        int s = -1;
        for (int i = 0; i < curTimeCrane[t].length; i++) {
            if (curTimeCrane[t][i] > 1 && isRightVessel(vesselData, allData.getVesselDataByBerthId(curTimeCrane[t][i]))) { // t时刻，桥机已经被其它船选中了，且该船在当前船舶左侧，只能往后选桥机
                s = i; // 开始桥机下标后移
            }
        }
        s = s + 1;
        for (int i = s; i <= C - craneNum; i++) {
            if (curTimeCrane[t][i] > 1 && isRightVessel(allData.getVesselDataByBerthId(curTimeCrane[t][i]), vesselData)) { // t时刻，桥机已经被其它船选中了，且该船在当前船舶右侧
                break;
            }
            if (curTimeCrane[t][i] == 0) { // 没被选择
                List<Integer> cranes = new LinkedList<>();
                for (int n = 0; n < craneNum; n++) { // 取连续的符合timeCrane[t]为0原则的craneNum部桥机
                    if (curTimeCrane[t][i + n] == 0) {
                        cranes.add(i + n);
                    } else {
                        break;
                    }
                }
                if (cranes.size() == craneNum) {
                    if (lastCraneNum > 0) { // 1、上次船舶选择了桥机，连续取上次选择的桥机
                        if (craneNum == lastCraneNum) {
                            boolean contain = true;
                            for (int c = 0; c < craneNum; c++) {
                                if (!cranes.get(c).equals(lastCranes.get(c))) {
                                    contain = false;
                                }
                            }
                            if (contain) {
                                craneList.add(cranes);
                            }
                        } else if (craneNum > lastCraneNum) { // 当前比上次的桥机数目多，则这次桥机选择必须包含上次选择的桥机

                        } else { // 当前比上次的桥机数目少，则这次桥机选择只能是从上次选择的桥机中选
                            boolean contain = false;
                            for (Integer c : lastCranes) {
                                if (cranes.contains(c)) {
                                    contain = true;
                                }
                            }
                            if (contain) {
                                craneList.add(cranes);
                            }
                        }
                    } else { // 2、取连续的符合craneNum数目的桥机
                        craneList.add(cranes);
                    }
                }
            }
        }
        return craneList;
    }

    private boolean isRightVessel(VesselData rightVesselData, VesselData vesselData) {
        return rightVesselData != null && rightVesselData.getVmSchedule().getPlanStartPst().compareTo(vesselData.getVmSchedule().getPlanStartPst()) > 0;
    }

    private double getCurMinCraneNum(long curTime, Long curRemainAmount, VesselData vesselData) {
        Long vesselTime = vesselData.getVmSchedule().getPlanEndWorkTime().getTime() / 1000 - curTime;
        return CalculateUtil.div(curRemainAmount.doubleValue(), vesselTime.doubleValue(), 2);
    }

    private List<long[][]> copyTimeCraneList(List<long[][]> availableTimeCraneList) {
        List<long[][]> timeCraneList = new ArrayList<>();
        for (long[][] timeCrane : availableTimeCraneList) {
            long[][] temp = copyTimeCrane(timeCrane);
            timeCraneList.add(temp);
        }
        return timeCraneList;
    }

    private long[][] copyTimeCrane(long[][] timeCrane) {
        long[][] temp = new long[timeCrane.length][timeCrane[timeCrane.length - 1].length];
        for (int t = 0; t < timeCrane.length; t++) {
            System.arraycopy(timeCrane[t], 0, temp[t], 0, timeCrane[t].length);
        }
        return temp;
    }
}
