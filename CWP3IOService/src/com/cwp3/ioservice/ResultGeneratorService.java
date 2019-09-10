package com.cwp3.ioservice;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.single.data.MoveData;
import com.shbtos.biz.smart.cwp.service.SmartCraneAllocationResults;
import com.shbtos.biz.smart.cwp.service.SmartCwp3Results;
import com.shbtos.biz.smart.cwp.service.SmartCwpResults;

/**
 * Created by csw on 2018/6/7.
 * Description:
 */
public interface ResultGeneratorService {

    void generateMoveResult(MoveData moveData, WorkingData workingData);

    void generateCwpResult(WorkingData workingData, SmartCwp3Results smartCwpResults);

    void generateOldCwpResult(SmartCwp3Results smartCwp3Results, SmartCwpResults smartCwpResults);

    void generateCraneAllocationResult(AllRuntimeData allRuntimeData, SmartCraneAllocationResults smartCraneAllocationResults);

    void generateMultipleCwpResult(AllRuntimeData allRuntimeData, SmartCwp3Results smartCwp3Results);
}
