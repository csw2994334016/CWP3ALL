package com.cwp3.single.algorithm.move.method;

import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.crane.CMCraneWorkFlow;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMContainerSlot;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.single.algorithm.move.maker.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by csw on 2017/9/21.
 * Description: 作业工艺、作业顺序相关工具方法类
 */
public class PublicMethod {

    public static List<AbstractMaker> getPTSeqListByCMCraneWorkFlow(CMCraneWorkFlow cmCraneWorkFlow) {
        List<AbstractMaker> ptSeqList = new ArrayList<>();
        if (cmCraneWorkFlow != null) {
            if (cmCraneWorkFlow.getSingle()) {
                ptSeqList.add(new M20Single());
                ptSeqList.add(new M40Single());
            }
            if (cmCraneWorkFlow.getTwin()) {
                ptSeqList.add(new M20Dual());
            }
            if (cmCraneWorkFlow.getTandem()) {
                ptSeqList.add(new M40Dual());
                ptSeqList.add(new M20Quad());
            }
        } else {
            ptSeqList.add(new M20Single());
            ptSeqList.add(new M40Single());
            ptSeqList.add(new M20Dual());
        }
        return ptSeqList;

    }

    public static List<AbstractMaker> getSinglePTSeqListByCMCraneWorkFlow(CMCraneWorkFlow cmCraneWorkFlow) {
        List<AbstractMaker> ptSeqList = new ArrayList<>();
        if (cmCraneWorkFlow != null) {
            if (cmCraneWorkFlow.getSingle()) {
                ptSeqList.add(new M20Single());
                ptSeqList.add(new M40Single());
            }
            if (cmCraneWorkFlow.getTandem()) {
                ptSeqList.add(new M20Twin());
            }
            if (cmCraneWorkFlow.getTwin()) {
                ptSeqList.add(new M20Dual());
            }
        } else {
            ptSeqList.add(new M20Single());
            ptSeqList.add(new M40Single());
            ptSeqList.add(new M20Dual());
        }
        return ptSeqList;
    }

    public static List<AbstractMaker> getDoublePTSeqListByCMCraneWorkFlow(CMCraneWorkFlow cmCraneWorkFlow) {
        List<AbstractMaker> ptSeqList = new ArrayList<>();
        if (cmCraneWorkFlow != null) {
            if (cmCraneWorkFlow.getSingle()) {
                ptSeqList.add(new M20Single());
                ptSeqList.add(new M40Single());
            }
            if (cmCraneWorkFlow.getTwin()) {
                ptSeqList.add(new M20Dual());
            }
            if (cmCraneWorkFlow.getTandem()) {
                ptSeqList.add(new M40Dual());
                ptSeqList.add(new M20Quad());
            }
        }
        return ptSeqList;
    }

    public static List<AbstractMaker> getDSinglePTSeqList() {
        List<AbstractMaker> ptSeqList = new ArrayList<>();
        ptSeqList.add(new M40Single());
        ptSeqList.add(new M20Dual());
        return ptSeqList;
    }

    public static boolean hasNoneWorkFlow(String workflow) {
        return !Arrays.asList(CWPCraneDomain.CT_SINGLE20, CWPCraneDomain.CT_TWIN20, CWPCraneDomain.CT_DUAL20, CWPCraneDomain.CT_QUAD20, CWPCraneDomain.CT_SINGLE40, CWPCraneDomain.CT_DUAL40).contains(workflow);
    }

    public static boolean isSingleWorkFlow(String workFlow) {
        return CWPCraneDomain.CT_SINGLE20.equals(workFlow) || CWPCraneDomain.CT_SINGLE40.equals(workFlow);
    }

