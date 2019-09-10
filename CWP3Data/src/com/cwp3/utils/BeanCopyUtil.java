package com.cwp3.utils;

import java.lang.reflect.Field;

/**
 * Created by csw on 2017/3/1 11:20.
 * Explain:
 */
public class BeanCopyUtil {

    public static Object copyBean(Object source, Object target) {
        if (source == null || target == null) {
            return target;
        }
        Field sourceField[] = source.getClass().getDeclaredFields();
        Field targetField[] = target.getClass().getDeclaredFields();
        if (sourceField == null || sourceField.length == 0) {
            return target;
        }
        if (targetField == null || targetField.length == 0) {
            return target;
        }
        for (Field tf : targetField) {
            tf.setAccessible(true);
            for (Field sf : sourceField) {
                sf.setAccessible(true);
                String tfType = tf.getType().getName();
                String sfType = sf.getType().getName();
                if (tf.getName().equals(sf.getName()) && tfType.equals(sfType)) {
                    try {
                        tf.set(target, sf.get(source));
                    } catch (IllegalAccessException e) {
//                        System.out.println("复制对象时，目标对象字段类型" + tfType + "与源对象字段类型" + sfType + "不匹配!");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return target;
    }
}
