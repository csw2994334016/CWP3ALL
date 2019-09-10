package com.cwp3.multiple.algorithm.process;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.log.Logger;
import com.cwp3.multiple.algorithm.analyzer.VesselDataAnalyzer;
import com.cwp3.multiple.data.AllData;
import com.cwp3.multiple.data.VesselData;
import com.cwp3.multiple.method.AllDataMethod;

import com.cwp3.single.service.CwpService;
import com.cwp3.single.service.impl.CwpServiceImpl;

import java.util.List;

/**
 * Created by csw on 2018/11/12.
 * Description:
 */
public class MultipleCwpProcess {

    private AllDataMethod allDataMethod;
    private VesselDataAnalyzer vesselDataAnalyzer;

    private CwpService cwpService;

    public MultipleCwpProcess(AllRuntimeData allRuntimeData) {
        allDataMethod = new AllDataMethod(allRuntimeData);
        vesselDataAnalyzer = new VesselDataAnalyzer();
        cwpService = new CwpServiceImpl();
    }

    public void processMultipleCwp() {
        Logger logger = allDataMethod.getLogger();
        logger.logInfo("开始执行...");
        long st = System.currentTimeMillis();

        // 初始化数据
        AllData allData = allDataMethod.initAllData();

        // 船舶优先级分析与排序
        List<VesselData> vesselDataList = allData.getAllVesselDataList();
        vesselDataAnalyzer.sortVesselDataByPriority(vesselDataList);

        // todo: 桥机资源池分配合理性验证，及桥机池调整

        // 依次调用单船CWP算法
        for (VesselData vesselData : vesselDataList) {
            if (CWPDomain.YES.equals(vesselData.getVmSchedule().getDoCwpFlag())) {
                if (CWPDomain.YES.equals(vesselData.getVmSchedule().getStartWorkFlag())) {
                    cwpService.doWorkCwp(allData.getAllRuntimeData(), vesselData.getBerthId());
                } else {
                    cwpService.doPlanCwp(allData.getAllRuntimeData(), vesselData.getBerthId());
                }
            }
        }

        long et = System.currentTimeMillis();
        logger.logInfo("执行结束，执行时间是：" + (et - st) / 1000 + "秒");
    }
}