    public static long getCntWorkTime(VMSlot vmSlot, VMContainer vmContainer, CwpConfig cwpConfig, String dlType) {
        if (vmContainer.getWorkFlow() != null) {
            long cntWorkTime = vmContainer.getCntWorkTime() != null ? vmContainer.getCntWorkTime() : 1;
            cntWorkTime = cntWorkTime > CWPDefaultValue.defaultCntWorkTime ? cntWorkTime : 1;
            VMContainerSlot vmContainerSlot = (VMContainerSlot) vmSlot;
            if (CWPDomain.BOARD_ABOVE.equals(vmContainerSlot.getVmBay().getAboveOrBelow()) && CWPDomain.DL_TYPE_LOAD.equals(dlType)) { // AL
                switch (vmContainer.getWorkFlow()) {
                    case CWPCraneDomain.CT_SINGLE20:
                        return cntWorkTime > cwpConfig.getSingle20TimeAL() ? cntWorkTime : cwpConfig.getSingle20TimeAL();
                    case CWPCraneDomain.CT_SINGLE40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle40TimeAL() ? cntWorkTime : cwpConfig.getSingle40TimeAL();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle45TimeAL() ? cntWorkTime : cwpConfig.getSingle45TimeAL();
                        } else {
                            return cntWorkTime > cwpConfig.getSingle40TimeAL() ? cntWorkTime : cwpConfig.getSingle40TimeAL();
                        }
                    case CWPCraneDomain.CT_DUAL20:
                        return cntWorkTime > cwpConfig.getDouble20TimeAL() ? cntWorkTime : cwpConfig.getDouble20TimeAL();
                    case CWPCraneDomain.CT_DUAL40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble40TimeAL() ? cntWorkTime : cwpConfig.getDouble40TimeAL();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble45TimeAL() ? cntWorkTime : cwpConfig.getDouble45TimeAL();
                        } else {
                            return cntWorkTime > cwpConfig.getDouble40TimeAL() ? cntWorkTime : cwpConfig.getDouble40TimeAL();
                        }
                    case CWPCraneDomain.CT_QUAD20:
                        return cwpConfig.getDouble40TimeAL();
                    case CWPCraneDomain.CT_TWIN20:
                        return cwpConfig.getDouble40TimeAL();
                    default:
                        return cntWorkTime;
                }
            } else if (CWPDomain.BOARD_ABOVE.equals(vmContainerSlot.getVmBay().getAboveOrBelow()) && CWPDomain.DL_TYPE_DISC.equals(dlType)) { // AD
                switch (vmContainer.getWorkFlow()) {
                    case CWPCraneDomain.CT_SINGLE20:
                        return cntWorkTime > cwpConfig.getSingle20TimeAD() ? cntWorkTime : cwpConfig.getSingle20TimeAD();
                    case CWPCraneDomain.CT_SINGLE40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle40TimeAD() ? cntWorkTime : cwpConfig.getSingle40TimeAD();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle45TimeAD() ? cntWorkTime : cwpConfig.getSingle45TimeAD();
                        } else {
                            return cntWorkTime > cwpConfig.getSingle40TimeAD() ? cntWorkTime : cwpConfig.getSingle40TimeAD();
                        }
                    case CWPCraneDomain.CT_DUAL20:
                        return cntWorkTime > cwpConfig.getDouble20TimeAD() ? cntWorkTime : cwpConfig.getDouble20TimeAD();
                    case CWPCraneDomain.CT_DUAL40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble40TimeAD() ? cntWorkTime : cwpConfig.getDouble40TimeAD();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble45TimeAD() ? cntWorkTime : cwpConfig.getDouble45TimeAD();
                        } else {
                            return cntWorkTime > cwpConfig.getDouble40TimeAD() ? cntWorkTime : cwpConfig.getDouble40TimeAD();
                        }
                    case CWPCraneDomain.CT_QUAD20:
                        return cwpConfig.getDouble40TimeAD();
                    case CWPCraneDomain.CT_TWIN20:
                        return cwpConfig.getDouble40TimeAD();
                    default:
                        return cntWorkTime;
                }
            } else if (CWPDomain.BOARD_BELOW.equals(vmContainerSlot.getVmBay().getAboveOrBelow()) && CWPDomain.DL_TYPE_LOAD.equals(dlType)) { // BL
                switch (vmContainer.getWorkFlow()) {
                    case CWPCraneDomain.CT_SINGLE20:
                        return cntWorkTime > cwpConfig.getSingle20TimeBL() ? cntWorkTime : cwpConfig.getSingle20TimeBL();
                    case CWPCraneDomain.CT_SINGLE40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle40TimeBL() ? cntWorkTime : cwpConfig.getSingle40TimeBL();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle45TimeBL() ? cntWorkTime : cwpConfig.getSingle45TimeBL();
                        } else {
                            return cntWorkTime > cwpConfig.getSingle40TimeBL() ? cntWorkTime : cwpConfig.getSingle40TimeBL();
                        }
                    case CWPCraneDomain.CT_DUAL20:
                        return cntWorkTime > cwpConfig.getDouble20TimeBL() ? cntWorkTime : cwpConfig.getDouble20TimeBL();
                    case CWPCraneDomain.CT_DUAL40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble40TimeBL() ? cntWorkTime : cwpConfig.getDouble40TimeBL();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble45TimeBL() ? cntWorkTime : cwpConfig.getDouble45TimeBL();
                        } else {
                            return cntWorkTime > cwpConfig.getDouble40TimeBL() ? cntWorkTime : cwpConfig.getDouble40TimeBL();
                        }
                    case CWPCraneDomain.CT_QUAD20:
                        return cwpConfig.getDouble40TimeBL();
                    case CWPCraneDomain.CT_TWIN20:
                        return cwpConfig.getDouble40TimeBL();
                    default:
                        return cntWorkTime;
                }
            } else if (CWPDomain.BOARD_BELOW.equals(vmContainerSlot.getVmBay().getAboveOrBelow()) && CWPDomain.DL_TYPE_DISC.equals(dlType)) { // BD
                switch (vmContainer.getWorkFlow()) {
                    case CWPCraneDomain.CT_SINGLE20:
                        return cntWorkTime > cwpConfig.getSingle20TimeBD() ? cntWorkTime : cwpConfig.getSingle20TimeBD();
                    case CWPCraneDomain.CT_SINGLE40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle40TimeBD() ? cntWorkTime : cwpConfig.getSingle40TimeBD();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getSingle45TimeBD() ? cntWorkTime : cwpConfig.getSingle45TimeBD();
                        } else {
                            return cntWorkTime > cwpConfig.getSingle40TimeBD() ? cntWorkTime : cwpConfig.getSingle40TimeBD();
                        }
                    case CWPCraneDomain.CT_DUAL20:
                        return cntWorkTime > cwpConfig.getDouble20TimeBD() ? cntWorkTime : cwpConfig.getDouble20TimeBD();
                    case CWPCraneDomain.CT_DUAL40:
                        if ("40".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble40TimeBD() ? cntWorkTime : cwpConfig.getDouble40TimeBD();
                        } else if ("45".equals(vmContainer.getSize())) {
                            return cntWorkTime > cwpConfig.getDouble45TimeBD() ? cntWorkTime : cwpConfig.getDouble45TimeBD();
                        } else {
                            return cntWorkTime > cwpConfig.getDouble40TimeBD() ? cntWorkTime : cwpConfig.getDouble40TimeBD();
                        }
                    case CWPCraneDomain.CT_QUAD20:
                        return cwpConfig.getDouble40TimeBD();
                    case CWPCraneDomain.CT_TWIN20:
                        return cwpConfig.getDouble40TimeBD();
                    default:
                        return cntWorkTime;
                }
            }

        }
        return cwpConfig.getOneCntTime();
    }

    public static String getWorkFlowStr(String workFlow) {
        switch (workFlow) {
            case CWPCraneDomain.CT_SINGLE20:
                return "1";
            case CWPCraneDomain.CT_SINGLE40:
                return "1";
            case CWPCraneDomain.CT_DUAL20:
                return "2";
            case CWPCraneDomain.CT_DUAL40:
                return "3";
            case CWPCraneDomain.CT_QUAD20:
                return "3";
            case CWPCraneDomain.CT_TWIN20:
                return "4";
            default:
                return "";
        }
    }

    public static String getWorkFlowStr1(String workFlow, String size) {
        switch (workFlow) {
            case "1":
                return size.startsWith("2") ? CWPCraneDomain.CT_SINGLE20 : CWPCraneDomain.CT_SINGLE40;
            case "2":
                return CWPCraneDomain.CT_DUAL20;
            case "3":
                return size.startsWith("2") ? CWPCraneDomain.CT_QUAD20 : CWPCraneDomain.CT_DUAL40;
            case "4":
                return CWPCraneDomain.CT_TWIN20;
            default:
                return null;
        }
    }

    public static double heightFormat(String cntHeight) {
        switch (cntHeight) {
            case "<8'":
                return 8.6;
            case "8'6''":
                return 8.6;
            case "9'6''":
                return 9.6;
            case "7'10''":
                return 7.10;
            default:
                return 0;
        }
    }

    public static List<String> getAllWorkFlowSeqList() {
        return Arrays.asList(CWPCraneDomain.CT_SINGLE20, CWPCraneDomain.CT_TWIN20, CWPCraneDomain.CT_DUAL20, CWPCraneDomain.CT_SINGLE40, CWPCraneDomain.CT_DUAL40, CWPCraneDomain.CT_QUAD20, CWPCraneDomain.CT_HATCH_COVER);
    }

    public static boolean isOverrunCnt(VMContainer vmContainer) {
        return CWPDomain.CNT_TYPE_O.equals(vmContainer.getOverrunCd()) || CWPDomain.CNT_TYPE_OH.equals(vmContainer.getOverrunCd())
                || CWPDomain.CNT_TYPE_OL.equals(vmContainer.getOverrunCd()) || CWPDomain.CNT_TYPE_OW.equals(vmContainer.getOverrunCd());
    }

    public static boolean isOverrunWidthCnt(String overrunCd) {
        return CWPDomain.CNT_TYPE_O.equals(overrunCd) || CWPDomain.CNT_TYPE_OW.equals(overrunCd);
    }
}
