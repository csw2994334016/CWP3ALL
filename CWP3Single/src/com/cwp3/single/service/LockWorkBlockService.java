package com.cwp3.single.service;

import com.cwp3.data.AllRuntimeData;

/**
 * Created by csw on 2019/09/18.
 * Description:
 */
public interface LockWorkBlockService {

    void analyzeLockWorkBlock(AllRuntimeData allRuntimeData, Long berthId);
}
