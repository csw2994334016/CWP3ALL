package com.cwp3.single.algorithm.cwp.modal;

import java.io.Serializable;

/**
 * Created by csw on 2017/2/28 16:22.
 * Explain: 键值对：DP选择的结果<craneId, bayNo>，桥机选择哪个倍位作业
 */
public class DPPair<A, B> implements Serializable {

    private A first;

    private B second;

    public DPPair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

}
