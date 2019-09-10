package com.cwp3.ioservice.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.ioservice.CwpDoService;
import com.cwp3.ioservice.ParseDataService;
import com.cwp3.ioservice.ResultGeneratorService;
import com.cwp3.multiple.service.MultipleService;
import com.cwp3.multiple.service.impl.MultipleServiceImpl;
import com.cwp3.single.service.CwpService;
import com.cwp3.single.service.impl.CwpServiceImpl;
import com.shbtos.biz.smart.cwp.service.SmartCraneAllocationResults;
import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;
import com.shbtos.biz.smart.cwp.service.SmartCwpResults;

/**
 * Created by csw on 2018/7/16.
 * Description:
 */
public class CwpDoServiceImpl implements CwpDoService {

    private ParseDataService parseDataService;

    private CwpService cwpService;
    private MultipleService multipleService;

    private ResultGeneratorService resultGeneratorService;

    public CwpDoServiceImpl() {
        parseDataService = new ParseDataServiceImpl();
        cwpService = new CwpServiceImpl();
        multipleService = new MultipleServiceImpl();
        resultGeneratorService = new ResultGeneratorServiceImpl();
    }

    @Override
    public void doPlanCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwp3Results) {
        AllRuntimeData allRuntimeData = parseDataService.parseAllRuntimeData(smartCwpImportData);
        for (Long berthId : allRuntimeData.getAllBerthId()) {
            cwpService.doPlanCwp(allRuntimeData, berthId);
            resultGeneratorService.generateCwpResult(allRuntimeData.getWorkingDataByBerthId(berthId), smartCwp3Results);
            saveSmartReMessageInfo(berthId, allRuntimeData, smartCwp3Results);
        }
    }

    @Override
    public void doWorkCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwp3Results) {
        AllRuntimeData allRuntimeData = parseDataService.parseAllRuntimeData(smartCwpImportData);
        for (Long berthId : allRuntimeData.getAllBerthId()) {
            cwpService.doWorkCwp(allRuntimeData, berthId);
            resultGeneratorService.generateCwpResult(allRuntimeData.getWorkingDataByBerthId(berthId), smartCwp3Results);
            saveSmartReMessageInfo(berthId, allRuntimeData, smartCwp3Results);
        }
    }

    public static void saveSmartReMessageInfo(Long berthId, AllRuntimeData allRuntimeData, SmartCwp3Results smartCwp3Results) {
        smartCwp3Results.getSmartReMessageInfo().setCwpVersion(CWPDefaultValue.CWP_VERSION);
        smartCwp3Results.getSmartReMessageInfo().putErrorLog(berthId, allRuntimeData.getLogger().getError());
        smartCwp3Results.getSmartReMessageInfo().putExecuteLog(berthId, allRuntimeData.getLogger().getInfo());
        smartCwp3Results.getSmartReMessageInfo().putErrorLog(berthId, allRuntimeData.getWorkingDataByBerthId(berthId).getLogger().getError());
        smartCwp3Results.getSmartReMessageInfo().putExecuteLog(berthId, allRuntimeData.getWorkingDataByBerthId(berthId).getLogger().getInfo());
    }

    @Override
    public void doOldCwpResult(SmartCwp3Results smartCwp3Results, SmartCwpResults smartCwpResults) {
        resultGeneratorService.generateOldCwpResult(smartCwp3Results, smartCwpResults);
    }

    @Override
    public void doCraneAllocation(SmartCwpImportData smartCwpImportData, SmartCraneAllocationResults smartCraneAllocationResults) {
        AllRuntimeData allRuntimeData = parseDataService.parseAllRuntimeDataByCraneAllocation(smartCwpImportData);
        multipleService.doCraneAllocation(allRuntimeData);
        resultGeneratorService.generateCraneAllocationResult(allRuntimeData, smartCraneAllocationResults);
        smartCraneAllocationResults.getSmartReMessageInfo().setCwpVersion(CWPDefaultValue.CRANE_ALLOCATION_VERSION);
        smartCraneAllocationResults.getSmartReMessageInfo().setErrorLog(allRuntimeData.getLogger().getError());
        smartCraneAllocationResults.getSmartReMessageInfo().setExecuteLog(allRuntimeData.getLogger().getInfo());
    }

    @Override
    public void doMultipleCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwp3Results) {
        AllRuntimeData allRuntimeData = parseDataService.parseAllRuntimeDataByMultiCwp(smartCwpImportData);
        multipleService.doMultipleCwp(allRuntimeData);
        resultGeneratorService.generateMultipleCwpResult(allRuntimeData, smartCwp3Results);
        smartCwp3Results.getSmartReMessageInfo().setCwpVersion(CWPDefaultValue.MULTIPLE_CWP_VERSION);
        smartCwp3Results.getSmartReMessageInfo().setErrorLog(allRuntimeData.getLogger().getError());
        smartCwp3Results.getSmartReMessageInfo().setExecuteLog(allRuntimeData.getLogger().getInfo());
    }

}
