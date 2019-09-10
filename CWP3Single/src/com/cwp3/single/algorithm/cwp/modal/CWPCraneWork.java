package com.cwp3.single.algorithm.cwp.modal;

import java.io.*;
import java.util.Date;

/**
 * Created by csw on 2018/11/5.
 * Description:
 */
public class CWPCraneWork implements Serializable {

    private String craneNo;
    private String addOrDeleteFlag;
    private Date addOrDeleteTime;

    public CWPCraneWork(String craneNo, String addOrDeleteFlag, Date addOrDeleteTime) {
        this.craneNo = craneNo;
        this.addOrDeleteFlag = addOrDeleteFlag;
        this.addOrDeleteTime = addOrDeleteTime;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public String getAddOrDeleteFlag() {
        return addOrDeleteFlag;
    }

    public Date getAddOrDeleteTime() {
        return addOrDeleteTime;
    }

    public CWPCraneWork deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            return (CWPCraneWork) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
