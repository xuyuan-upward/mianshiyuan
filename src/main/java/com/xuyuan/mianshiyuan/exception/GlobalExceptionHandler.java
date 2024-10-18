package com.xuyuan.mianshiyuan.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.xuyuan.mianshiyuan.common.BaseResponse;
import com.xuyuan.mianshiyuan.common.ErrorCode;
import com.xuyuan.mianshiyuan.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author <a href="https://github.com/xuyuan-upward">许苑向上</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(NotRoleException.class)
    public BaseResponse<?> notRoleExceptionHandler(RuntimeException e) {
        log.error("NotRoleException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无权限");
    }

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler(RuntimeException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "未登录");
    }

}
