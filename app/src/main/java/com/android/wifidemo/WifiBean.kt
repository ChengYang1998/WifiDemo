package com.android.wifidemo

class WifiBean : Comparable<WifiBean> {
    var wifiName: String? = null
    var level: String? = null
    var state: String? = null //已连接  正在连接  未连接 三种状态
    var capabilities: String? = null //加密方式
    override fun toString(): String {
        return "WifiBean{" +
                "wifiName='" + wifiName + '\'' +
                ", level='" + level + '\'' +
                ", state='" + state + '\'' +
                ", capabilities='" + capabilities + '\'' +
                '}'
    }

    override fun compareTo(o: WifiBean): Int {
        val level1 = level!!.toInt()
        val level2 = o.level!!.toInt()
        return level1 - level2
    }
}