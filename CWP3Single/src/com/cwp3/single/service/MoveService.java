package com.cwp3.single.service;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.single.data.MoveData;

public interface MoveService {

    void makeWorkFlow(AllRuntimeData allRuntimeData, Long berthId);

    void calculateMoves(AllRuntimeData allRuntimeData, Long berthId, MoveData moveData);
}
