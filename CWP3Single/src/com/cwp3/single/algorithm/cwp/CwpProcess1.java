package com.cwp3.single.algorithm.cwp;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.log.Logger;
import com.cwp3.single.algorithm.cwp.decision.*;
import com.cwp3.single.algorithm.cwp.method.CraneMethod;
import com.cwp3.single.algorithm.cwp.method.LogPrintMethod;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;
import com.cwp3.single.method.CwpDataMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by csw on 2018/5/29.
 * Description:
 */
public class CwpProcess1 {

    private CwpDataMethod cwpDataMethod;
    private Analyzer analyzer;
    private Evaluator evaluator;
    private Dynamic dynamic;
    private ExecutorService executorService;
    private int threadNums;

    public CwpProcess1() {
        cwpDataMethod = new CwpDataMethod();
        analyzer = new Analyzer();
        evaluator = new Evaluator();
        dynamic = new Dynamic();
        threadNums = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(threadNums);
    }

    public void processCwp(AllRuntimeData allRuntimeData, Long berthId) {
        Logger logger = allRuntimeData.getWorkingDataByBerthId(berthId).getLogger();
        logger.logInfo("CWP algorithm is starting...");
        if (CWPDomain.YES.equals(allRuntimeData.getWorkingDataByBerthId(berthId).getCwpConfig().getMultiThreadFlag())) {
            logger.logInfo("Running algorithm using multithreading, the number of thread is: " + threadNums);
        }
        long st = System.currentTimeMillis();

        CwpData cwpData = cwpDataMethod.initCwpData(berthId, allRuntimeData);

        List<CwpData> cwpDataList = new ArrayList<>();
        startSearch(cwpData, cwpDataList);

        List<CwpData> cwpDataNewList = evaluator.getCorrectResult(cwpDataList);
        if (cwpDataNewList.size() == 0) {
            cwpData.getWorkingData().getLogger().logError("无法得到满足船期条件的最佳结果！");
            return;
        }

        List<CwpData> cwpDataResultList = evaluator.getBestResult(cwpDataNewList);

        cwpDataMethod.generateResult(cwpDataResultList);

        long et = System.currentTimeMillis();
        cwpData.getWorkingData().getLogger().logInfo("CWP algorithm finished. The running time of algorithm is " + (et - st) / 1000 + "s");
    }

