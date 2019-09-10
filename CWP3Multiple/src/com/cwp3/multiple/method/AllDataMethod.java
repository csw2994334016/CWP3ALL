package com.cwp3.multiple.method;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.crane.CMCrane;
import com.cwp3.model.log.Logger;
import com.cwp3.model.vessel.VMContainerAmount;
import com.cwp3.model.work.CranePlan;
import com.cwp3.multiple.data.AllData;
import com.cwp3.multiple.data.VesselData;
import com.cwp3.multiple.model.Crane;
import com.cwp3.multiple.model.DefaultValue;
import com.cwp3.multiple.model.Schedule;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by csw on 2018/11/12.
 * Description:
 */
public class AllDataMethod {

    private AllRuntimeData allRuntimeData;

    public AllDataMethod(AllRuntimeData allRuntimeData) {
        this.allRuntimeData = allRuntimeData;
    }

    public Logger getLogger() {
        return allRuntimeData.getLogger();
    }

    public AllData initAllData() {
        AllData allData = new AllData(allRuntimeData);
        Logger logger = allRuntimeData.getLogger();
        try {
            logger.logInfo("The AllData is being initialized.");
            // 获取allRuntimeData里面的桥机信息
            List<CMCrane> cmCraneList = allData.getMachineData().getAllCMCraneList();
            Collections.sort(cmCraneList, new Comparator<CMCrane>() {
                @Override
                public int compare(CMCrane o1, CMCrane o2) {
                    return o1.getCraneSeq().compareTo(o2.getCraneSeq());
                }
            });
            for (CMCrane cmCrane : cmCraneList) {
                Crane crane = new Crane(cmCrane);
                allData.addCrane(crane);
            }
            // 从allRuntimeData里面获取数据，并组装singleData
            for (WorkingData workingData : allRuntimeData.getAllWorkingDataList()) {
                Schedule schedule = new Schedule(workingData.getVmSchedule());
                VesselData vesselData = new VesselData(schedule);
                //进出口船图信息装载
                long load2Amount = 0;
                long load4Amount = 0;
                long disc2Amount = 0;
                long disc4Amount = 0;
                List<VMContainerAmount> vmContainerAmountList = workingData.getVmContainerAmountList();
                for (VMContainerAmount vmContainerAmount : vmContainerAmountList) {
                    VMContainerAmount vmContainerAmountCopy = new VMContainerAmount(vmContainerAmount.getBerthId(), vmContainerAmount.getDlType(), vmContainerAmount.getSize(), vmContainerAmount.getContainerAmount());
                    vesselData.getVmContainerAmountList().add(vmContainerAmountCopy);
                    if (CWPDomain.DL_TYPE_DISC.equals(vmContainerAmount.getDlType())) {
                        if (vmContainerAmount.getSize().startsWith("2")) {
                            disc2Amount += vmContainerAmount.getContainerAmount();
                        } else {
                            disc4Amount += vmContainerAmount.getContainerAmount();
                        }
                    } else {
                        if (vmContainerAmount.getSize().startsWith("2")) {
                            load2Amount += vmContainerAmount.getContainerAmount();
                        } else {
                            load4Amount += vmContainerAmount.getContainerAmount();
                        }
                    }
                }
                Long allAmount = (disc2Amount + load2Amount) * CWPDefaultValue.oneCntWorkTime / 2 + (disc4Amount + load4Amount) * CWPDefaultValue.oneCntWorkTime;
                vesselData.setDpRemainAmount(allAmount);
                vesselData.setAllAmount(allAmount);
                allData.addVesselData(vesselData);
            }
        } catch (Exception e) {
            logger.logError("初始化算法数据(AllData)过程中发生异常！");
            e.printStackTrace();
        }
        return allData;
    }

    public void generateResult(long[][] timeCrane, AllData allData) {
        allRuntimeData.putStorage("allData", allData); // todo: 为画资源策划的结果图表，暂时这样处理
        long startTime = allData.getStartTime();
        List<Crane> cranes = allData.getAllCranes();
        List<VesselData> vesselDataList = allData.getAllVesselDataList();
        for (VesselData vesselData : vesselDataList) {
            for (int c = 0; c < timeCrane[0].length; c++) {
                Crane crane = cranes.get(c);
                long min = Long.MAX_VALUE;
                long max = Long.MIN_VALUE;
                for (int t = 0; t < timeCrane[c].length; t++) {
                    if (timeCrane[t][c] == vesselData.getBerthId()) {
                        long time = startTime + t * DefaultValue.timeSpan;
                        min = time < min ? time : min;
                        max = time > max ? time : max;
                    }
                }
                if (min < Long.MAX_VALUE && max > Long.MIN_VALUE) {
                    CranePlan cranePlan = new CranePlan();
                    cranePlan.setBerthId(vesselData.getBerthId());
                    cranePlan.setCraneNo(crane.getCraneNo());
                    cranePlan.setWorkingStartTime(new Date(min * 1000));
                    cranePlan.setWorkingEndTime(new Date(max * 1000));
                    allRuntimeData.getCranePlanList().add(cranePlan);
                }
            }
        }
    }
}
