package com.cwp3.single.algorithm.cwp.method;

import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.model.log.Logger;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;

import java.util.List;

/**
 * Created by csw on 2017/11/14.
 * Description:
 */
public class LogPrintMethod {

    public static void printCurBayWorkTime(List<CWPBay> cwpBays, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            StringBuilder bayWorkTimeStr = new StringBuilder();
            StringBuilder bayStr = new StringBuilder();
            for (CWPBay cwpBay : cwpBays) {
                bayStr.append(cwpBay.getBayNo()).append(":").append(cwpBay.getDpAvailableWorkTime()).append(" ");
                bayWorkTimeStr.append(cwpBay.getBayNo()).append(":").append(cwpBay.getDpCurrentTotalWorkTime()).append(" ");
            }
            logger.logDebug("当前每个倍可作业量:" + bayStr);
            logger.logDebug("当前每个倍作业总量:" + bayWorkTimeStr);
        }
    }

    public static void printSelectedCrane(List<CWPCrane> cwpCraneList, Logger logger) {
        StringBuilder selectedCraneNoStr = new StringBuilder("CWP algorithm selects cranes(No): ");
        for (CWPCrane craneNo : cwpCraneList) {
            selectedCraneNoStr.append(craneNo.getCraneNo()).append(" ");
        }
        if (CWPDefaultValue.outputLogToConsole) {
            logger.logDebug(selectedCraneNoStr.toString());
            StringBuilder bayNos = new StringBuilder();
            for (CWPCrane cwpCrane : cwpCraneList) {
                selectedCraneNoStr.setLength(0);
                bayNos.setLength(0);
                for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
                    bayNos.append(bayNo).append("、");
                }
                selectedCraneNoStr.append(cwpCrane.getCraneNo()).append(", ").append(cwpCrane.getDpCurrentWorkBayNo()).append(", ").
                        append(cwpCrane.getDpWorkBayNoFrom()).append("~").append(cwpCrane.getDpWorkBayNoTo()).append(", ").
                        append(cwpCrane.getDpWorkTimeFrom()).append("~").append(cwpCrane.getDpWorkTimeTo()).append(", ").
                        append(cwpCrane.getDpCurMeanWt()).append(", ").
                        append(bayNos);
                logger.logDebug("作业范围(桥机号,作业倍,左右范围倍号,左右范围量,meanWt,bayNos): " + selectedCraneNoStr);
            }
        }
    }

    public static void printDpInfo1(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, DPResult[][] dp, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            int craneNum = cwpCranes.size();
            int bayNum = cwpBays.size();
            StringBuilder str = new StringBuilder("dp:");
            for (int t = 0; t < dp[craneNum - 1][bayNum - 1].getDpTraceBack().size(); t++) {
                str.append("(").append(dp[craneNum - 1][bayNum - 1].getDpTraceBack().get(t).getFirst()).append(",").append(dp[craneNum - 1][bayNum - 1].getDpTraceBack().get(t).getSecond()).append(")");
            }
            logger.logDebug(str.toString());
        }
    }

    public static void printCraneSelectBayInfo(CwpData cwpData, Logger logger) {
        if (CWPDefaultValue.outputLogToConsole) {
            for (CWPCrane cwpCrane : cwpData.getDpCwpCraneList()) {
                StringBuilder str = new StringBuilder("桥机倍位特征值(" + cwpCrane.getCraneNo() + "):");
                for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
                    DPPair dpPair = new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo());
                    DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(cwpData.getDpCraneSelectBays(), dpPair);
                    str.append(cwpBay.getBayNo()).append(":").append(dpCraneSelectBay != null ? dpCraneSelectBay.getDpFeature().getCode() : -1).append(" ");
                }
                logger.logDebug(str.toString());
            }
        }
    }

    public static void printMaxCwpBay(List<CWPBay> maxCwpBayList, Logger logger) {
        StringBuilder sb = new StringBuilder("The max road is: ");
        for (CWPBay cwpBay : maxCwpBayList) {
            sb.append(cwpBay.getBayNo()).append(" ");
        }
        logger.logInfo(sb.toString());
    }
}