    private void startSearch(CwpData cwpData, List<CwpData> cwpDataList) {
        Logger logger = cwpData.getWorkingData().getLogger();

        cwpDataMethod.computeCurrentWorkTime(cwpData.getDpResult(), cwpData);

        analyzer.firstAnalyzeCwpBay(cwpData);
        LogPrintMethod.printCurBayWorkTime(cwpData.getAllCWPBays(), logger);

        if (!finish(1, cwpData)) {
            cwpDataList.add(cwpData);
            return;
        }
        if (cwpData.getAllCWPCranes().size() == 0 || cwpData.getAllCWPBays().size() == 0) {
            return;
        }

        analyzer.firstAnalyzeCwpCrane(cwpData);

        List<DPBranch> dpBranchList = evaluator.getFirstDpBranchList(cwpData);

        for (int i = 0; i < dpBranchList.size(); i++) {
            logger.logInfo("The first search，branch(" + i + "):====================================");
            CwpData cwpDataCopy = cwpDataMethod.copyCwpData(cwpData);
            setDpBranch(dpBranchList.get(i), cwpDataCopy);

            LogPrintMethod.printSelectedCrane(cwpDataCopy.getDpCwpCraneList(), logger);
            LogPrintMethod.printCraneSelectBayInfo(cwpDataCopy, logger);
            DPResult dpResult = dynamic.cwpKernel(cwpDataCopy.getDpCwpCraneList(), cwpDataCopy.getAllCWPBays(), cwpDataCopy);

            long minWorkTime = CraneMethod.obtainMinWorkTime(dpResult, cwpDataCopy);

            realWork(dpResult, minWorkTime, cwpDataCopy, cwpDataMethod);

            cwpDataCopy.setDpResult(dpResult);

            cwpDataList.add(cwpDataCopy);

            if (CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getMultiThreadFlag())) {
                Search search = new Search(cwpDataCopy, dpResult, 2);
                executorService.submit(search);
            } else {
                singleSearch(cwpDataCopy, dpResult, 2, cwpDataMethod, analyzer, evaluator, dynamic);
            }
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) { // time out, threads are forced to closed.
                logger.logInfo("多线程没有运行完成，程序非正常关闭！");
                executorService.shutdownNow();
            }
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }
    }

    class Search implements Runnable {

        private CwpData cwpData;
        private DPResult dpResult;
        private int depth;

        private CwpDataMethod cwpDataMethod;
        private Analyzer analyzer;
        private Evaluator evaluator;
        private Dynamic dynamic;

        Search(CwpData cwpData, DPResult dpResult, int depth) {
            this.cwpData = cwpData;
            this.dpResult = dpResult;
            this.depth = depth;
            cwpDataMethod = new CwpDataMethod();
            analyzer = new Analyzer();
            evaluator = new Evaluator();
            dynamic = new Dynamic();
        }

        @Override
        public void run() {
            try {
                singleSearch(cwpData, dpResult, depth, cwpDataMethod, analyzer, evaluator, dynamic);
//                multipleSearch(cwpData, dpResult, depth, cwpDataMethod, analyzer, evaluator, dynamic);
            } catch (Exception e) {
                e.printStackTrace();
                cwpData.setDpExceptionBranch(CWPDomain.EXCEPTION_BRANCH);
                cwpData.getWorkingData().getLogger().logError("多线程CWP算法在执行过程中发生未知异常！");
            }
        }
    }

    private void multipleSearch(CwpData cwpData, DPResult dpResult, int depth, CwpDataMethod cwpDataMethod, Analyzer analyzer, Evaluator evaluator, Dynamic dynamic) {
        Logger logger = cwpData.getWorkingData().getLogger();
        logger.logDebug("第" + depth + "次search:--------------------------");

        cwpDataMethod.computeCurrentWorkTime(dpResult, cwpData);

        analyzer.analyzeCwpBay(cwpData);
        LogPrintMethod.printCurBayWorkTime(cwpData.getAllCWPBays(), logger);

        if (!finish(depth, cwpData)) {
            return;
        }

        analyzer.analyzeCwpCrane(cwpData);
        LogPrintMethod.printSelectedCrane(cwpData.getDpCwpCraneList(), logger);

        if (evaluator.invalidBranch(cwpData)) {
            return;
        }

        // 汇总多分支深度为5的结果，选择最优的结果进入下一次递归
        List<CwpData> cwpDataList = new ArrayList<>();
        List<DPBranch> dpBranchList = evaluator.getCurDpBranchList(cwpData);
        for (DPBranch dpBranch : dpBranchList) {
            CwpData cwpDataCopy = dpBranchList.size() == 1 ? cwpData : cwpDataMethod.copyCwpData(cwpData);
            setDpBranch(dpBranch, cwpDataCopy);
            cwpDataList.add(cwpDataCopy);

            LogPrintMethod.printCraneSelectBayInfo(cwpDataCopy, logger);
            DPResult dpResultNew = dynamic.cwpKernel(cwpDataCopy.getDpCwpCraneList(), cwpDataCopy.getAllCWPBays(), cwpDataCopy);

            long minWorkTime = CraneMethod.obtainMinWorkTime(dpResultNew, cwpDataCopy);

            if (minWorkTime > 0) { // todo: 如果minWorkTime==0，则断定为出现了加减桥机信息，继续下次决策
                long realWorkTime = realWork(dpResultNew, minWorkTime, cwpDataCopy, cwpDataMethod);
                if (!cwpDataCopy.getFirstDoCwp() && realWorkTime == 0) { // todo: 非第一次作业、且没有进行实际作业，断定为异常的决策，结束分支？
                    cwpDataCopy.setDpInvalidateBranch(CWPDomain.EXCEPTION_BRANCH);
                    return;
                }
            }

            setDpResult(dpResultNew, cwpDataCopy);
            subSearch(cwpDataCopy, dpResultNew, 1, cwpDataMethod, analyzer, evaluator, dynamic);
        }

        CwpData cwpDataBest = evaluator.getDepthBestResult(cwpDataList);
        setCwpData(cwpDataBest, cwpData);

        multipleSearch(cwpData, cwpData.getDpResult(), depth + 1, cwpDataMethod, analyzer, evaluator, dynamic);
    }

    private void subSearch(CwpData cwpData, DPResult dpResult, int depth, CwpDataMethod cwpDataMethod, Analyzer analyzer, Evaluator evaluator, Dynamic dynamic) {
        Logger logger = cwpData.getWorkingData().getLogger();

        if (cwpData.getDpChangeHatchNumber() == 1) { // 深度为5
            logger.logDebug("");
            return;
        }
        logger.logDebug("——" + depth + ":------------");

        DPResult dpResultNew = search(cwpData, dpResult, depth, cwpDataMethod, analyzer, evaluator, dynamic);
        if (dpResultNew == null) {
            return;
        }

        subSearch(cwpData, dpResultNew, depth + 1, cwpDataMethod, analyzer, evaluator, dynamic);
    }

    private void singleSearch(CwpData cwpData, DPResult dpResult, int depth, CwpDataMethod cwpDataMethod, Analyzer analyzer, Evaluator evaluator, Dynamic dynamic) {
        Logger logger = cwpData.getWorkingData().getLogger();
        logger.logDebug("第" + depth + "次search:--------------------------");

        DPResult dpResultNew = search(cwpData, dpResult, depth, cwpDataMethod, analyzer, evaluator, dynamic);
        if (dpResultNew == null) {
            return;
        }

        singleSearch(cwpData, dpResultNew, depth + 1, cwpDataMethod, analyzer, evaluator, dynamic);
    }

    private DPResult search(CwpData cwpData, DPResult dpResult, int depth, CwpDataMethod cwpDataMethod, Analyzer analyzer, Evaluator evaluator, Dynamic dynamic) {
        Logger logger = cwpData.getWorkingData().getLogger();

        cwpDataMethod.computeCurrentWorkTime(dpResult, cwpData);

        analyzer.analyzeCwpBay(cwpData);
        LogPrintMethod.printCurBayWorkTime(cwpData.getAllCWPBays(), logger);

        if (!finish(depth, cwpData)) {
            return null;
        }

        analyzer.analyzeCwpCrane(cwpData);
        LogPrintMethod.printSelectedCrane(cwpData.getDpCwpCraneList(), logger);

        if (evaluator.invalidBranch(cwpData)) {
            return null;
        }

        DPBranch dpBranch = evaluator.getCurDpBranch(cwpData);
        cwpData.setDpCraneSelectBays(dpBranch.getDpCraneSelectBays());

        LogPrintMethod.printCraneSelectBayInfo(cwpData, logger);
        DPResult dpResultNew = dynamic.cwpKernel(cwpData.getDpCwpCraneList(), cwpData.getAllCWPBays(), cwpData);

        long minWorkTime = CraneMethod.obtainMinWorkTime(dpResultNew, cwpData);

        if (minWorkTime > 0) { // todo: 如果minWorkTime==0，则断定为出现了加减桥机信息，继续下次决策
            long realWorkTime = realWork(dpResultNew, minWorkTime, cwpData, cwpDataMethod);
            if (!cwpData.getFirstDoCwp() && realWorkTime == 0) { // todo: 非第一次作业、且没有进行实际作业，则断定为异常的决策，结束分支？
                cwpData.setDpInvalidateBranch(CWPDomain.EXCEPTION_BRANCH);
                return null;
            }
        }

        setDpResult(dpResultNew, cwpData);

        return dpResultNew;
    }

    private void setDpBranch(DPBranch dpBranch, CwpData cwpDataCopy) {
        cwpDataCopy.setDpStrategyType(dpBranch.getDpStrategyType());
        cwpDataCopy.setDpCwpCraneList(dpBranch.getDpCwpCraneList());
        cwpDataCopy.setDpCraneSelectBays(dpBranch.getDpCraneSelectBays());
    }

    private void setCwpData(CwpData cwpDataBest, CwpData cwpData) {
        cwpData.setCwpStartTime(cwpDataBest.getCwpStartTime());
        cwpData.setMoveData(cwpDataBest.getMoveData());
        cwpData.setMoveResults(cwpDataBest.getMoveResults());
        cwpData.setFirstDoCwp(cwpDataBest.getFirstDoCwp());
        cwpData.setDpCurrentTime(cwpDataBest.getDpCurrentTime());
        cwpData.setDpResult(cwpDataBest.getDpResult());
        cwpData.setDpCwpCraneList(cwpDataBest.getDpCwpCraneList());
        cwpData.setDpMoveNumber(cwpDataBest.getDpMoveNumber());
        cwpData.setDpInvalidateBranch(cwpDataBest.getDpInvalidateBranch());
        cwpData.setDpExceptionBranch(cwpDataBest.getDpExceptionBranch());
        cwpData.setDpChangeHatchNumber(0);
    }

    private void setDpResult(DPResult dpResultNew, CwpData cwpData) {
        DPResult curDpResult = dpResultNew.deepCopy();
        List<DPPair> traceBackList = new ArrayList<>();
        List<DPPair> cranePositionList = new ArrayList<>();
        for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
            String craneNo = cwpBay.getDpSelectedByCraneNo();
            if (craneNo != null && !CWPDomain.YES.equals(cwpBay.getDpSelectedByCraneTrue())) {
                Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(craneNo, cwpData.getDpResult());
                if (bayNoLast != null) { // 桥机必须选择该倍位作业，则要以上次决策的倍位作为这次的决策结果（因为一般情况下桥机还要回去作业）
                    DPPair dpPair = new DPPair<>(craneNo, bayNoLast);
                    traceBackList.add(dpPair);
                    cranePositionList.add(dpPair);
                }
            }
        }
        for (CWPCrane cwpCrane : cwpData.getDpCwpCraneList()) {
            if (cwpCrane.getDpWait()) {
                Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
                if (bayNoLast != null) { // 桥机是等待状态，则要以上次决策的倍位作为这次的决策结果（因为一般情况下桥机还要回去作业）
                    DPPair dpPair = new DPPair<>(cwpCrane.getCraneNo(), bayNoLast);
                    traceBackList.add(dpPair);
                    cranePositionList.add(dpPair);
                }
            }
        }
        addCurDpPairToList(curDpResult.getDpTraceBack(), traceBackList);
        addCurDpPairToList(curDpResult.getDpCranePosition(), cranePositionList);
        curDpResult.setDpTraceBack(traceBackList);
        curDpResult.setDpCranePosition(cranePositionList);
        cwpData.setDpResult(curDpResult);
    }

    private void addCurDpPairToList(List<DPPair> curDpPairList, List<DPPair> dpPairList) {
        for (DPPair dpPair : curDpPairList) {
            boolean flag = false;
            for (DPPair dpPair1 : dpPairList) {
                if (dpPair.getFirst().equals(dpPair1.getFirst())) {
                    flag = true;
                }
            }
            if (!flag) {
                dpPairList.add(dpPair);
            }
        }
    }

    private boolean finish(int depth, CwpData cwpData) {
        boolean isFinish = true;
        for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
            if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                isFinish = false;
                break;
            }
        }
        int d = 200;
        isFinish = depth > d || isFinish;
        if (isFinish) {
            if (depth > d) {
                StringBuilder strBuilder = new StringBuilder("bayNo: ");
                for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
                    if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                        strBuilder.append(cwpBay.getBayNo()).append(":").append(cwpBay.getDpCurrentTotalWorkTime()).append("-").append(cwpBay.getDpAvailableWorkTime()).append(" ");
                    }
                }
                cwpData.getWorkingData().getLogger().logError("CWP算法没有排完所有箱子的计划，请检查倍位(" + strBuilder.toString() + ")！");
                return false;
            }
        }
        return !isFinish;
    }

    private long realWork(DPResult dpResult, long minWorkTime, CwpData cwpData, CwpDataMethod cwpDataMethod) {
        CwpConfig cwpConfig = cwpData.getWorkingData().getCwpConfig();
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            CWPCrane cwpCrane = PublicMethod.getCwpCraneByCraneNo((String) dpPair.getFirst(), cwpData.getDpCwpCraneList());
            CWPBay cwpBay = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
            DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(cwpData.getDpCraneSelectBays(), dpPair);
            if (cwpCrane != null && cwpBay != null && dpCraneSelectBay != null) { // can not be null
                // 加上移动时间，得到真正开始作业的时间
                long moveTime = 0L;
                if (!cwpData.getFirstDoCwp()) { // 不是第一次dp决策，要计算桥机移动时间
                    moveTime += (long) (dpCraneSelectBay.getDpDistance() / cwpConfig.getCraneMoveSpeed());
                    if (dpCraneSelectBay.getTroughMachine()) { // 过驾驶台的移动时间
                        moveTime += cwpConfig.getCrossBarTime();
                    }
                    cwpCrane.addDpCurrentTime(moveTime);
                    // 扫舱时间
                    Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
                    if (bayNoLast != null && !cwpBay.getBayNo().equals(bayNoLast)) {
                        moveTime += cwpConfig.getHatchScanTime();
                        cwpCrane.addDpCurrentTime(cwpData.getWorkingData().getCwpConfig().getHatchScanTime());
                        cwpData.setDpMoveNumber(cwpData.getDpMoveNumber() + 1);
                    }
                    // 打锁钮的时间
                    if (!cwpBay.getBayNo().equals(bayNoLast)) {
                        long unLockTime = cwpDataMethod.computeUnLockTime(cwpBay, cwpData);
                        moveTime += unLockTime;
                        cwpCrane.addDpCurrentTime(unLockTime);
                    }
                } else {
                    cwpCrane.setDpCurrentTime(cwpData.getDpCurrentTime());
                }
                // 桥机作业结束时间
                long workTime;
                if (minWorkTime > moveTime) {
                    workTime = minWorkTime - moveTime;
                    long availableWt;
                    if (cwpBay.getBayNo() % 2 == 0) { // 大倍位可作业量有卸有装时，取卸的作业量
                        availableWt = cwpBay.getDpAvailableDiscWtD() > 0 && cwpBay.getDpAvailableLoadWtD() > 0 ? cwpBay.getDpAvailableDiscWtD() : cwpBay.getDpAvailableWorkTime();
                    } else {
                        availableWt = cwpBay.getDpAvailableWorkTime();
                    }
                    if (availableWt - workTime < CWPDefaultValue.moreCntMoveNum * cwpConfig.getOneCntTime()) { // todo: 多作业剩余的两关箱子
                        workTime = availableWt;
                    }
                } else {
                    if (!cwpData.getFirstDoCwp()) {// 桥机置为正在移动状态
                        cwpData.getWorkingData().getLogger().logDebug("桥机(" + cwpCrane.getCraneNo() + ")移动时间大于下次作业时间，正在移动准备下一次作业！");
                    }
                    workTime = minWorkTime;
                }
                cwpCrane.setDpEndWorkTime(cwpCrane.getDpCurrentTime() + workTime); // 设置桥机作业结束时间
            }
        }
        // 按最大时间，统一桥机作业结束时间，这样会导致需要换倍作业的桥机作业move时间延长很多
        List<CWPCrane> cwpCranes = cwpData.getDpCwpCraneList();
        long maxEndWorkTime = 0;
        for (CWPCrane cwpCrane : cwpCranes) {
            if (cwpCrane.getDpEndWorkTime() != null) {
                maxEndWorkTime = Math.max(maxEndWorkTime, cwpCrane.getDpEndWorkTime());
            }
        }
        for (CWPCrane cwpCrane : cwpCranes) {
            cwpCrane.setDpEndWorkTime(maxEndWorkTime);
        }
        long maxRealWorkTime = 0;
        boolean hatchWtCompletedFlag = false;
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            CWPCrane cwpCrane = PublicMethod.getCwpCraneByCraneNo((String) dpPair.getFirst(), cwpCranes);
            CWPBay cwpBay = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
            DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(cwpData.getDpCraneSelectBays(), dpPair);
            if (cwpCrane != null && dpCraneSelectBay != null) { //can not be null
                cwpCrane.setDpCurrentWorkPosition(cwpBay.getWorkPosition());
                cwpCrane.setDpCurrentWorkBayNo(cwpBay.getBayNo());
                long realWorkTime = cwpDataMethod.doProcessOrder(cwpCrane, cwpBay, dpCraneSelectBay.getDpFeature().getDesc(), cwpData);
                maxRealWorkTime = Math.max(maxRealWorkTime, realWorkTime);
                cwpCrane.addDpCurrentTime(realWorkTime); // 桥机增加实际作业了多长时间
                cwpCrane.setDpCurMeanWt(cwpCrane.getDpCurMeanWt() - realWorkTime);
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoFrom())) {
                    if (cwpCrane.getDpWorkTimeFrom() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        cwpCrane.setDpWorkTimeFrom(cwpCrane.getDpWorkTimeFrom() - realWorkTime);
                    }
                }
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoTo())) {
                    if (cwpCrane.getDpWorkTimeTo() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        cwpCrane.setDpWorkTimeTo(cwpCrane.getDpWorkTimeTo() - realWorkTime);
                    }
                }
                // 该次决策舱作业量被做完的次数
                List<CWPBay> cwpBayList = cwpData.getCwpHatchBayMap().get(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD());
                if (PublicMethod.getCurTotalWorkTime(cwpBayList) - realWorkTime <= 0) {
                    hatchWtCompletedFlag = true;
                }
            }
        }
        cwpData.getWorkingData().getLogger().logDebug("决策作业时间：" + maxRealWorkTime);
        boolean isFirstRealWork = !(maxRealWorkTime > 0) && cwpData.getFirstDoCwp();
        cwpData.setFirstDoCwp(isFirstRealWork);
        if (hatchWtCompletedFlag) {
            cwpData.setDpChangeHatchNumber(cwpData.getDpChangeHatchNumber() + 1);
        }
        //设置CWPData全局时间
        long maxCurrentTime = Long.MIN_VALUE;
        for (CWPCrane cwpCrane : cwpCranes) { //每部桥机的作业时间会有偏差，一个move的时间
            maxCurrentTime = Math.max(maxCurrentTime, cwpCrane.getDpCurrentTime());
        }
        cwpData.setDpCurrentTime(maxCurrentTime);
        for (CWPCrane cwpCrane : cwpCranes) {
            if (PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), dpResult) == null) { //没有被选择作业的桥机当前时间需要增加
                cwpCrane.setDpCurrentTime(cwpData.getDpCurrentTime());
            }
        }
        for (CWPCrane cwpCrane : cwpData.getAllCWPCranes()) {
            cwpCrane.setDpCurrentTime(cwpData.getDpCurrentTime());
        }
        return maxRealWorkTime;
    }
}
