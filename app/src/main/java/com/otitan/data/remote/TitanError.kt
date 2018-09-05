package com.otitan.data.remote

/**
 * Created by hanyw on 2018/8/10
 * 约定异常
 */

object TitanError {
    /**
     * 未知错误
     */
    val UNKNOWN = 1000
    /**
     * 解析错误
     */
    val PARSE_ERROR = 1001
    /**
     * 网络错误
     */
    val NETWORD_ERROR = 1002
    /**
     * 协议出错
     */
    val HTTP_ERROR = 1003
}
