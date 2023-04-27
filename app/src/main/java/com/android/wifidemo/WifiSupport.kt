package com.android.wifidemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.TextUtils


object WifiSupport {
    private val TAG = "WifiSupport"

    enum class WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    fun WifiSupport() {}

    @SuppressLint("MissingPermission")
    fun getWifiScanResult(context: Context?): List<ScanResult?>? {
        val b = context == null
        return (context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager).scanResults
    }

    fun isWifiEnable(context: Context): Boolean {
        return (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled
    }


    @SuppressLint("MissingPermission")
    fun getConfigurations(context: Context): List<*>? {
        return (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).configuredNetworks
    }

    /**
     * 连接wifi
     *
     * @param ssid     名称
     * @param password 密码
     * @return 成功与否
     */
    fun connect(ssid: String, password: String, context: Context): Boolean {
        var result = false
        var wifiConfig: WifiConfiguration? = null
        wifiConfig = isExits(ssid, context)
        if (wifiConfig == null) {
            wifiConfig = createWifiConfig(ssid, password, WifiCipherType.WIFICIPHER_WPA)
        }
        result = addNetWork(wifiConfig!!, ssid, context)
        if (result) {
            val connection_manager =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
            request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            connection_manager.registerNetworkCallback(request.build(), object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    ConnectivityManager.setProcessDefaultNetwork(network)
                }
            })
        }
        return result
    }


    //查看以前是否也配置过这个网络
    fun isExits(SSID: String, context: Context): WifiConfiguration? {
        val wifimanager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @SuppressLint("MissingPermission") val existingConfigs = wifimanager.configuredNetworks
        for (existingConfig in existingConfigs) {
            if (existingConfig.SSID == "\"" + SSID + "\"") {
                return existingConfig
            }
        }
        return null
    }


    //创建wifi配置
    fun createWifiConfig(SSID: String, password: String, type: WifiCipherType): WifiConfiguration? {
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + SSID + "\""
        if (type == WifiCipherType.WIFICIPHER_NOPASS) {
//            config.wepKeys[0] = "";  //注意这里
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            //            config.wepTxKeyIndex = 0;
        }
        if (type == WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\""
            config.hiddenSSID = true
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            config.wepTxKeyIndex = 0
        }
        if (type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\""
            config.hiddenSSID = true
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED
        }
        return config
    }

    /**
     * 接入某个wifi热点
     */
    fun addNetWork(config: WifiConfiguration, ssid: String, context: Context): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo.ssid.replace("\"", "") == ssid) {
            return true
        }
        wifiManager.disableNetwork(wifiInfo.networkId)
        var result = false
        if (config.networkId > 0) {
            result = wifiManager.enableNetwork(config.networkId, true)
            wifiManager.updateNetwork(config)
        } else {
            val i = wifiManager.addNetwork(config)
            if (i > 0) {
                wifiManager.saveConfiguration()
                return wifiManager.enableNetwork(i, true)
            }
        }
        return result
    }


    // 断开当前网络
    fun disconnectWifi(context: Context) {
        val wifimanager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifimanager.isWifiEnabled) {
            wifimanager.disconnect()
        }
    }


    /**
     * 判断wifi热点支持的加密方式
     */
    fun getWifiCipher(s: String): WifiCipherType? {
        return if (s.isEmpty()) {
            WifiCipherType.WIFICIPHER_INVALID
        } else if (s.contains("WEP")) {
            WifiCipherType.WIFICIPHER_WEP
        } else if (s.contains("WPA") || s.contains("WPA2") || s.contains("WPS")) {
            WifiCipherType.WIFICIPHER_WPA
        } else {
            WifiCipherType.WIFICIPHER_NOPASS
        }
    }

    //查看以前是否也配置过这个网络
    fun isExsits(SSID: String, context: Context): WifiConfiguration? {
        val wifimanager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @SuppressLint("MissingPermission") val existingConfigs = wifimanager.configuredNetworks
        for (existingConfig in existingConfigs) {
            if (existingConfig.SSID == "\"" + SSID + "\"") {
                return existingConfig
            }
        }
        return null
    }

    // 打开WIFI
    fun openWifi(context: Context) {
        val wifimanager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifimanager.isWifiEnabled) {
            wifimanager.isWifiEnabled = true
        }
    }

    // 关闭WIFI
    fun closeWifi(context: Context) {
        val wifimanager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifimanager.isWifiEnabled) {
            wifimanager.isWifiEnabled = false
        }
    }

    fun isOpenWifi(context: Context): Boolean {
        val wifimanager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifimanager.isWifiEnabled
    }

    /**
     * 将idAddress转化成string类型的Id字符串
     *
     * @param idString
     * @return
     */
    fun getStringId(idString: Int): String? {
        val sb = StringBuffer()
        var b = idString shr 0 and 0xff
        sb.append("$b.")
        b = idString shr 8 and 0xff
        sb.append("$b.")
        b = idString shr 16 and 0xff
        sb.append("$b.")
        b = idString shr 24 and 0xff
        sb.append(b)
        return sb.toString()
    }

    /**
     * 设置安全性
     *
     * @param capabilities
     * @return
     */
    fun getCapabilitiesString(capabilities: String): String? {
        return if (capabilities.contains("WEP")) {
            "WEP"
        } else if (capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains(
                "WPS"
            )
        ) {
            "WPA/WPA2"
        } else {
            "OPEN"
        }
    }

    fun getIsWifiEnabled(context: Context): Boolean {
        val wifimanager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifimanager.isWifiEnabled
    }

    fun getReplace(context: Context, list: MutableList<WifiBean>) {
        val wifi: WifiInfo = WifiSupport.getConnectedWifiInfo(context)
        val listCopy: MutableList<WifiBean> = ArrayList()
        listCopy.addAll(list)
        for (i in list.indices) {
            if ("\"" + list[i].wifiName + "\"" == wifi.ssid) {
                listCopy.add(0, list[i])
                listCopy.removeAt(i + 1)
                listCopy[0].state = "已连接"
            }
        }
        list.clear()
        list.addAll(listCopy)
    }


    /**
     * 去除同名WIFI
     *
     * @param oldSr 需要去除同名的列表
     * @return 返回不包含同命的列表
     */
    fun noSameName(oldSr: List<ScanResult>): List<ScanResult> {
        val newSr: MutableList<ScanResult> = ArrayList<ScanResult>()
        for (result in oldSr) {
            if (!TextUtils.isEmpty(result.SSID) && !containName(newSr, result.SSID)) newSr.add(
                result
            )
        }
        return newSr
    }

    /**
     * 判断一个扫描结果中，是否包含了某个名称的WIFI
     *
     * @param sr   扫描结果
     * @param name 要查询的名称
     * @return 返回true表示包含了该名称的WIFI，返回false表示不包含
     */
    fun containName(sr: List<ScanResult>, name: String?): Boolean {
        for (result in sr) {
            if (!TextUtils.isEmpty(result.SSID) && result.SSID.equals(name)) return true
        }
        return false
    }

    /**
     * 返回level 等级
     */
    fun getLevel(level: Int): Int {
        return if (Math.abs(level) < 50) {
            1
        } else if (Math.abs(level) < 75) {
            2
        } else if (Math.abs(level) < 90) {
            3
        } else {
            4
        }
    }


    fun getConnectedWifiInfo(context: Context): WifiInfo {
        return (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo
    }

}