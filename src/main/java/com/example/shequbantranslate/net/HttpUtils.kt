@file:JvmName("HttpUtils")

package com.example.shequbantranslate.net

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.shequbantranslate.bean.EMPTY
import com.example.shequbantranslate.bean.TranslationBean
import com.example.shequbantranslate.main.LocalData
import org.intellij.lang.annotations.Language
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Created by Pinger on 2016/12/10.
 * 网络工具类，可以直接访问，不需要开子线程

 * @author ice1000
 * *
 * @author Pinger
 */

private const val BASE_URL = "http://fanyi.youdao.com/openapi.do?keyfrom=Skykai521&key=977124034&type=data&doctype=json&version=1.1&q="

/**
 * 请求网络数据
 * @author ice1000
 */
fun requestNetData(queryWord: String, callBack: TranslateCallBack<TranslationBean>) {
	try {
		LocalData.read(queryWord)?.let {
			try {
				callBack.onSuccess(Gson().fromJson<TranslationBean>(it, callBack.type))
			} catch (e: JsonSyntaxException) {
				callBack.onFailure("返回的 Json 解析不了，你看看是不是报了什么奇怪的错，去 GitHub 跟我说下：\n$it")
			}
			return
		}

		@Language("RegExp")
		val url = URL("$BASE_URL${URLEncoder.encode(queryWord.replace(Regex("[*+\\- \r]+"), " "), "UTF-8")}")
		val conn = url.openConnection() as HttpURLConnection

		conn.connectTimeout = 3000
		conn.readTimeout = 3000
		conn.requestMethod = conn.requestMethod

		// 连接成功
		if (conn.responseCode == 200) {
			val ins = conn.inputStream

			// 获取到Json字符串
			val content = StreamUtils.getStringFromStream(ins)
			if (content.isNotBlank()) {
				callBack.onSuccess(Gson().fromJson<TranslationBean>(content, callBack.type))
				LocalData.store(queryWord, content)
			} else callBack.onFailure(EMPTY)
		} else callBack.onFailure("错误码：${conn.responseCode}\n错误信息：\n${conn.responseMessage}")
	} catch (e: IOException) {
		callBack.onFailure("无法访问：\n${e.message}")
	}
}
