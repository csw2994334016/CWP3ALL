package com.cwp3.single.algorithm.cwp.modal;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DPPair<?, ?> dpPair = (DPPair<?, ?>) o;
        return Objects.equals(first, dpPair.first) &&
                Objects.equals(second, dpPair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
