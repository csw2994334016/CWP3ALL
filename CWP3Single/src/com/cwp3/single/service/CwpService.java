package com.cwp3.single.service;

import com.cwp3.data.AllRuntimeData;

/**
 * Created by csw on 2018/5/29.
 * Description:
 */
public interface CwpService {

    void doPlanCwp(AllRuntimeData allRuntimeData, Long berthId);

    void doWorkCwp(AllRuntimeData allRuntimeData, Long berthId);

    void doMultipleCwp(AllRuntimeData allRuntimeData, Long berthId);

}
