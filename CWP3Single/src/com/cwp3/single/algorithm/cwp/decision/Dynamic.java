package com.cwp3.single.algorithm.cwp.decision;

import com.cwp3.single.algorithm.cwp.method.LogPrintMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;
import com.cwp3.utils.CalculateUtil;

import java.util.List;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class Dynamic {

    public DPResult cwpKernel(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        List<DPCraneSelectBay> dpCraneSelectBays = cwpData.getDpCraneSelectBays();
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();

        int craneNum = cwpCranes.size();
        int bayNum = cwpBays.size();
        int dpSize = dpCraneSelectBays.size();
        if (craneNum == 0 || bayNum == 0 || dpSize != craneNum * bayNum) {
            cwpData.getWorkingData().getLogger().logError("Dynamic error!(" + craneNum + "、" + bayNum + "、" + dpSize + ")");
            return new DPResult();
        }

        DPResult[][] dp = new DPResult[craneNum][bayNum];
        for (int i = 0; i < craneNum; i++) {
            for (int j = 0; j < bayNum; j++) {
                dp[i][j] = new DPResult();
            }
        }

        CWPCrane cwpCrane = cwpCranes.get(0);
        CWPBay cwpBay = cwpBays.get(0);
        DPPair dpPair = new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo());
        DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(dpCraneSelectBays, dpPair);
        if (dpCraneSelectBay != null) {
            if (dpCraneSelectBay.getDpWorkTime() > 0) {
                dp[0][0].setDpFeatureCode(dpCraneSelectBay.getDpFeature().getCode());
                dp[0][0].setDpWorkTime(dpCraneSelectBay.getDpWorkTime());
                dp[0][0].setDpDistance(dpCraneSelectBay.getDpDistance());
                dp[0][0].getDpTraceBack().add(dpCraneSelectBay.getDpPair());
                dp[0][0].getDpCranePosition().add(dpCraneSelectBay.getDpPair());
            } else {
                dp[0][0].setDpDistance(dpCraneSelectBay.getDpDistance());
                dp[0][0].getDpCranePosition().add(dpCraneSelectBay.getDpPair());
            }
        }

        for (int i = 1; i < craneNum; i++) {
            cwpCrane = cwpCranes.get(i);
            dpPair = new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo());
            dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(dpCraneSelectBays, dpPair);
            if (dpCraneSelectBay != null) {
                if (dpCraneSelectBay.getDpWorkTime() > 0) {
                    dp[i][0].setDpFeatureCode(dpCraneSelectBay.getDpFeature().getCode());
                    dp[i][0].setDpWorkTime(dpCraneSelectBay.getDpWorkTime());
                    dp[i][0].setDpDistance(dpCraneSelectBay.getDpDistance());
                    dp[i][0].getDpTraceBack().add(dpCraneSelectBay.getDpPair());
                    dp[i][0].getDpCranePosition().add(dpCraneSelectBay.getDpPair());
                } else {
                    dp[i][0].setDpDistance(dpCraneSelectBay.getDpDistance()); //？
                    dp[i][0].getDpCranePosition().add(dpCraneSelectBay.getDpPair());
                }
            }
        }

        cwpCrane = cwpCranes.get(0);
        for (int j = 1; j < bayNum; j++) {
            cwpBay = cwpBays.get(j);
            dpPair = new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo());
            dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(dpCraneSelectBays, dpPair);
            if (dpCraneSelectBay != null) {
                dp[0][j].setDpFeatureCode(dpCraneSelectBay.getDpFeature().getCode());
                dp[0][j].setDpWorkTime(dpCraneSelectBay.getDpWorkTime());
                dp[0][j].setDpDistance(dpCraneSelectBay.getDpDistance());
                if (better(dp[0][j], dp[0][j - 1])) {
                    if (dpCraneSelectBay.getDpWorkTime() > 0) {
                        dp[0][j].getDpTraceBack().add(dpCraneSelectBay.getDpPair());
                    }
                    dp[0][j].getDpCranePosition().add(dpCraneSelectBay.getDpPair());
                } else {
                    dp[0][j] = dp[0][j - 1].deepCopy();
                }
            }
        }

        for (int i = 1; i < craneNum; i++) {
            for (int j = 1; j < bayNum; j++) {
                cwpCrane = cwpCranes.get(i);
                cwpBay = cwpBays.get(j);
                dpPair = new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo());
                dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(dpCraneSelectBays, dpPair);
                if (dpCraneSelectBay != null) {
                    if (dpCraneSelectBay.getDpFeature().getCode() > 0 || cwpCrane.getDpWait() || i == craneNum - 1) {
                        DPResult cur_dp = new DPResult();
                        int k = j;
                        while (k >= 0 && CalculateUtil.sub(cwpBay.getWorkPosition(), cwpBays.get(k).getWorkPosition()) < craneSafeSpan) {
                            k--;
                        }
                        if (k < 0) {
                            cur_dp.setDpFeatureCode(dpCraneSelectBay.getDpFeature().getCode());
                            cur_dp.setDpWorkTime(dpCraneSelectBay.getDpWorkTime());
                            cur_dp.setDpDistance(dpCraneSelectBay.getDpDistance());
                        } else {
                            cur_dp.setDpFeatureCode(dpCraneSelectBay.getDpFeature().getCode() + dp[i - 1][k].getDpFeatureCode());
                            cur_dp.setDpWorkTime(dpCraneSelectBay.getDpWorkTime() + dp[i - 1][k].getDpWorkTime());
                            double d = dp[i - 1][k].getDpWorkTime() > 0 ? dp[i - 1][k].getDpDistance() : 0;
                            cur_dp.setDpDistance(dpCraneSelectBay.getDpDistance() + d); //???
                            cur_dp.setDpTraceBack(dp[i - 1][k].getDpTraceBack());
                            cur_dp.setDpCranePosition(dp[i - 1][k].getDpCranePosition());
                        }
                        if (better(cur_dp, dp[i][j - 1])) {
                            dp[i][j] = cur_dp.deepCopy();
                            dp[i][j].getDpCranePosition().add(dpCraneSelectBay.getDpPair());
                            if (dpCraneSelectBay.getDpWorkTime() > 0) {
                                dp[i][j].getDpTraceBack().add(dpCraneSelectBay.getDpPair());
                            }
                        } else {
                            dp[i][j] = dp[i][j - 1].deepCopy();
                        }
                    } else { //????????
                        dp[i][j] = dp[i][j - 1].deepCopy();
                    }
                }
            }
        }

        DPResult dpResult = dp[craneNum - 1][bayNum - 1].deepCopy();
        for (CWPCrane cwpCrane1 : cwpCranes) {
            for (DPPair dpPair1 : dpResult.getDpCranePosition()) {
                if (cwpCrane1.getCraneNo().equals(dpPair1.getFirst())) {
                    cwpCrane1.setDpCurrentWorkBayNo((Integer) dpPair1.getSecond());
                }
            }
        }

        LogPrintMethod.printDpInfo1(cwpCranes, cwpBays, dp, cwpData.getWorkingData().getLogger());

        return dpResult;
    }

//    private boolean better(DPResult cur_dp, DPResult dpResult) {
//        if (cur_dp.getDpFeatureCode() > dpResult.getDpFeatureCode()) {
//            return true;
//        } else {
//            if (cur_dp.getDpFeatureCode() == dpResult.getDpFeatureCode()) {
//                return cur_dp.getDpDistance() < dpResult.getDpDistance();
//            } else {
//                return false;
//            }
//        }
//    }

    private boolean better(DPResult cur_dp, DPResult dpResult) {
        if (cur_dp.getDpFeatureCode() > dpResult.getDpFeatureCode()) {
            return true;
        } else {
            if (cur_dp.getDpFeatureCode() == dpResult.getDpFeatureCode()) {
                // 如果dpResult的移动距离=0、且cur_dp的移动距离>0，则比较当前决策可以选择的作业量
                if (cur_dp.getDpDistance() > 0 && dpResult.getDpDistance() == 0) {
                    return cur_dp.getDpWorkTime().compareTo(dpResult.getDpWorkTime()) >= 0;
                }
                return cur_dp.getDpDistance() < dpResult.getDpDistance();
            } else {
                return false;
            }
        }
    }
}
