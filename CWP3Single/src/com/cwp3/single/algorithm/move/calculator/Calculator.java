package com.cwp3.single.algorithm.move.calculator;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.calculator.analysis.Analyzer;
import com.cwp3.single.algorithm.move.calculator.strategy.Strategy;
import com.cwp3.single.data.MoveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/9/4.
 * Description:
 */
public class Calculator {

    private String curWorkFlow; //当前作业工艺（上一次作业工艺）
    private List<WorkMove> delayWorkMoveList; //需要延后编序的箱子

    public Calculator() {
        delayWorkMoveList = new ArrayList<>();
    }

    public WorkMove findFirstWorkMove(Long hatchId, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData) {

        //当前装卸策略：卸船策略、装船策略、边装边卸策略
        Strategy strategy = Analyzer.getCurStrategy(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);

        //当前作业工艺，关键点：1、要按倍位上次作业的工艺继续编序；2、第一次编序时选择什么样的工艺（根据船图箱量分布情况计算）
        curWorkFlow = strategy.getCurWorkFlow(curWorkFlow);

        //当前作业工艺下作业的层号，保证一层一层作业
        int curTierNo = strategy.getCurWorkTierNo(curWorkFlow, delayWorkMoveList);

        //根据curWorkFlow、curTierNo、delayWorkMoveList返回当前最合适编序的WorkMove
        return strategy.getFirstWorkMove(curWorkFlow, curTierNo, delayWorkMoveList);

    }

}
