package com.cwp3.model.vessel;

import com.cwp3.domain.CWPDomain;
import com.cwp3.utils.CalculateUtil;

import java.util.*;

/**
 * Created by csw on 2017/4/20 11:38.
 * Explain: 舱信息
 */
public class VMHatch {

    private Long hatchId;
    private Integer hatchSeq;
    private Double hatchPosition;
    private Double hatchLength;
    private Set<Integer> bayNos;

    public VMHatch(Long hatchId) {
        this.hatchId = hatchId;
        this.bayNos = new HashSet<>();
    }

    public Long getHatchId() {
        return hatchId;
    }

    public Integer getHatchSeq() {
        return hatchSeq;
    }

    public void setHatchSeq(Integer hatchSeq) {
        this.hatchSeq = hatchSeq;
    }

    public Double getHatchPosition() {
        return hatchPosition;
    }

    public void setHatchPosition(Double hatchPosition) {
        this.hatchPosition = hatchPosition;
    }

    public Double getHatchLength() {
        return hatchLength;
    }

    public void setHatchLength(Double hatchLength) {
        this.hatchLength = hatchLength;
    }

    public void addByNo(Integer bayNo) {
        bayNos.add(bayNo);
    }

    public Integer getBayNo1() {
        List<Integer> bayNoList = getBayNos();
        Collections.sort(bayNoList);
        return bayNoList.get(0);
    }

    public Integer getBayNo2() {
        List<Integer> bayNoList = getBayNos();
        Collections.sort(bayNoList);
        if (bayNoList.size() == 2) {
            return bayNoList.get(1);
        }
        return bayNoList.get(0);
    }

    public Integer getPairBayNo(Integer bayNo) {
        if (bayNo.equals(getBayNo1())) {
            return getBayNo2();
        }
        if (bayNo.equals(getBayNo2())) {
            return getBayNo1();
        }
        return bayNo;
    }

    public List<Integer> getBayNos() {
        return new ArrayList<>(bayNos);
    }

    public List<Integer> getAllBayNos() {
        Set<Integer> bayNoSet = new HashSet<>(getBayNos());
        bayNoSet.add((getBayNo1() + getBayNo2()) / 2);
        List<Integer> bayNoList = new ArrayList<>(bayNoSet);
        Collections.sort(bayNoList);
        return bayNoList;
    }

    public Integer getBayNoD() {
        return (getBayNo1() + getBayNo2()) / 2;
    }

    public Double getVMBayPosition(Integer bayNo) {
        Double hatchPo = null;
        List<Integer> bayNoList = getAllBayNos();
        if (bayNoList.size() == 1) { //只有一个倍位
            if (bayNo.equals(bayNoList.get(0))) {
                hatchPo = CalculateUtil.add(hatchPosition, hatchLength / 2);
            }
        } else if (bayNoList.size() == 3) { //否则有三个作业倍位
            if (bayNo.equals(bayNoList.get(0))) {
                hatchPo = CalculateUtil.add(hatchPosition, hatchLength / 4);
            } else if (bayNo.equals(bayNoList.get(1))) {
                hatchPo = CalculateUtil.add(hatchPosition, hatchLength / 2);
            } else if (bayNo.equals(bayNoList.get(2))) {
                hatchPo = CalculateUtil.add(hatchPosition, hatchLength * 3 / 4);
            }
        }
        return hatchPo;
    }
}
