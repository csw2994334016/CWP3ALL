package com.cwp3.ioservice.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.all.MachineData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCntDomain;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.ioservice.ParseDataService;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.crane.*;
import com.cwp3.model.log.Logger;
import com.cwp3.model.other.AreaContainer;
import com.cwp3.model.vessel.*;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.utils.BeanCopyUtil;
import com.cwp3.utils.StringUtil;
import com.cwp3.utils.ValidatorUtil;
import com.shbtos.biz.smart.cwp.pojo.*;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CarloJones on 2018/3/6.
 */
public class ParseDataServiceImpl implements ParseDataService {

    private Logger logger;

    public ParseDataServiceImpl() {
        this.logger = new Logger();
    }

    @Override
    public AllRuntimeData parseAllRuntimeData(SmartCwpImportData smartCwpImportData) {
        AllRuntimeData allRuntimeData = new AllRuntimeData();

        List<VMSchedule> vmScheduleList = parseSchedule(smartCwpImportData.getSmartScheduleIdInfoList(), CWPDefaultValue.CWP_VERSION);
        for (VMSchedule vmSchedule : vmScheduleList) {
            allRuntimeData.addStructData(new StructureData(vmSchedule.getVesselCode()));
            allRuntimeData.addWorkingData(new WorkingData(vmSchedule));
        }

        parseCMCraneWithType(smartCwpImportData.getSmartCraneBaseInfoList(), allRuntimeData.getMachineData());

        //解析桥机物理移动范围SmartCraneMoveRangeInfo，需要转换成实际坐标值
        parseCraneMoveRange(smartCwpImportData.getSmartCraneMoveRangeInfoList(), allRuntimeData.getMachineData());

        parseStructData(smartCwpImportData, allRuntimeData);
        parseWorkingData(smartCwpImportData, allRuntimeData);

        allRuntimeData.setLogger(logger);
        return allRuntimeData;
    }

    @Override
    public AllRuntimeData parseAllRuntimeDataByCraneAllocation(SmartCwpImportData smartCwpImportData) {
        AllRuntimeData allRuntimeData = new AllRuntimeData();
        //重新封装SmartScheduleIdInfoList到类（VMSchedule）
        List<VMSchedule> vmScheduleList = parseSchedule(smartCwpImportData.getSmartScheduleIdInfoList(), CWPDefaultValue.CRANE_ALLOCATION_VERSION);
        for (VMSchedule vmSchedule : vmScheduleList) {
            allRuntimeData.addStructData(new StructureData(vmSchedule.getVesselCode()));
            allRuntimeData.addWorkingData(new WorkingData(vmSchedule));
        }
        //解析桥机
        parseCMCraneWithType(smartCwpImportData.getSmartCraneBaseInfoList(), allRuntimeData.getMachineData());

        //解析桥机物理移动范围SmartCraneMoveRangeInfo，需要转换成实际坐标值
        parseCraneMoveRange(smartCwpImportData.getSmartCraneMoveRangeInfoList(), allRuntimeData.getMachineData());

        //解析人工设置的桥机分配信息
        parseCraneManual(smartCwpImportData.getSmartCraneManualInfoList(), allRuntimeData);

        //解析桥机维修计划SmartCraneMaintainPlanInfo
        parseCraneMaintainPlan(smartCwpImportData.getSmartCraneMaintainPlanInfoList(), allRuntimeData.getMachineData());

        //解析桥机作业计划SmartCraneWorkPlanInfo，即有些桥机已经安排给某些船舶（有作业实际段）
        parseCraneWorkPlan(smartCwpImportData.getSmartCraneWorkPlanInfoList(), allRuntimeData.getMachineData());

        //解析船舶结构，只要舱和倍位信息
        parseHatchInfo(smartCwpImportData.getSmartVpsVslHatchsInfoList(), allRuntimeData);
        parseBayInfo(smartCwpImportData.getSmartVpsVslBaysInfoList(), allRuntimeData);

        //解析船舶箱量信息SmartVesselContainerAmountInfo
        parseVesselContainerAmount(smartCwpImportData.getSmartVesselContainerAmountInfoList(), smartCwpImportData.getSmartVesselContainerInfoList(), allRuntimeData);

        allRuntimeData.setLogger(logger);
        return allRuntimeData;
    }

    @Override
    public AllRuntimeData parseAllRuntimeDataByMultiCwp(SmartCwpImportData smartCwpImportData) {
        AllRuntimeData allRuntimeData = new AllRuntimeData();
        //重新封装SmartScheduleIdInfoList到类（VMSchedule）
        List<VMSchedule> vmScheduleList = parseSchedule(smartCwpImportData.getSmartScheduleIdInfoList(), CWPDefaultValue.MULTIPLE_CWP_VERSION);
        for (VMSchedule vmSchedule : vmScheduleList) {
            allRuntimeData.addStructData(new StructureData(vmSchedule.getVesselCode()));
            allRuntimeData.addWorkingData(new WorkingData(vmSchedule));
        }
        //解析桥机
        parseCMCraneWithType(smartCwpImportData.getSmartCraneBaseInfoList(), allRuntimeData.getMachineData());

        //解析桥机物理移动范围SmartCraneMoveRangeInfo，需要转换成实际坐标值
        parseCraneMoveRange(smartCwpImportData.getSmartCraneMoveRangeInfoList(), allRuntimeData.getMachineData());

        parseStructData(smartCwpImportData, allRuntimeData);
        parseWorkingData(smartCwpImportData, allRuntimeData);

        // 解析船舶箱量信息SmartVesselContainerAmountInfo
        parseVesselContainerAmount(smartCwpImportData.getSmartVesselContainerAmountInfoList(), smartCwpImportData.getSmartVesselContainerInfoList(), allRuntimeData);

        allRuntimeData.setLogger(logger);
        return allRuntimeData;
    }

    private void parseStructData(SmartCwpImportData smartCwpImportData, AllRuntimeData allRuntimeData) {
        parseHatchInfo(smartCwpImportData.getSmartVpsVslHatchsInfoList(), allRuntimeData);
        parseBayInfo(smartCwpImportData.getSmartVpsVslBaysInfoList(), allRuntimeData);
        parseRowInfo(smartCwpImportData.getSmartVpsVslRowsInfoList(), allRuntimeData);
        parseLocationInfo(smartCwpImportData.getSmartVpsVslLocationsInfoList(), allRuntimeData);
        parseHatchCoverInfo(smartCwpImportData.getSmartVpsVslHatchcoversInfoList(), allRuntimeData);
        parseMachineInfo(smartCwpImportData.getSmartVesselMachinesInfoList(), allRuntimeData);
        // 处理一下特殊船舶结构：非两边的槽，如果甲板上/下有，甲板下/上没有，则进行相应的补全
        analyzeStructData(allRuntimeData);
    }

