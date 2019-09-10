package com.cwp3.single.algorithm.move;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.calculator.Calculator;
import com.cwp3.single.data.MoveData;
import com.cwp3.single.data.MoveResults;
import com.cwp3.single.method.MoveDataMethod;

import java.util.*;

/**
 * Created by csw on 2018/5/31.
 * Description:
 */
public class MoveCalculator {

    public void calculateAvailableMove(Long hatchId, MoveData moveData, MoveResults moveResults, WorkingData workingData, StructureData structureData) {
        List<Integer> bayNos = structureData.getVMHatchByHatchId(hatchId).getAllBayNos();
        MoveDataMethod moveDataMethod = new MoveDataMethod();
        for (Integer bayNo : bayNos) { //按倍位计算可作业量
            String curWorkFlow1 = moveData.getCurWorkFlowMap1().get(bayNo);
            Calculator calculator = new Calculator();
            moveResults.clearAvailableMoveByBayNo(bayNo);
            long order = moveData.getCurMoveOrder(hatchId);
            List<WorkMove> workMoveList = moveData.getCurTopMoveListByHatchIdAndBayNo(hatchId, bayNo);
            Map<Integer, WorkMove> rowNoMoveMap = curTopMoveListToMap(workMoveList); //顶层Map，主要对这个Map进行遍历查找最合适作业的move
            List<WorkMove> workMoveFirstList = new ArrayList<>();
            while (rowNoMoveMap.size() > 0) {
                long st = System.currentTimeMillis();
                WorkMove workMoveFirst = calculator.findFirstWorkMove(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
                long et = System.currentTimeMillis();
                if (workMoveFirst != null) {
                    //作为可作业量进行编序
                    workMoveFirst.setMoveOrder(order++);
                    workMoveFirstList.add(workMoveFirst);
                    //深复制move，不然对其它倍位的判断有问题
                    WorkMove workMoveFirstCopy = workMoveFirst.baseCopy();
                    workMoveFirstCopy.setMoveOrder(workMoveFirst.getMoveOrder());
                    Integer hcSeq = workMoveFirstCopy.getHcSeq(); // 一般情况，生成move的时候，会计算hcSeq的值
                    if (workMoveFirstCopy.getHcSeq() == null) {
                        hcSeq = workingData.getHcSeqByWorkMove(hatchId, workMoveFirstCopy); //分档序号
                    }
                    moveResults.addAvailableMove(bayNo, hcSeq, workMoveFirstCopy);
                    rowNoMoveMap.remove(workMoveFirst.getRowNo()); //从顶层Map中去掉计算过的move
                    // 如果move对象是舱盖板，则需要在MoveData中设置作业舱盖板的吊具类型
                    if (CWPDomain.MOVE_TYPE_HC.equals(workMoveFirst.getMoveType())) {
                        moveData.setCurWorkFlow1(bayNo, workMoveFirst.getHcWorkFlow());
                    }
                    long st1 = System.currentTimeMillis();
                    Set<WorkMove> workMoveNextSet = moveDataMethod.getNextTopWorkMoveSet(workMoveFirst, moveData, structureData, workingData); // 查找下一个顶层
                    long et1 = System.currentTimeMillis();
//                    System.out.println("得到第一个move的时间：" + (et - st) + "ms，之后获取顶层的时间：" + (et1 - st1) + "ms");
                    for (WorkMove workMoveNext : workMoveNextSet) {
                        rowNoMoveMap.put(workMoveNext.getRowNo(), workMoveNext); //将下一个顶层放到顶层Map中
                    }
                } else { // 根据编序策略无法找到合适作业的WorkMove
                    workingData.getLogger().logError("根据编序策略无法找到倍位(" + bayNo + ")合适作业的WorkMove，order：" + order);
                    break;
                }
            }
            // 已经从MoveData中复制出move放入MoveResults了，所以需要对MoveData中的move进行还原
            for (WorkMove workMove : workMoveFirstList) {
                workMove.setMoveOrder(null);
            }
            // 还原MoveData中设置的舱盖板作业吊具类型
            moveData.setCurWorkFlow1(bayNo, curWorkFlow1);
        }
    }

    private Map<Integer, WorkMove> curTopMoveListToMap(List<WorkMove> workMoveList) {
        Map<Integer, WorkMove> rowNoMoveMap = new LinkedHashMap<>();
        for (WorkMove workMove : workMoveList) {
            rowNoMoveMap.put(workMove.getRowNo(), workMove);
        }
        return rowNoMoveMap;
    }

    public void calculateTotalMove(Long hatchId, MoveData moveData, MoveResults moveResults, StructureData structureData) {
        for (Integer bayNo : structureData.getVMHatchByHatchId(hatchId).getAllBayNos()) {
            moveResults.clearCurrentTotalMoveByBayNo(bayNo);
            List<WorkMove> workMoveList = moveData.getTotalMoveListByBayNo(bayNo);
            Set<WorkMove> workMoveSet = new HashSet<>(workMoveList);
            for (WorkMove workMove : workMoveSet) {
                moveResults.addCurrentTotalMove(bayNo, workMove.getHcSeq(), workMove);
            }
        }
    }
}
