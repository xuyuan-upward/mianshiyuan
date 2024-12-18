package com.xuyuan.mianshiyuan.exception;

import com.xuyuan.mianshiyuan.common.ErrorCode;

/**
 * 抛异常工具类
 *
 * @author <a href="https://github.com/xuyuan-upward">许苑向上</a>
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param runtimeException
     */
  /*  public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }*/

    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }
    public static void throwIf(boolean condition, ErrorCode errorCode,String message) {
        throwIf(condition, new BusinessException(errorCode,message));
    }
    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     */
  /*  public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }*/

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     * @param message
     */
  /*  public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }*/
}