    private void analyzeStructData(AllRuntimeData allRuntimeData) {
        for (StructureData structureData : allRuntimeData.getAllStructureDataList()) {
            List<VMHatch> vmHatchList = structureData.getAllVMHatchs();
            for (VMHatch vmHatch : vmHatchList) {
                List<Integer> bayNoList = vmHatch.getBayNos();
                for (Integer bayNo : bayNoList) {
                    VMBay vmBayA = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_ABOVE));
                    VMBay vmBayB = structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_BELOW));
                    List<Integer> rowNoListA = vmBayA.getRowNoList();
                    List<Integer> rowNoListB = vmBayB.getRowNoList();
                    if (rowNoListA.contains(0) && !rowNoListB.contains(0)) {
                        addVMRowAndVMSlot(vmBayB, structureData);
                    } else if (!rowNoListA.contains(0) && rowNoListB.contains(0)) {
                        addVMRowAndVMSlot(vmBayA, structureData);
                    }
                }
            }
        }
    }

    private void addVMRowAndVMSlot(VMBay vmBayA, StructureData structureData) {
        VMRow vmRowSide = vmBayA.getVMRowByRowNo(1);
        if (vmRowSide != null) {
            // 添加排
            VMRow vmRow = new VMRow(vmBayA.getBayId(), vmBayA.getBayKey(), 0);
            vmBayA.addVMRow(vmRow);
            // 添加VMSlot
            for (VMSlot vmSlot : vmRowSide.getAllVMSlotList()) {
                VMContainerSlot vmContainerSlotSide = (VMContainerSlot) vmSlot;
                VMPosition vmPosition = new VMPosition(vmSlot.getVmPosition().getBayNo(), 0, vmSlot.getVmPosition().getTierNo());
                VMContainerSlot vmContainerSlot = new VMContainerSlot(vmPosition, vmBayA, vmContainerSlotSide.getSize());
                vmRow.addVMSlot(vmContainerSlot);
                structureData.addVMSlot(vmContainerSlot);
            }
        }
    }


    private void parseWorkingData(SmartCwpImportData smartCwpImportData, AllRuntimeData allRuntimeData) {
        parseCraneWorkFlow(smartCwpImportData.getSmartCraneWorkFlowInfoList(), allRuntimeData);
        parseParameter(smartCwpImportData.getSmartCwpParameterInfoList(), allRuntimeData);
        parseVesselContainer(smartCwpImportData.getSmartVesselContainerInfoList(), allRuntimeData);
        parseCranePool(smartCwpImportData.getSmartVesselCranePoolInfoList(), smartCwpImportData.getSmartCranePoolInfoList(), smartCwpImportData.getSmartCraneFirstWorkInfoList(), allRuntimeData);
        parseCraneAddOrDelete(smartCwpImportData.getSmartCraneAddOrDelInfoList(), allRuntimeData);
        parseCwpWorkBlock(smartCwpImportData.getSmartCwpWorkBlockInfoList(), allRuntimeData);
    }

    private List<VMSchedule> parseSchedule(List<SmartScheduleIdInfo> smartScheduleIdInfoList, String version) {
        List<VMSchedule> vmScheduleList = new ArrayList<>();
        logger.logInfo("当前运行的算法版本号为: " + version);
        logger.logError("航次信息", ValidatorUtil.isEmpty(smartScheduleIdInfoList));
        for (SmartScheduleIdInfo smartScheduleIdInfo : smartScheduleIdInfoList) {
            Long berthId = smartScheduleIdInfo.getBerthId();
            String vesselCode = smartScheduleIdInfo.getVesselCode();
            String planBerthDirect = smartScheduleIdInfo.getPlanBerthDirect();
            String vesselType = smartScheduleIdInfo.getVesselType();
            try {
                logger.logError("航次信息-靠泊Id", ValidatorUtil.isNull(berthId));
                logger.logError("航次信息-船舶代码", ValidatorUtil.isBlank(vesselCode));
                logger.logError("航次信息-停靠方向", ValidatorUtil.isBlank(planBerthDirect));
                logger.logError("航次信息-船舶类型", ValidatorUtil.isBlank(vesselType));
                VMSchedule vmSchedule = new VMSchedule(berthId, vesselCode);
                vmSchedule.setPlanBeginWorkTime(smartScheduleIdInfo.getPlanBeginWorkTime());
                vmSchedule.setPlanEndWorkTime(smartScheduleIdInfo.getPlanEndWorkTime());
                vmSchedule.setPlanStartPst(smartScheduleIdInfo.getPlanStartPst());
                vmSchedule.setPlanEndPst(smartScheduleIdInfo.getPlanEndPst());
                vmSchedule.setSendWorkInstruction(smartScheduleIdInfo.getSendWorkInstruction());
                logger.logInfo("berthId: " + berthId + ", vesselCode: " + vesselCode + ", planBerthDirect: " + planBerthDirect + ", vesselType: " + vesselType);
                planBerthDirect = planBerthDirect.equals("L") ? CWPDomain.VES_BER_DIRECT_L : CWPDomain.VES_BER_DIRECT_R;
                vesselType = vesselType.equals("BAR") ? CWPDomain.VESSEL_TYPE_BAR : CWPDomain.VESSEL_TYPE_FCS;
                vmSchedule.setVesselType(vesselType);
                vmSchedule.setPlanBerthDirect(planBerthDirect);
                vmSchedule.setDoCwpFlag(smartScheduleIdInfo.getDoCwpFlag());
                vmSchedule.setStartWorkFlag(smartScheduleIdInfo.getStartWorkFlag());
                vmScheduleList.add(vmSchedule);
            } catch (Exception e) {
                logger.logError("解析航次(berthId:" + berthId + ", vesselCode:" + vesselCode + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
        return vmScheduleList;
    }

    private void parseCMCraneWithType(List<SmartCraneBaseInfo> smartCraneBaseInfoList, MachineData machineData) {
        logger.logError("桥机信息", ValidatorUtil.isEmpty(smartCraneBaseInfoList));
        for (SmartCraneBaseInfo smartCraneBaseInfo : smartCraneBaseInfoList) {
            CMCrane cmCrane = new CMCrane(smartCraneBaseInfo.getCraneNo());
            cmCrane = (CMCrane) BeanCopyUtil.copyBean(smartCraneBaseInfo, cmCrane);
            //解析QCType
            boolean matchSomeQCType = false;
            for (CMCraneType cmCraneType : machineData.getCMCraneTypes()) {
                //判断是否符合
                if (cmCraneType.getMaxWeightKg().equals(cmCrane.getCraneMaxCarryWeight())) {
                    matchSomeQCType = true;
                    //桥机类型已经存在，绑定桥机类型
                    cmCrane.setCraneTypeId(cmCraneType.getCraneTypeId());
                    break;
                }
            }
            if (!matchSomeQCType) { //桥机类型不存在
                //增加新的type
                CMCraneType cmCraneType = new CMCraneType(StringUtil.getKey("CT", cmCrane.getCraneMaxCarryWeight()), cmCrane.getCraneMaxCarryWeight());
                //默认支持所有作业工艺
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_SINGLE20);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_DUAL20);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_SINGLE40);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_DUAL40);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_QUAD20);
                machineData.addCMCraneType(cmCraneType);
                //绑定桥机类型
                cmCrane.setCraneTypeId(cmCraneType.getCraneTypeId());
            }
            machineData.addCMCrane(cmCrane);
        }
    }

    private void parseCraneMoveRange(List<SmartCraneMoveRangeInfo> smartCraneMoveRangeInfoList, MachineData machineData) {
        for (SmartCraneMoveRangeInfo smartCraneMoveRangeInfo : smartCraneMoveRangeInfoList) {
            CMCraneMoveRange cmCraneMoveRange = new CMCraneMoveRange();
            cmCraneMoveRange = (CMCraneMoveRange) BeanCopyUtil.copyBean(smartCraneMoveRangeInfo, cmCraneMoveRange);
            machineData.getCmCraneMoveRangeList().add(cmCraneMoveRange);
        }
    }

    private void parseHatchInfo(List<SmartVpsVslHatchsInfo> smartVpsVslHatchsInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("舱信息", ValidatorUtil.isEmpty(smartVpsVslHatchsInfoList));
        for (SmartVpsVslHatchsInfo smartVpsVslHatchsInfo : smartVpsVslHatchsInfoList) {
            String vesselCode = smartVpsVslHatchsInfo.getVesselCode();
            Long hatchId = smartVpsVslHatchsInfo.getHatchId();
            try {
                if (allRuntimeData.getStructDataByVesselCode(vesselCode) != null) {
                    logger.logError("舱信息-舱(Id:" + hatchId + ")信息为null", ValidatorUtil.isNull(hatchId));
                    VMHatch vmHatch = new VMHatch(hatchId);
                    logger.logError("舱信息-舱(Id:" + hatchId + ")位置坐标为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchPosition()));
                    vmHatch.setHatchPosition(smartVpsVslHatchsInfo.getHatchPosition());
                    logger.logError("舱信息-舱(Id:" + hatchId + ")长度为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchLength()));
                    vmHatch.setHatchLength(smartVpsVslHatchsInfo.getHatchLength());
                    logger.logError("舱信息-舱(Id:" + hatchId + ")序号为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchSeq()));
                    vmHatch.setHatchSeq(smartVpsVslHatchsInfo.getHatchSeq());
                    allRuntimeData.getStructDataByVesselCode(vesselCode).addVMHatch(vmHatch);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")舱(Id:" + hatchId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseBayInfo(List<SmartVpsVslBaysInfo> smartVpsVslBaysInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("倍位信息", ValidatorUtil.isEmpty(smartVpsVslBaysInfoList));
        StructureData structureData;
        for (SmartVpsVslBaysInfo smartVpsVslBaysInfo : smartVpsVslBaysInfoList) {
            Long bayId = smartVpsVslBaysInfo.getBayId();
            Long hatchId = smartVpsVslBaysInfo.getHatchId();
            String aboveOrBelow = smartVpsVslBaysInfo.getDeckOrHatch();
            String vesselCode = smartVpsVslBaysInfo.getVesselCode();
            Integer bayNo = null;
            try {
                structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    logger.logError("倍位信息-倍位号", ValidatorUtil.isNull(smartVpsVslBaysInfo.getBayNo()));
                    bayNo = Integer.valueOf(smartVpsVslBaysInfo.getBayNo());
                    logger.logError("倍位信息-倍位信息中甲板上、下字段为null", ValidatorUtil.isNull(aboveOrBelow));
                    aboveOrBelow = aboveOrBelow.equals("D") ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
                    String bayKey = StringUtil.getKey(bayNo, aboveOrBelow);
                    VMBay vmBay = new VMBay(bayId, bayKey, bayNo, aboveOrBelow, hatchId);
                    structureData.addVMBay(vmBay);
                    //设置舱内bay1、bay2，数字小在前
                    VMHatch vmHatch = structureData.getVMHatchByHatchId(hatchId);
                    vmHatch.addByNo(bayNo);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")倍位(bayNo:" + bayNo + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseRowInfo(List<SmartVpsVslRowsInfo> smartVpsVslRowsInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("排信息", ValidatorUtil.isEmpty(smartVpsVslRowsInfoList));
        StructureData structureData;
        for (SmartVpsVslRowsInfo smartVpsVslRowsInfo : smartVpsVslRowsInfoList) {
            Long bayId = smartVpsVslRowsInfo.getBayId();
            String vesselCode = smartVpsVslRowsInfo.getVesselCode();
            Integer rowNo = null;
            try {
                structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    logger.logError("排信息-排号", ValidatorUtil.isNull(smartVpsVslRowsInfo.getRowNo()));
                    rowNo = Integer.valueOf(smartVpsVslRowsInfo.getRowNo());
                    VMBay vmBay = structureData.getVMBayByBayId(bayId);
                    VMRow vmRow = new VMRow(vmBay.getBayId(), vmBay.getBayKey(), rowNo);
                    vmBay.addVMRow(vmRow);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")排(rowNo:" + rowNo + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseLocationInfo(List<SmartVpsVslLocationsInfo> smartVpsVslLocationsInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("船箱位信息", ValidatorUtil.isEmpty(smartVpsVslLocationsInfoList));
        StructureData structureData;
        for (SmartVpsVslLocationsInfo smartVpsVslLocationsInfo : smartVpsVslLocationsInfoList) {
            String vLocation = smartVpsVslLocationsInfo.getLocation();
            Long bayId = smartVpsVslLocationsInfo.getBayId();
            String vesselCode = smartVpsVslLocationsInfo.getVesselCode();
            String size = smartVpsVslLocationsInfo.getSize();
            try {
                structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    VMPosition vmPosition = new VMPosition(vLocation);
                    VMBay vmBay = structureData.getVMBayByBayId(bayId);
                    VMContainerSlot vmContainerSlot = new VMContainerSlot(vmPosition, vmBay, size);
                    structureData.addVMSlot(vmContainerSlot);
                    //要根据船箱位信息，初始化该倍位下每排的最大层号和最小层号
                    Integer rowNo = vmPosition.getRowNo();
                    VMRow vmRow = vmBay.getVMRowByRowNo(rowNo);
                    logger.logError("船箱位信息-查找不到排(" + rowNo + ")信息！", ValidatorUtil.isNull(vmRow));
                    vmRow.addVMSlot(vmContainerSlot);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")船箱位(vLocation:" + vLocation + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseHatchCoverInfo(List<SmartVpsVslHatchcoversInfo> smartVpsVslHatchCoversInfoList, AllRuntimeData allRuntimeData) {
        logger.logInfo("舱盖板信息", ValidatorUtil.isEmpty(smartVpsVslHatchCoversInfoList));
        StructureData structureData;
        for (SmartVpsVslHatchcoversInfo smartVpsVslHatchcoversInfo : smartVpsVslHatchCoversInfoList) {
            String vesselCode = smartVpsVslHatchcoversInfo.getVesselCode();
            structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
            if (structureData != null) {
                Long hatchCoverId = smartVpsVslHatchcoversInfo.getHatchCoverId();
                try {
                    VMHatchCover vmHatchCover = new VMHatchCover();
                    vmHatchCover.setHatchCoverNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getHatchCoverNo()));
                    vmHatchCover.setOpenSeq(smartVpsVslHatchcoversInfo.getOpenSeq());
                    vmHatchCover.setHatchFromRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getHatchFromRowNo()));
                    vmHatchCover.setHatchToRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getHatchToRowNo()));
                    vmHatchCover.setDeckFromRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getDeckFromRowNo()));
                    vmHatchCover.setDeckToRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getDeckToRowNo()));
                    vmHatchCover.setLeftCoverFather(smartVpsVslHatchcoversInfo.getLeftCoverFather());
                    vmHatchCover.setRightCoverFather(smartVpsVslHatchcoversInfo.getRightCoverFather());
                    vmHatchCover.setFrontCoverFather(smartVpsVslHatchcoversInfo.getFrontCoverFather());
                    vmHatchCover.setBehindCoverFather(smartVpsVslHatchcoversInfo.getBehiendCoverFather());
                    //舱盖板bayNo、hatchId
                    VMBay vmBayFrom = structureData.getVMBayByBayId(smartVpsVslHatchcoversInfo.getBayIdFrom());
                    VMBay vmBayTo = structureData.getVMBayByBayId(smartVpsVslHatchcoversInfo.getBayIdTo());
                    vmHatchCover.setBayNoFrom(vmBayFrom.getBayNo());
                    vmHatchCover.setBayNoTo(vmBayTo.getBayNo());
                    vmHatchCover.setHatchId(vmBayFrom.getHatchId());
                    //VMHatchCoverSlot
                    VMPosition vmPosition = new VMPosition((vmBayFrom.getBayNo() + vmBayTo.getBayNo()) / 2, vmHatchCover.getHatchCoverNo(), 50);
                    vmHatchCover.setvLocation(vmPosition.getVLocation());
                    vmHatchCover.setWeight(smartVpsVslHatchcoversInfo.getWeight());
                    VMHatchCoverSlot vmHatchCoverSlot = new VMHatchCoverSlot(vmPosition, vmHatchCover.getHatchId());
                    structureData.addVMSlot(vmHatchCoverSlot);
                    structureData.addVMHatchCover(vmHatchCover);
                } catch (Exception e) {
                    logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")舱盖板(Id:" + hatchCoverId + ")信息过程中发生数据异常！");
                    e.printStackTrace();
                }
            }
        }

    }

    private void parseMachineInfo(List<SmartVesselMachinesInfo> smartVesselMachinesInfoList, AllRuntimeData allRuntimeData) {
        for (SmartVesselMachinesInfo smartVesselMachinesInfo : smartVesselMachinesInfoList) {
            String vesselCode = smartVesselMachinesInfo.getVesselCode();
            Double machinePosition = smartVesselMachinesInfo.getMachinePosition();
            String machineType = smartVesselMachinesInfo.getMachineType();
            try {
                StructureData structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    logger.logError("船舶机械(machineType:" + machineType + ")-位置坐标为null", ValidatorUtil.isNull(machinePosition));
                    VMMachine vmMachine = new VMMachine();
                    vmMachine = (VMMachine) BeanCopyUtil.copyBean(smartVesselMachinesInfo, vmMachine);
                    structureData.addVMMachine(vmMachine);
                }
            } catch (Exception e) {
                logger.logError("解析船舶机械(vesselCode:" + vesselCode + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseCraneWorkFlow(List<SmartCraneWorkFlowInfo> smartCraneWorkFlowInfoList, AllRuntimeData allRuntimeData) {
        logger.logInfo("舱作业工艺设置", ValidatorUtil.isEmpty(smartCraneWorkFlowInfoList));
        WorkingData workingData;
        for (SmartCraneWorkFlowInfo smartCraneWorkFlowInfo : smartCraneWorkFlowInfoList) {
            Long berthId = smartCraneWorkFlowInfo.getBerthId();
            Long hatchId = smartCraneWorkFlowInfo.getHatchId();
            String ldStrategy = smartCraneWorkFlowInfo.getLdStrategy();
            String aboveOrBelow = smartCraneWorkFlowInfo.getDeckOrHatch();
            ldStrategy = ldStrategy != null ? ldStrategy.equals("LD") ? CWPDomain.LD_STRATEGY_LD : CWPDomain.LD_STRATEGY_BLD : CWPDomain.LD_STRATEGY_BLD;
            try {
                aboveOrBelow = aboveOrBelow.equals("D") ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
                workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    CMCraneWorkFlow cmCraneWorkFlow = new CMCraneWorkFlow(hatchId, aboveOrBelow, CWPDomain.DL_TYPE_DISC);
                    cmCraneWorkFlow.setSingle(smartCraneWorkFlowInfo.getSingle());
                    cmCraneWorkFlow.setTwin(smartCraneWorkFlowInfo.getTwin());
//                    cmCraneWorkFlow.setTandem(true);
                    cmCraneWorkFlow.setTandem(smartCraneWorkFlowInfo.getTandem());
                    cmCraneWorkFlow.setLdStrategy(ldStrategy);
                    workingData.addCMCraneWorkFlow(cmCraneWorkFlow);
                    CMCraneWorkFlow cmCraneWorkFlowL = new CMCraneWorkFlow(hatchId, aboveOrBelow, CWPDomain.DL_TYPE_LOAD);
                    cmCraneWorkFlowL.setSingle(smartCraneWorkFlowInfo.getSingle());
                    cmCraneWorkFlowL.setTwin(smartCraneWorkFlowInfo.getTwin());
                    cmCraneWorkFlowL.setTandem(Boolean.FALSE);
                    cmCraneWorkFlowL.setLdStrategy(ldStrategy);
                    workingData.addCMCraneWorkFlow(cmCraneWorkFlowL);
                }
            } catch (Exception e) {
                logger.logError("解析桥作业工艺设置(berthId:" + berthId + ", hatchId:" + hatchId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseParameter(List<SmartCwpParameterInfo> smartCwpParameterInfoList, AllRuntimeData allRuntimeData) {
        //test
        if (ValidatorUtil.isEmpty(smartCwpParameterInfoList)) {
            for (Long berthId : allRuntimeData.getAllBerthId()) {
                SmartCwpParameterInfo smartCwpParameterInfo = new SmartCwpParameterInfo();
                smartCwpParameterInfo.setBerthId(berthId);
                smartCwpParameterInfoList.add(smartCwpParameterInfo);
            }
        }
        logger.logError("输入数据中没有CWP算法配置参数信息", ValidatorUtil.isEmpty(smartCwpParameterInfoList));
        for (SmartCwpParameterInfo smartCwpParameterInfo : smartCwpParameterInfoList) {
            Long berthId = smartCwpParameterInfo.getBerthId();
            try {
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    CwpConfig cwpConfig = new CwpConfig();

                    //特殊因素影响效率的影响因子，1.0
                    Double impactFactor = smartCwpParameterInfo.getImpactFactor();
                    impactFactor = impactFactor != null ? impactFactor : CWPDefaultValue.impactFactor;
                    cwpConfig.setImpactFactor(impactFactor);

                    //重量差，千克kg
                    Integer twinWeightDiff = smartCwpParameterInfo.getTwinWeightDiff();
                    twinWeightDiff = twinWeightDiff != null ? twinWeightDiff : CWPDefaultValue.twinWeightDiff;
                    cwpConfig.setTwinWeightDiff(twinWeightDiff);
                    //桥机安全距离，14米
                    Double craneSafeSpan = smartCwpParameterInfo.getSafeDistance();
                    craneSafeSpan = craneSafeSpan != null ? craneSafeSpan : CWPDefaultValue.craneSafeSpan;
                    cwpConfig.setSafeDistance(craneSafeSpan);
                    //桥机跨机械起趴大梁移动时间，900s
                    Long crossBarTime = smartCwpParameterInfo.getCrossBarTime();
                    crossBarTime = crossBarTime != null ? crossBarTime : CWPDefaultValue.crossBarTime;
                    cwpConfig.setCrossBarTime(crossBarTime);
                    //桥机移动速度，0.75m/s
                    Double craneSpeed = smartCwpParameterInfo.getCraneMoveSpeed();
                    craneSpeed = craneSpeed != null ? craneSpeed : CWPDefaultValue.craneSpeed;
                    cwpConfig.setCraneMoveSpeed(craneSpeed);
                    //甲板上拆锁时间，甲板五层高及以上集装箱拆锁用时，90s
                    Long unlockTwistTime = smartCwpParameterInfo.getUnlockTwistTime();
                    unlockTwistTime = unlockTwistTime != null ? unlockTwistTime : CWPDefaultValue.unlockTwistTime;
                    cwpConfig.setUnlockTwistTime((long) (unlockTwistTime * impactFactor));

                    //桥机作业单块舱盖板时间，240s
                    String hatchCoverTime = smartCwpParameterInfo.getHatchCoverTime();
                    Long hatchCoverTimeL = CWPDefaultValue.hatchCoverOpenTime;
                    Long hatchCoverTimeD = CWPDefaultValue.hatchCoverOpenTime;
                    List<Long> values = StringUtil.getTwoTimeValues(hatchCoverTime);
                    if (values.size() == 2) {
                        hatchCoverTimeL = values.get(0);
                        hatchCoverTimeD = values.get(1);
                    }
                    cwpConfig.setHatchCoverTimeL((long) (hatchCoverTimeL * impactFactor));
                    cwpConfig.setHatchCoverTimeD((long) (hatchCoverTimeD * impactFactor));
                    //单20尺垫脚箱作业用时，180s
                    String single20FootPadTime = smartCwpParameterInfo.getSingle20FootPadTime();
                    Long single20FootPadTimeL = CWPDefaultValue.single20FootPadTime;
                    Long single20FootPadTimeD = CWPDefaultValue.single20FootPadTime;
                    values = StringUtil.getTwoTimeValues(single20FootPadTime);
                    if (values.size() == 2) {
                        single20FootPadTimeL = values.get(0);
                        single20FootPadTimeD = values.get(1);
                    }
                    cwpConfig.setSingle20FootPadTimeL((long) (single20FootPadTimeL * impactFactor));
                    cwpConfig.setSingle20FootPadTimeD((long) (single20FootPadTimeD * impactFactor));
                    //单20尺全隔槽作业用时，180s
                    String single20SeparateTime = smartCwpParameterInfo.getSingle20SeparateTime();
                    Long single20SeparateTimeL = CWPDefaultValue.single20SeparateTime;
                    Long single20SeparateTimeD = CWPDefaultValue.single20SeparateTime;
                    values = StringUtil.getTwoTimeValues(single20SeparateTime);
                    if (values.size() == 2) {
                        single20SeparateTimeL = values.get(0);
                        single20SeparateTimeD = values.get(1);
                    }
                    cwpConfig.setSingle20SeparateTimeL((long) (single20SeparateTimeL * impactFactor));
                    cwpConfig.setSingle20SeparateTimeD((long) (single20SeparateTimeD * impactFactor));

                    //单20尺普通箱作业用时，120s
                    String single20Time = smartCwpParameterInfo.getSingle20Time();
                    Long single20TimeAL = CWPDefaultValue.single20Time;
                    Long single20TimeAD = CWPDefaultValue.single20Time;
                    Long single20TimeBL = CWPDefaultValue.single20Time;
                    Long single20TimeBD = CWPDefaultValue.single20Time;
                    values = StringUtil.getFourTimeValues(single20Time);
                    if (values.size() == 4) {
                        single20TimeAL = values.get(0);
                        single20TimeAD = values.get(1);
                        single20TimeBL = values.get(2);
                        single20TimeBD = values.get(3);
                    }
                    cwpConfig.setSingle20TimeAL((long) (single20TimeAL * impactFactor));
                    cwpConfig.setSingle20TimeAD((long) (single20TimeAD * impactFactor));
                    cwpConfig.setSingle20TimeBL((long) (single20TimeBL * impactFactor));
                    cwpConfig.setSingle20TimeBD((long) (single20TimeBD * impactFactor));
                    //单40尺普通箱作业用时，120s
                    String single40Time = smartCwpParameterInfo.getSingle40Time();
                    Long single40TimeAL = CWPDefaultValue.single40Time;
                    Long single40TimeAD = CWPDefaultValue.single40Time;
                    Long single40TimeBL = CWPDefaultValue.single40Time;
                    Long single40TimeBD = CWPDefaultValue.single40Time;
                    values = StringUtil.getFourTimeValues(single40Time);
                    if (values.size() == 4) {
                        single40TimeAL = values.get(0);
                        single40TimeAD = values.get(1);
                        single40TimeBL = values.get(2);
                        single40TimeBD = values.get(3);
                    }
                    cwpConfig.setSingle40TimeAL((long) (single40TimeAL * impactFactor));
                    cwpConfig.setSingle40TimeAD((long) (single40TimeAD * impactFactor));
                    cwpConfig.setSingle40TimeBL((long) (single40TimeBL * impactFactor));
                    cwpConfig.setSingle40TimeBD((long) (single40TimeBD * impactFactor));
                    //单45尺普通箱作业用时，120s
                    String single45Time = smartCwpParameterInfo.getSingle45Time();
                    Long single45TimeAL = CWPDefaultValue.single45Time;
                    Long single45TimeAD = CWPDefaultValue.single45Time;
                    Long single45TimeBL = CWPDefaultValue.single45Time;
                    Long single45TimeBD = CWPDefaultValue.single45Time;
                    values = StringUtil.getFourTimeValues(single45Time);
                    if (values.size() == 4) {
                        single45TimeAL = values.get(0);
                        single45TimeAD = values.get(1);
                        single45TimeBL = values.get(2);
                        single45TimeBD = values.get(3);
                    }
                    cwpConfig.setSingle45TimeAL((long) (single45TimeAL * impactFactor));
                    cwpConfig.setSingle45TimeAD((long) (single45TimeAD * impactFactor));
                    cwpConfig.setSingle45TimeBL((long) (single45TimeBL * impactFactor));
                    cwpConfig.setSingle45TimeBD((long) (single45TimeBD * impactFactor));
                    //双20尺普通箱作业用时，150s
                    String double20Time = smartCwpParameterInfo.getDouble20Time();
                    Long double20TimeAL = CWPDefaultValue.double20Time;
                    Long double20TimeAD = CWPDefaultValue.double20Time;
                    Long double20TimeBL = CWPDefaultValue.double20Time;
                    Long double20TimeBD = CWPDefaultValue.double20Time;
                    values = StringUtil.getFourTimeValues(double20Time);
                    if (values.size() == 4) {
                        double20TimeAL = values.get(0);
                        double20TimeAD = values.get(1);
                        double20TimeBL = values.get(2);
                        double20TimeBD = values.get(3);
                    }
                    cwpConfig.setDouble20TimeAL((long) (double20TimeAL * impactFactor));
                    cwpConfig.setDouble20TimeAD((long) (double20TimeAD * impactFactor));
                    cwpConfig.setDouble20TimeBL((long) (double20TimeBL * impactFactor));
                    cwpConfig.setDouble20TimeBD((long) (double20TimeBD * impactFactor));
                    //双吊具40尺作业用时，140s
                    String double40Time = smartCwpParameterInfo.getDouble40Time();
                    Long double40TimeAL = CWPDefaultValue.double40Time;
                    Long double40TimeAD = CWPDefaultValue.double40Time;
                    Long double40TimeBL = CWPDefaultValue.double40Time;
                    Long double40TimeBD = CWPDefaultValue.double40Time;
                    values = StringUtil.getFourTimeValues(double40Time);
                    if (values.size() == 4) {
                        double40TimeAL = values.get(0);
                        double40TimeAD = values.get(1);
                        double40TimeBL = values.get(2);
                        double40TimeBD = values.get(3);
                    }
                    cwpConfig.setDouble40TimeAL((long) (double40TimeAL * impactFactor));
                    cwpConfig.setDouble40TimeAD((long) (double40TimeAD * impactFactor));
                    cwpConfig.setDouble40TimeBL((long) (double40TimeBL * impactFactor));
                    cwpConfig.setDouble40TimeBD((long) (double40TimeBD * impactFactor));
                    //双吊具45尺作业用时，140s
                    String double45Time = smartCwpParameterInfo.getDouble45Time();
                    Long double45TimeAL = CWPDefaultValue.double45Time;
                    Long double45TimeAD = CWPDefaultValue.double45Time;
                    Long double45TimeBL = CWPDefaultValue.double45Time;
                    Long double45TimeBD = CWPDefaultValue.double45Time;
                    values = StringUtil.getFourTimeValues(double45Time);
                    if (values.size() == 4) {
                        double45TimeAL = values.get(0);
                        double45TimeAD = values.get(1);
                        double45TimeBL = values.get(2);
                        double45TimeBD = values.get(3);
                    }
                    cwpConfig.setDouble45TimeAL((long) (double45TimeAL * impactFactor));
                    cwpConfig.setDouble45TimeAD((long) (double45TimeAD * impactFactor));
                    cwpConfig.setDouble45TimeBL((long) (double45TimeBL * impactFactor));
                    cwpConfig.setDouble45TimeBD((long) (double45TimeBD * impactFactor));
                    //超限箱、分体大件作业用时，360s
                    String specialCntTime = smartCwpParameterInfo.getSpecialCntTime();
                    Long specialCntTimeAL = CWPDefaultValue.specialCntTime;
                    Long specialCntTimeAD = CWPDefaultValue.specialCntTime;
                    Long specialCntTimeBL = CWPDefaultValue.specialCntTime;
                    Long specialCntTimeBD = CWPDefaultValue.specialCntTime;
                    values = StringUtil.getFourTimeValues(specialCntTime);
                    if (values.size() == 4) {
                        specialCntTimeAL = values.get(0);
                        specialCntTimeAD = values.get(1);
                        specialCntTimeBL = values.get(2);
                        specialCntTimeBD = values.get(3);
                    }
                    cwpConfig.setSpecialCntTimeAL((long) (specialCntTimeAL * impactFactor));
                    cwpConfig.setSpecialCntTimeAD((long) (specialCntTimeAD * impactFactor));
                    cwpConfig.setSpecialCntTimeBL((long) (specialCntTimeBL * impactFactor));
                    cwpConfig.setSpecialCntTimeBD((long) (specialCntTimeBD * impactFactor));
                    //直装直提危险品作业用时，360s
                    String dangerCntTime = smartCwpParameterInfo.getDangerCntTime();
                    Long dangerCntTimeAL = CWPDefaultValue.dangerCntTime;
                    Long dangerCntTimeAD = CWPDefaultValue.dangerCntTime;
                    Long dangerCntTimeBL = CWPDefaultValue.dangerCntTime;
                    Long dangerCntTimeBD = CWPDefaultValue.dangerCntTime;
                    values = StringUtil.getFourTimeValues(dangerCntTime);
                    if (values.size() == 4) {
                        dangerCntTimeAL = values.get(0);
                        dangerCntTimeAD = values.get(1);
                        dangerCntTimeBL = values.get(2);
                        dangerCntTimeBD = values.get(3);
                    }
                    cwpConfig.setDangerCntTimeAL((long) (dangerCntTimeAL * impactFactor));
                    cwpConfig.setDangerCntTimeAD((long) (dangerCntTimeAD * impactFactor));
                    cwpConfig.setDangerCntTimeBL((long) (dangerCntTimeBL * impactFactor));
                    cwpConfig.setDangerCntTimeBD((long) (dangerCntTimeBD * impactFactor));
                    //桥机换倍船扫时间，300s
                    Long hatchScanTime = smartCwpParameterInfo.getHatchScanTime();
                    hatchScanTime = hatchScanTime != null ? hatchScanTime : CWPDefaultValue.hatchScanTime;
                    cwpConfig.setHatchScanTime((long) (hatchScanTime * impactFactor));

                    //所有桥机平均效率，30关/小时
                    cwpConfig.setOneCntTime((long) (cwpConfig.getDouble20TimeAL() + cwpConfig.getSingle40TimeAL()) / 2);

                    //是否过驾驶台起大梁
                    Boolean crossBridge = smartCwpParameterInfo.getSetupBridge();
                    crossBridge = crossBridge != null ? crossBridge : CWPDefaultValue.crossBridge;
                    cwpConfig.setSetupBridge(crossBridge);
                    //是否过烟囱起大梁
                    Boolean crossChimney = smartCwpParameterInfo.getSetupChimney();
                    crossChimney = crossChimney != null ? crossChimney : CWPDefaultValue.crossChimney;
                    cwpConfig.setSetupChimney(crossChimney);
                    //装卸策略，即边装边卸：BLD、一般装卸：LD
                    String ldStrategy = smartCwpParameterInfo.getLdStrategy();
                    ldStrategy = StringUtil.isNotBlank(ldStrategy) ? ldStrategy.equals("LD") ? CWPDomain.LD_STRATEGY_LD : CWPDomain.LD_STRATEGY_BLD : CWPDomain.LD_STRATEGY_BLD;
                    cwpConfig.setLdStrategy(ldStrategy);
                    //建议开路数
                    Integer craneAdviceNumber = smartCwpParameterInfo.getCraneAdviceNumber();
                    cwpConfig.setCraneAdviceNumber(craneAdviceNumber);
                    //回收指令标记，控制是否回收队列中的指令，Y表示回收，N表示不回收
                    String recycleCntWiFlag = smartCwpParameterInfo.getRecycleCntWiFlag();
                    recycleCntWiFlag = StringUtil.isNotBlank(recycleCntWiFlag) ? CWPDomain.YES.equals(recycleCntWiFlag) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.recycleCntWiFlag;
                    cwpConfig.setRecycleCntWiFlag(recycleCntWiFlag);
                    //桥机第一次开路倍位，四部桥机101、102、103、104，开路倍位设置依次为格式："02,10,00,49"倍位之间用","隔开，"00"代表对应桥机不设置开路倍位
                    cwpConfig.setCraneAdviceWorkBayNos(smartCwpParameterInfo.getCraneAdviceWorkBayNos());

                    //船舶开路装卸平衡考虑参数。首次开路全装、首次开路全卸、首次开路装卸错开:"L"、"D"、"LD"
                    String loadPrior = smartCwpParameterInfo.getLoadPrior();
                    cwpConfig.setLoadPrior(loadPrior);
                    //均衡每部桥吊的作业量，整船桥吊同时完工，Y/N
                    String craneSameWorkTime = smartCwpParameterInfo.getCraneSameWorkTime();
                    craneSameWorkTime = StringUtil.isNotBlank(craneSameWorkTime) ? CWPDomain.YES.equals(craneSameWorkTime) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.craneSameWorkTime;
                    cwpConfig.setCraneSameWorkTime(craneSameWorkTime);
                    //避免开工作业甲板装船箱，避让时间为开工后一小时，Y/N
                    String deckWorkLater = smartCwpParameterInfo.getDeckWorkLater();
                    deckWorkLater = StringUtil.isNotBlank(deckWorkLater) ? CWPDomain.YES.equals(deckWorkLater) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.deckWorkLater;
                    cwpConfig.setDeckWorkLater(deckWorkLater);
                    //重点路单桥吊持续作业，其余箱量由左右桥吊分配， Y/N
                    String mainRoadOneCrane = smartCwpParameterInfo.getMainRoadOneCrane();
                    mainRoadOneCrane = StringUtil.isNotBlank(mainRoadOneCrane) ? CWPDomain.YES.equals(mainRoadOneCrane) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.mainRoadOneCrane;
                    cwpConfig.setMainRoadOneCrane(mainRoadOneCrane);
                    //分割舱优先作业设定，Y/N
                    String dividedHatchFirst = smartCwpParameterInfo.getDividedHatchFirst();
                    dividedHatchFirst = StringUtil.isNotBlank(dividedHatchFirst) ? CWPDomain.YES.equals(dividedHatchFirst) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.dividedHatchFirst;
                    cwpConfig.setDividedHatchFirst(dividedHatchFirst);
                    ////桥机作业量不平均（中间桥机晚结束，两边桥机早结束，分割舱最少），Y/N
                    String craneNotSameWorkTime = smartCwpParameterInfo.getCraneNotSameWorkTime();
                    craneNotSameWorkTime = StringUtil.isNotBlank(craneNotSameWorkTime) ? CWPDomain.YES.equals(craneNotSameWorkTime) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.craneNotSameWorkTime;
                    cwpConfig.setCraneNotSameWorkTime(craneNotSameWorkTime);
                    //劈路原则，Y/N
                    String splitRoad = smartCwpParameterInfo.getSplitRoad();
                    splitRoad = StringUtil.isNotBlank(splitRoad) ? CWPDomain.YES.equals(splitRoad) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.splitRoad;
                    cwpConfig.setSplitRoad(splitRoad);

                    // 双吊具控制参数
                    Double allContainerWeight = smartCwpParameterInfo.getAllContainerWeight();
                    allContainerWeight = allContainerWeight != null ? allContainerWeight : CWPDefaultValue.allContainerWeight;
                    cwpConfig.setAllContainerWeight(allContainerWeight);
                    Double tandemWeightDiff = smartCwpParameterInfo.getTandemWeightDiff();
                    tandemWeightDiff = tandemWeightDiff != null ? tandemWeightDiff : CWPDefaultValue.tandemWeightDiff;
                    cwpConfig.setTandemWeightDiff(tandemWeightDiff);
                    Double tandemHeightDiff = smartCwpParameterInfo.getTandemHeightDiff();
                    tandemHeightDiff = tandemHeightDiff != null ? tandemHeightDiff : CWPDefaultValue.tandemHeightDiff;
                    cwpConfig.setTandemHeightDiff(tandemHeightDiff);
                    String tandemContainerType = smartCwpParameterInfo.getTandemContainerType();
                    tandemContainerType = StringUtil.isNotBlank(tandemContainerType) ? tandemContainerType : CWPDefaultValue.tandemContainerType;
                    cwpConfig.setTandemContainerType(tandemContainerType);
                    String tandemDangerCnt = smartCwpParameterInfo.getTandemDangerCnt();
                    tandemDangerCnt = StringUtil.isNotBlank(tandemDangerCnt) ? tandemDangerCnt : CWPDefaultValue.tandemDangerCnt;
                    cwpConfig.setTandemDangerCnt(tandemDangerCnt);

                    String multiThreadFlag = smartCwpParameterInfo.getMultiThreadFlag();
                    multiThreadFlag = StringUtil.isNotBlank(multiThreadFlag) ? multiThreadFlag : CWPDefaultValue.multiThreadFlag;
                    cwpConfig.setMultiThreadFlag(multiThreadFlag);

                    workingData.setCwpConfig(cwpConfig);
                }
            } catch (Exception e) {
                logger.logError("解析算法配置参数(berthId:" + berthId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseVesselContainer(List<SmartVesselContainerInfo> smartVesselContainerInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("进出口船图箱信息", ValidatorUtil.isEmpty(smartVesselContainerInfoList));
        WorkingData workingData;
        for (SmartVesselContainerInfo smartVesselContainerInfo : smartVesselContainerInfoList) {
            Long berthId = smartVesselContainerInfo.getBerthId();
            String vLocation = smartVesselContainerInfo.getvLocation();
            Long vpcCntId = smartVesselContainerInfo.getVpcCntrId();
            String size = smartVesselContainerInfo.getcSzCsizecd();
            String type = smartVesselContainerInfo.getcTypeCd();
            String dlType = smartVesselContainerInfo.getLduldfg();
            String throughFlag = smartVesselContainerInfo.getThroughFlag();
            Long hatchId = smartVesselContainerInfo.getHatchId();
            String workFlow = smartVesselContainerInfo.getWorkflow();
            Long moveOrder = smartVesselContainerInfo.getCwpwkMoveNum();
            Long cntWorkTime = smartVesselContainerInfo.getContainerWorkInterval(); //单位秒
            Double weight = smartVesselContainerInfo.getWeight();
            String dgCd = smartVesselContainerInfo.getDtpDnggcd(); //危险品：
            String isHeight = smartVesselContainerInfo.getIsHeight(); //是否是高箱：Y/N
            String cntHeight = smartVesselContainerInfo.getCntHeightDesc(); //箱子具体高度
            String rfFlag = smartVesselContainerInfo.getRfcfg(); //冷藏标记：Y/N
            String overrunCd = smartVesselContainerInfo.getOvlmtcd(); //超限箱标记：Y/N
            String workStatus = smartVesselContainerInfo.getWorkStatus();
            throughFlag = "N".equals(throughFlag) ? CWPDomain.THROUGH_NO : CWPDomain.THROUGH_YES;
            size = "53".equals(size) ? "43" : size;
            try {
                workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    VMContainer vmContainer = new VMContainer(vLocation, dlType);
                    vmContainer.setThroughFlag(throughFlag);
                    vmContainer.setVpcCntId(vpcCntId);
                    vmContainer.setYardContainerId(smartVesselContainerInfo.getYardContainerId());
                    vmContainer.setBerthId(berthId);
                    vmContainer.setHatchId(hatchId);
                    vmContainer.setCntHeight(cntHeight);
                    vmContainer.setCntWorkTime(cntWorkTime);
                    vmContainer.setWeightKg(weight);
                    vmContainer.setSize(size);
                    vmContainer.setType(type);
                    vmContainer.setDgCd(dgCd);
                    vmContainer.setIsHeight(isHeight);
                    vmContainer.setRfFlag(rfFlag);
                    vmContainer.setOverrunCd(overrunCd);
                    vmContainer.setReStowType(smartVesselContainerInfo.getReStowType());
                    vmContainer.setEfFlag(smartVesselContainerInfo.getEffg());
                    if (CWPDomain.THROUGH_NO.equals(throughFlag)) { //非过境箱
                        //人工指定作业工艺
                        vmContainer.setCwoManualWorkflowTemp(workFlow);
                        vmContainer.setCwoManualSeqNoTemp(moveOrder);
                        if ("Y".equals(smartVesselContainerInfo.getCwoManualWorkflow())) {
                            if (StringUtil.isNotBlank(workFlow)) {
                                vmContainer.setWorkFlow(PublicMethod.getWorkFlowStr1(workFlow, size));
                                vmContainer.setCwoManualWorkflow(smartVesselContainerInfo.getCwoManualWorkflow());
                            } else {
                                logger.logError("人工指定了(vLocation: " + vLocation + ")作业工艺，但是工艺字段为空：" + workFlow);
                            }
                        }
                        if ("Y".equals(smartVesselContainerInfo.getCwoManualSeqno())) {
                            if (moveOrder != null) {
                                vmContainer.setMoveOrder(moveOrder);
                                vmContainer.setWorkFirst(CWPDomain.YES);
                            } else {
                                logger.logError("人工指定了(vLocation: " + vLocation + ")作业顺序，但是顺序字段为空：" + workFlow);
                            }
                        }
                        if (!(CWPCntDomain.C.equals(workStatus) || CWPCntDomain.RC.equals(workStatus))) {
                            if (CWPCntDomain.A.equals(workStatus) || CWPCntDomain.W.equals(workStatus)) { //作业、队列中的箱子，需要考虑是否需要回收重排
                                vmContainer.setWorkStatus(workStatus);
                                vmContainer.setMoveStage(smartVesselContainerInfo.getMoveStage());
                                vmContainer.setDispatchedTask(smartVesselContainerInfo.getDispatchedTask());
                                vmContainer.setCanRecycleFlag(smartVesselContainerInfo.getCanRecycleFlag());
                                vmContainer.setCwpBlockId(smartVesselContainerInfo.getCwpBlockId());
                                vmContainer.setCraneNo(smartVesselContainerInfo.getCraneNo());
                                vmContainer.setPlanWorkFlow(workFlow);
                                vmContainer.setPlanMoveOrder(moveOrder);
                                workingData.getSentContainerList().add(vmContainer);
                            } else {
                                workingData.putVMContainer(new VMPosition(vLocation), vmContainer);
                            }
                        }
                        // 先保存卸船出翻舱箱信息
                        if (CWPDomain.RE_STOW_VY.equals(vmContainer.getReStowType()) && vmContainer.getYardContainerId() != null && CWPDomain.DL_TYPE_DISC.equals(vmContainer.getDlType())) {
                            workingData.getReStowContainerMapD().put(vmContainer.getYardContainerId(), vmContainer);
                        }
                        // 根据在场箱Id保存装船箱子信息
                        if (vmContainer.getYardContainerId() != null && CWPDomain.DL_TYPE_LOAD.equals(vmContainer.getDlType())) {
//                            workingData.getReStowContainerMapL().put(vmContainer.getYardContainerId(), vmContainer);
                        }
                    } else { // 过境箱也需要存起来，后面有用
                        workingData.putThroughVMContainer(new VMPosition(vLocation), vmContainer);
                    }
                }
            } catch (Exception e) {
                logger.logError("解析进出口船图箱(berthId:" + berthId + ", vLocation:" + vLocation + ", dlType:" + dlType + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseVesselContainerAmount(List<SmartVesselContainerAmountInfo> smartVesselContainerAmountInfoList, List<SmartVesselContainerInfo> smartVesselContainerInfoList, AllRuntimeData allRuntimeData) {
        logger.logInfo("船舶进出口船图箱量信息", ValidatorUtil.isEmpty(smartVesselContainerAmountInfoList));
        WorkingData workingData;
        for (SmartVesselContainerAmountInfo smartVesselContainerAmountInfo : smartVesselContainerAmountInfoList) {
            Long berthId = smartVesselContainerAmountInfo.getBerthId();
            workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
            if (workingData != null) {
                VMContainerAmount vmContainerAmount = new VMContainerAmount(berthId);
                vmContainerAmount.setSize(smartVesselContainerAmountInfo.getSize());
                vmContainerAmount.setDlType(smartVesselContainerAmountInfo.getDlType());
                vmContainerAmount.setContainerAmount(smartVesselContainerAmountInfo.getContainerAmount());
                workingData.getVmContainerAmountList().add(vmContainerAmount);
            }
        }
        // 如果某些船舶的SmartVesselContainerAmountInfo数据没有，则从SmartVesselContainerInfo箱数据中统计出来
        for (WorkingData workingData1 : allRuntimeData.getAllWorkingDataList()) {
            if (workingData1.getVmContainerAmountList().size() == 0) {
                int load2Amount = 0;
                int load4Amount = 0;
                int disc2Amount = 0;
                int disc4Amount = 0;
                for (SmartVesselContainerInfo smartVesselContainerInfo : smartVesselContainerInfoList) {
                    if (workingData1.getVmSchedule().getBerthId().equals(smartVesselContainerInfo.getBerthId())) {
                        if (smartVesselContainerInfo.getcSzCsizecd().startsWith("2") && "L".equals(smartVesselContainerInfo.getLduldfg())) {
                            load2Amount += 1;
                        }
                        if (smartVesselContainerInfo.getcSzCsizecd().startsWith("4") && "L".equals(smartVesselContainerInfo.getLduldfg())) {
                            load4Amount += 1;
                        }
                        if (smartVesselContainerInfo.getcSzCsizecd().startsWith("2") && "D".equals(smartVesselContainerInfo.getLduldfg())) {
                            disc2Amount += 1;
                        }
                        if (smartVesselContainerInfo.getcSzCsizecd().startsWith("4") && "D".equals(smartVesselContainerInfo.getLduldfg())) {
                            disc4Amount += 1;
                        }
                    }
                }
                VMContainerAmount vmContainerAmount2l = new VMContainerAmount(workingData1.getVmSchedule().getBerthId(), CWPDomain.DL_TYPE_LOAD, "2", load2Amount);
                VMContainerAmount vmContainerAmount4l = new VMContainerAmount(workingData1.getVmSchedule().getBerthId(), CWPDomain.DL_TYPE_LOAD, "4", load4Amount);
                VMContainerAmount vmContainerAmount2d = new VMContainerAmount(workingData1.getVmSchedule().getBerthId(), CWPDomain.DL_TYPE_DISC, "2", disc2Amount);
                VMContainerAmount vmContainerAmount4d = new VMContainerAmount(workingData1.getVmSchedule().getBerthId(), CWPDomain.DL_TYPE_DISC, "4", disc4Amount);
                workingData1.getVmContainerAmountList().add(vmContainerAmount2l);
                workingData1.getVmContainerAmountList().add(vmContainerAmount4l);
                workingData1.getVmContainerAmountList().add(vmContainerAmount2d);
                workingData1.getVmContainerAmountList().add(vmContainerAmount4d);
            }
        }
    }

    private void parseCranePool(List<SmartVesselCranePoolInfo> smartVesselCranePoolInfoList, List<SmartCranePoolInfo> smartCranePoolInfoList, List<SmartCraneFirstWorkInfo> smartCraneFirstWorkInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("船舶桥机池信息", ValidatorUtil.isEmpty(smartVesselCranePoolInfoList));
        for (SmartVesselCranePoolInfo smartVesselCranePoolInfo : smartVesselCranePoolInfoList) {
            Long berthId = smartVesselCranePoolInfo.getBerthId();
            Long poolId = smartVesselCranePoolInfo.getPoolId();
            try {
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    for (SmartCranePoolInfo smartCranePoolInfo : smartCranePoolInfoList) {
                        if (poolId.equals(smartCranePoolInfo.getPoolId())) {
                            String craneNo = smartCranePoolInfo.getCraneNo();
                            logger.logError("桥机池中桥机号为null", ValidatorUtil.isNull(craneNo));
                            CMCranePool cmCranePool = new CMCranePool(poolId, craneNo);
                            cmCranePool = (CMCranePool) BeanCopyUtil.copyBean(smartCranePoolInfo, cmCranePool);
                            if (StringUtil.isNotBlank(smartVesselCranePoolInfo.getFirstCraneNos())) {
                                cmCranePool.setFirstCraneFlag(smartVesselCranePoolInfo.getFirstCraneNos().contains(craneNo));
                            } else {
                                cmCranePool.setFirstCraneFlag(Boolean.TRUE);
                            }
                            //桥机第一次选择的倍位、关号
                            for (SmartCraneFirstWorkInfo smartCraneFirstWorkInfo : smartCraneFirstWorkInfoList) {
                                if (berthId.equals(smartCraneFirstWorkInfo.getBerthId()) && craneNo.equals(smartCraneFirstWorkInfo.getCraneNo())) {
                                    if (StringUtil.isNotBlank(smartCraneFirstWorkInfo.getFirstWorkBayNo()) && smartCraneFirstWorkInfo.getFirstWorkAmount() != null) {
                                        cmCranePool.setFirstWorkBayNo(Integer.valueOf(smartCraneFirstWorkInfo.getFirstWorkBayNo()));
                                        cmCranePool.setFirstWorkAmount(smartCraneFirstWorkInfo.getFirstWorkAmount());
                                        break;
                                    }
                                }
                            }
                            workingData.addCMCranePool(cmCranePool);
                        }
                    }
                    if (workingData.getAllCMCranePools().size() == 0) {
                        logger.logError("船舶桥机池中(berthId:" + berthId + ", poolId:" + poolId + ")没有桥机信息");
                    }
                }
            } catch (Exception e) {
                logger.logError("解析船舶桥机池(berthId:" + berthId + ", poolId:" + poolId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseCraneAddOrDelete(List<SmartCraneAddOrDelInfo> smartCraneAddOrDelInfoList, AllRuntimeData allRuntimeData) {
        logger.logInfo("加减桥机信息", ValidatorUtil.isEmpty(smartCraneAddOrDelInfoList));
        for (SmartCraneAddOrDelInfo smartCraneAddOrDelInfo : smartCraneAddOrDelInfoList) {
            Long berthId = smartCraneAddOrDelInfo.getBerthId();
            try {
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    if (smartCraneAddOrDelInfo.getAddOrDelDate().compareTo(workingData.getVmSchedule().getPlanBeginWorkTime()) > 0
                            || smartCraneAddOrDelInfo.getAddOrDelDate().compareTo(workingData.getVmSchedule().getPlanEndWorkTime()) < 0) {
                        CMCraneAddOrDelete cmCraneAddOrDelete = new CMCraneAddOrDelete();
                        cmCraneAddOrDelete = (CMCraneAddOrDelete) BeanCopyUtil.copyBean(smartCraneAddOrDelInfo, cmCraneAddOrDelete);
                        workingData.getCmCraneAddOrDeleteList().add(cmCraneAddOrDelete);
                    } else {
                        logger.logError("加减桥机的时间(" + smartCraneAddOrDelInfo.getAddOrDelDate().toString() + ")在船期时间之外，信息无效！请检查加减桥机时间是否符合船期时间！");
                    }
                }
            } catch (Exception e) {
                logger.logError("解析加减桥机(berthId:" + berthId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseCraneManual(List<SmartCraneManualInfo> smartCraneManualInfoList, AllRuntimeData allRuntimeData) {
        WorkingData workingData = null;
        for (SmartCraneManualInfo smartCraneManualInfo : smartCraneManualInfoList) {
            workingData = allRuntimeData.getWorkingDataByBerthId(smartCraneManualInfo.getBerthId());
            if (workingData != null) {
                CMCraneManual cmCraneManual = new CMCraneManual();
                cmCraneManual = (CMCraneManual) BeanCopyUtil.copyBean(smartCraneManualInfo, cmCraneManual);
                workingData.setCmCraneManual(cmCraneManual);
            }
        }
    }

    private void parseCraneMaintainPlan(List<SmartCraneMaintainPlanInfo> smartCraneMaintainPlanInfoList, MachineData machineData) {
        for (SmartCraneMaintainPlanInfo smartCraneMaintainPlanInfo : smartCraneMaintainPlanInfoList) {
            String craneNo = smartCraneMaintainPlanInfo.getCraneNo();
            if (StringUtil.isNotBlank(craneNo)) {
                CMCraneMaintainPlan cmCraneMaintainPlan = new CMCraneMaintainPlan(craneNo);
                cmCraneMaintainPlan.setMaintainStartTime(smartCraneMaintainPlanInfo.getMaintainStartTime());
                cmCraneMaintainPlan.setMaintainEndTime(smartCraneMaintainPlanInfo.getMaintainEndTime());
                cmCraneMaintainPlan.setCraneStatus(smartCraneMaintainPlanInfo.getCraneStatus());
                cmCraneMaintainPlan.setCraneMoveStatus(smartCraneMaintainPlanInfo.getCraneMoveStatus());
                machineData.addCMCraneMaintainPlan(cmCraneMaintainPlan);
            }
        }
    }

    private void parseCraneWorkPlan(List<SmartCraneWorkPlanInfo> smartCraneWorkPlanInfoList, MachineData machineData) {
        for (SmartCraneWorkPlanInfo smartCraneWorkPlanInfo : smartCraneWorkPlanInfoList) {
            Long berthId = smartCraneWorkPlanInfo.getBerthId();
            String craneNo = smartCraneWorkPlanInfo.getCraneNo();
            if (berthId != null && StringUtil.isNotBlank(craneNo)) {
                CMCraneWorkPlan cmCraneWorkPlan = new CMCraneWorkPlan(berthId, craneNo);
                cmCraneWorkPlan.setWorkPlanStartTime(smartCraneWorkPlanInfo.getWorkStartTime());
                cmCraneWorkPlan.setWorkPlanEndTime(smartCraneWorkPlanInfo.getWorkEndTime());
                machineData.addCMCraneWorkPlan(cmCraneWorkPlan);
            }
        }
    }

    private void parseAreaCnt(List<SmartAreaContainerInfo> smartAreaContainerInfoList, AllRuntimeData allRuntimeData) {
        for (SmartAreaContainerInfo smartAreaContainerInfo : smartAreaContainerInfoList) {
            Long berthId = smartAreaContainerInfo.getBerthId();
            String areaNo = smartAreaContainerInfo.getAreaNo();
            try {
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {

                }
                AreaContainer areaContainer = new AreaContainer();

            } catch (Exception e) {
                logger.logError("解析堆场箱区(areaNo:" + areaNo + ")统计信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseCwpWorkBlock(List<SmartCwpWorkBlockInfo> smartCwpWorkBlockInfoList, AllRuntimeData allRuntimeData) {
        for (SmartCwpWorkBlockInfo smartCwpWorkBlockInfo : smartCwpWorkBlockInfoList) {
            if ("Y".equals(smartCwpWorkBlockInfo.getLockFlag())) {
                logger.logError("锁定作业块桥机号不可以为空", !StringUtil.isNotBlank(smartCwpWorkBlockInfo.getCraneNo()));
                logger.logError("锁定作业块桥机序不可以为空", smartCwpWorkBlockInfo.getCraneSeq() == null);
                logger.logError("锁定作业块舱序不可以为空", smartCwpWorkBlockInfo.getHatchSeq() == null);
                logger.logError("锁定作业块倍位号不可以为空", !StringUtil.isNotBlank(smartCwpWorkBlockInfo.getBayNo()));
                logger.logError("锁定作业块计划作业量不可以为空", smartCwpWorkBlockInfo.getPlanAmount() == null);
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(smartCwpWorkBlockInfo.getBerthId());
                if (workingData != null) {
                    WorkBlock workBlock = new WorkBlock();
                    workBlock = (WorkBlock) BeanCopyUtil.copyBean(smartCwpWorkBlockInfo, workBlock);
                    workingData.addLockHatchWorkBlock(workBlock);
                    workingData.addLockCraneWorkBlock(workBlock);
                }
            }
        }
    }

}
