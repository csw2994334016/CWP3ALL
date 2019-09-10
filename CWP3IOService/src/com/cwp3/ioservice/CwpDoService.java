package com.cwp3.ioservice;

import com.shbtos.biz.smart.cwp.service.SmartCraneAllocationResults;
import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;
import com.shbtos.biz.smart.cwp.service.SmartCwpResults;

/**
 * Created by csw on 2018/7/16.
 * Description:
 */
public interface CwpDoService {

    void doPlanCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwpResults);

    void doWorkCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwp3Results);

    void doOldCwpResult(SmartCwp3Results smartCwp3Results, SmartCwpResults smartCwpResults);

    void doCraneAllocation(SmartCwpImportData smartCwpImportData, SmartCraneAllocationResults smartCraneAllocationResults);

    void doMultipleCwp(SmartCwpImportData smartCwpImportData, SmartCwp3Results smartCwp3Results);
}
