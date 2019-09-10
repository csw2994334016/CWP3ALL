package com.cwp3.ioservice;
import com.cwp3.data.*;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;


public interface ParseDataService {

    AllRuntimeData parseAllRuntimeData(SmartCwpImportData smartCwpImportData);

    AllRuntimeData parseAllRuntimeDataByCraneAllocation(SmartCwpImportData smartCwpImportData);

    AllRuntimeData parseAllRuntimeDataByMultiCwp(SmartCwpImportData smartCwpImportData);
}
