package com.cwp3.model.work;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMHatchCover;

import java.util.*;

public class HatchBlock {

    private Long hatchId;

    private Map<Integer, List<Integer>> aboveBlockMap; //甲板上分档定义，主键为舱盖板陆侧到海侧顺序，<hcSeq, [rowNoFrom...rowNoTo]>>
    private Map<Integer, List<Integer>> belowBlockMap; //甲板下分档定义
    private Map<Integer, VMHatchCover> hatchCoverMap; //舱盖板对象，主键为舱盖板陆侧到海侧顺序,<hcSeq,VMHatchCover>

    public HatchBlock(Long hatchId) {
        this.hatchId = hatchId;
        this.aboveBlockMap = new LinkedHashMap<>();
        this.belowBlockMap = new LinkedHashMap<>();
        this.hatchCoverMap = new LinkedHashMap<>();
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void addAboveBlock(Integer seq, List<Integer> rows) {
        this.aboveBlockMap.put(seq, rows);
    }

    public List<Integer> getAboveBlockRowListByHcSeq(Integer hcSeq) {
        return this.aboveBlockMap.get(hcSeq);
    }

    public void addBelowBlock(Integer seq, List<Integer> rows) {
        this.belowBlockMap.put(seq, rows);
    }

    public List<Integer> getBelowBlockRowListByHcSeq(Integer hcSeq) {
        return this.belowBlockMap.get(hcSeq);
    }

    public void addVMHatchCover(Integer seq, VMHatchCover hatchCover) {
        this.hatchCoverMap.put(seq, hatchCover);
    }

    public Map<Integer, List<Integer>> getAboveBlockMap() {
        return aboveBlockMap;
    }

    public Map<Integer, List<Integer>> getBelowBlockMap() {
        return belowBlockMap;
    }

    public VMHatchCover getVMHatchCoverBySeq(Integer seq) {
        return this.hatchCoverMap.get(seq);
    }

    public Integer getHatchCoverNumber(){
        return hatchCoverMap.size();
    }

    public Integer getHcSeqByRowNo(Integer rowNo, String board) {
        if (CWPDomain.BOARD_ABOVE.equals(board)) {
            for (Map.Entry<Integer, List<Integer>> entry : aboveBlockMap.entrySet()) {
                for (Integer rowNo1 : entry.getValue()) { //舱盖板不会叠压
                    if (rowNo.equals(rowNo1)) {
                        return entry.getKey();
                    }
                }
            }
        } else {
            for (Map.Entry<Integer, List<Integer>> entry : belowBlockMap.entrySet()) {
                for (Integer rowNo1 : entry.getValue()) { //舱盖板不会叠压
                    if (rowNo.equals(rowNo1)) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    public Integer getHcSeqByOtherRowNo(Integer rowNo1, String board) {
        if (CWPDomain.BOARD_ABOVE.equals(board)) {
            return getHcSeq(rowNo1, aboveBlockMap);
        } else {
            return getHcSeq(rowNo1, belowBlockMap);
        }
    }

    private Integer getHcSeq(Integer rowNo1, Map<Integer, List<Integer>> blockMap) {
        List<Integer> hcSeqList = new ArrayList<>(blockMap.keySet());
        if (hcSeqList.size() == 1) {
            return hcSeqList.get(0);
        } else {
            int rowNo = rowNo1;
            while (!blockMap.get(hcSeqList.get(0)).contains(rowNo)) {
                rowNo -= 2;
                if (blockMap.get(hcSeqList.get(0)).contains(rowNo)) {
                    return hcSeqList.get(0);
                }
                if (rowNo < 0) {
                    break;
                }
            }
            rowNo = rowNo1;
            while (!blockMap.get(hcSeqList.get(hcSeqList.size() - 1)).contains(rowNo)) {
                rowNo -= 2;
                if (blockMap.get(hcSeqList.get(hcSeqList.size() - 1)).contains(rowNo)) {
                    return hcSeqList.get(hcSeqList.size() - 1);
                }
                if (rowNo < 0) {
                    break;
                }
            }
        }
        return null;
    }
}
