package com.cwp3.ioservice.method;

import com.cwp3.utils.BeanCopyUtil;
import com.shbtos.biz.smart.cwp.pojo.Results.SmartReCwpBlockInfo;

import java.util.*;

/**
 * Created by csw on 2017/3/14 21:41.
 * Explain:
 */
public class CraneOrderMethod {

    public List<SmartReCwpBlockInfo> getHatchSeq(List<SmartReCwpBlockInfo> smartReCwpBlockInfoList) {
        List<SmartReCwpBlockInfo> resultInfoList = new ArrayList<>();

        Map<Long, Map<Long, SmartReCwpBlockInfo>> hatchIdMap = new HashMap<>();

        for (SmartReCwpBlockInfo smartReCwpBlockInfo : smartReCwpBlockInfoList) {
            Long hatchId = smartReCwpBlockInfo.getHatchId();
            Long startSec = smartReCwpBlockInfo.getWorkingStartTime().getTime();

            if (hatchIdMap.get(hatchId) == null) {
                hatchIdMap.put(hatchId, new HashMap<Long, SmartReCwpBlockInfo>());
            }
            hatchIdMap.get(hatchId).put(startSec, smartReCwpBlockInfo);
        }

        for (Map<Long, SmartReCwpBlockInfo> valueMap : hatchIdMap.values()) {
            List<Long> timeList = new ArrayList(valueMap.keySet());
            Collections.sort(timeList);
            Long seq = 1L;
            SmartReCwpBlockInfo lastBlock = new SmartReCwpBlockInfo();
            for (Long tKey : timeList) {
                SmartReCwpBlockInfo currentBlock = valueMap.get(tKey);
                currentBlock.setHatchSeq(seq);
                if (lastBlock.getBerthId() == null) {
                    lastBlock = (SmartReCwpBlockInfo) BeanCopyUtil.copyBean(currentBlock, lastBlock);
                    seq++;
                } else {    //上一个时间片对象不为空
                    //判断当前对象的开始时间是否与上一个对象的结束时间相同（由于cwp的输出有可能前者会小于后者），
                    // 还有在同一个倍位上（桥机没有移动），
                    // 桥机装、卸的块分开
                    // 则桥机顺序号是相同的
                    long cur_last_time = currentBlock.getWorkingStartTime().getTime() / 1000 - lastBlock.getWorkingEndTime().getTime() / 1000;
                    if (cur_last_time == 1 && currentBlock.getCraneNo().equals(lastBlock.getCraneNo())
                            && currentBlock.getBayNo().equals(lastBlock.getBayNo())
                            && currentBlock.getLduldfg().equals(lastBlock.getLduldfg())) {
                        currentBlock.setHatchSeq(lastBlock.getHatchSeq());
                    } else {
                        lastBlock = (SmartReCwpBlockInfo) BeanCopyUtil.copyBean(currentBlock, lastBlock);
                        seq++;
                    }
                }
                resultInfoList.add(currentBlock);
            }
        }

        return resultInfoList;
    }

    public List<SmartReCwpBlockInfo> getCraneSeq(List<SmartReCwpBlockInfo> smartReCwpBlockInfoList) {
        List<SmartReCwpBlockInfo> resultInfoList = new ArrayList<>();

        Map<String, Map<Long, SmartReCwpBlockInfo>> craneNoMap = new HashMap<>();

        //将数据按桥机保存在Map里
        for (SmartReCwpBlockInfo smartReCwpBlockInfo : smartReCwpBlockInfoList) {
            String craneNo = smartReCwpBlockInfo.getCraneNo();
            Long startSec = smartReCwpBlockInfo.getWorkingStartTime().getTime();

            if (craneNoMap.get(craneNo) == null) {
                craneNoMap.put(craneNo, new HashMap<Long, SmartReCwpBlockInfo>());
            }
            craneNoMap.get(craneNo).put(startSec, smartReCwpBlockInfo);
        }

        for (Map<Long, SmartReCwpBlockInfo> valueMap : craneNoMap.values()) {
            List<Long> timeList = new ArrayList(valueMap.keySet());
            Collections.sort(timeList);
            Long seq = 1L;
            SmartReCwpBlockInfo lastBlock = new SmartReCwpBlockInfo();
            for (Long tKey : timeList) {
                SmartReCwpBlockInfo currentBlock = valueMap.get(tKey);
                //添加桥机作业顺序
                currentBlock.setCraneSeq(seq);
                if (lastBlock.getBerthId() == null) {
                    lastBlock = (SmartReCwpBlockInfo) BeanCopyUtil.copyBean(currentBlock, lastBlock);
                    seq++;
                } else {    //上一个时间片对象不为空
                    //判断当前对象的开始时间是否与上一个对象的结束时间相同（由于cwp的输出有可能前者会小于后者），
                    // 还有在同一个倍位上（桥机没有移动），
                    // 桥机装、卸的块分开
                    // 则桥机顺序号是相同的
                    long cur_last_time = currentBlock.getWorkingStartTime().getTime() / 1000 - lastBlock.getWorkingEndTime().getTime() / 1000;
                    if (cur_last_time == 1 && currentBlock.getHatchId().equals(lastBlock.getHatchId())
                            && currentBlock.getLduldfg().equals(lastBlock.getLduldfg())) {
                        currentBlock.setCraneSeq(lastBlock.getCraneSeq());
                    } else {
                        lastBlock = (SmartReCwpBlockInfo) BeanCopyUtil.copyBean(currentBlock, lastBlock);
                        seq++;
                    }
                }
                resultInfoList.add(currentBlock);
            }
        }

        return resultInfoList;
    }
}
