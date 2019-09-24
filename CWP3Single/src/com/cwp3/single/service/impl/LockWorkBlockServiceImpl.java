package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.single.service.LockWorkBlockService;
import com.cwp3.utils.StringUtil;

import java.util.*;

/**
 * Created by csw on 2019/09/18.
 * Description:
 */
public class LockWorkBlockServiceImpl implements LockWorkBlockService {

    @Override
    public void analyzeLockWorkBlock(AllRuntimeData allRuntimeData, Long berthId) {
        WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
        Set<VMContainer> vmContainerSet = new HashSet<>(workingData.getDiscContainerMap().values());
        vmContainerSet.addAll(workingData.getLoadContainerMap().values());
        for (Map.Entry<Long, List<WorkBlock>> entry : workingData.getLockHatchWorkBlockMap().entrySet()) {
            // 将vmContainer按作业顺序、hatchId，成对形成move
            Map<Long, List<VMContainer>> moveCntListMap = new HashMap<>();
            for (VMContainer vmContainer : vmContainerSet) {
                boolean condition = vmContainer.getCwoManualSeqNoTemp() != null && StringUtil.isNotBlank(vmContainer.getCwoManualWorkflowTemp());
                if (vmContainer.getHatchId().equals(entry.getKey()) && condition) {
                    if (moveCntListMap.get(vmContainer.getCwoManualSeqNoTemp()) == null) {
                        moveCntListMap.put(vmContainer.getCwoManualSeqNoTemp(), new ArrayList<VMContainer>());
                    }
                    moveCntListMap.get(vmContainer.getCwoManualSeqNoTemp()).add(vmContainer);
                }
            }
            // moveCntListMap按key从小到大排序
            List<Long> moveOrderList = new ArrayList<>(moveCntListMap.keySet());
            Collections.sort(moveOrderList);
            Map<Long, List<VMContainer>> sortMoveCntListMap = new LinkedHashMap<>();
            for (Long moveOrder : moveOrderList) {
                sortMoveCntListMap.put(moveOrder, moveCntListMap.get(moveOrder));
            }
            // 该舱锁定的作业块按舱序排序，按计划量人工锁定箱子的作业顺序和作业工艺
            List<WorkBlock> workBlockList = new ArrayList<>(entry.getValue());
            Collections.sort(workBlockList, new Comparator<WorkBlock>() {
                @Override
                public int compare(WorkBlock o1, WorkBlock o2) {
                    return o1.getHatchSeq().compareTo(o2.getHatchSeq());
                }
            });
            int amount = 0;
            for (WorkBlock workBlock : workBlockList) {
                amount += workBlock.getPlanAmount();
            }
            for (int i = 0; i < amount; i++) {
                if (i < moveOrderList.size()) {
                    for (VMContainer vmContainer : sortMoveCntListMap.get(moveOrderList.get(i))) {
                        vmContainer.setWorkFlow(PublicMethod.getWorkFlowStr1(vmContainer.getCwoManualWorkflowTemp(), vmContainer.getSize()));
                        vmContainer.setCwoManualWorkflow("Y");
                        vmContainer.setMoveOrder(vmContainer.getCwoManualSeqNoTemp());
                        vmContainer.setWorkFirst(CWPDomain.YES);
                    }
                }
            }
        }
    }
}
