package com.example.shequbantranslate.main

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.example.shequbantranslate.bean.TranslationBean
import com.example.shequbantranslate.net.TranslateCallBack
import com.example.shequbantranslate.net.requestNetData
import javazoom.jl.player.Player
import java.awt.Color
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.sound.sampled.AudioSystem


/**
 * Created by Pinger on 2016/12/10.
 * 翻译插件动作
 *
 *
 * 需要完成的逻辑有三段：
 * 第一：获取选中的单词
 * 第二：联网查询选中的单词意思，返回json，然后解析
 * 第三：弹出PopupWindow，显示结果

 * @author ice1000
 * *
 * @author Pinger
 */

class TranslateAction : AnAction() {
    companion object {
        private val icon = IconLoader.getIcon("/icons/a8.png")
        private val ttsUrl = "https://sp1.baidu.com/-rM1hT4a2gU2pMbgoY3K/gettts?lan=en&text="
    }

    private lateinit var editor: Editor
    private var latestClickTime = 0L  // 上一次的点击时间

    override fun actionPerformed(e: AnActionEvent) {
        if (!isFastClick(1000)) {
            /* 第一步 --> 选中单词 */
            // 获取动作编辑器
            editor = e.getData(PlatformDataKeys.EDITOR) ?: return

            // 获取选择模式对象
            val model = editor.selectionModel

            // 选中文字
            val selectedText = model.selectedText ?: return
            if (selectedText.isBlank()) return

            /* 第二步 ---> API查询 */
            requestNetData(selectedText, object : TranslateCallBack<TranslationBean>() {
                override fun onSuccess(result: TranslationBean) = showPopupWindow(selectedText,result.toString())
                override fun onFailure(message: String) = showPopupWindow(selectedText,message)
                override fun onError(message: String) = showPopupWindow(selectedText,message)
            })
        }
    }

    /**
     * 第三步 --> 弹出对话框

     * @param result string result
     */
    private fun showPopupWindow(originWord:String,result: String) {
        println(result)
        ApplicationManager.getApplication().invokeLater {
            JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(result, icon, JBColor(Color(186, 238, 186), Color(73, 117, 73)), null)
                .setFadeoutTime(15000)
                .setHideOnAction(true)
                .setClickHandler({
//                    println("测试发觉是离开管局啦是噶奥克斯管局卡拉是管局")
					downMp3(originWord)
                }, false)
                .createBalloon()
                .show(JBPopupFactory.getInstance().guessBestPopupLocation(editor), Balloon.Position.below)


        }

//		val voice = JButton("上一个")
//		val panel = JPanel()
//		panel.setSize(40,20)
//		panel.add(JLabel(result), BorderLayout.CENTER)
//		panel.add(voice, BorderLayout.NORTH)
//		voice.addActionListener {
//			println("测试发觉是离开管局啦是噶奥克斯管局卡拉是管局")
//		}
////			panel.add(JButton("下一个"), BorderLayout.SOUTH)
//
//		val jbPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null).setMinSize(Dimension(150,300)).createPopup()
//		jbPopup.showInFocusCenter()


//		val instance = JBPopupFactory.getInstance() //创建JBPopupFactory实例
//
//		val button = JButton("发音") //新建Button放到自定义弹框中
//
//		val panel = JPanel()
//		panel.setSize(20, 20) //限制Buton大小，否则会填充整个弹框
//
//		panel.add(button)
//		button.addActionListener { println("Hello Idea Plugin") } // 添加监听事件
//
//		instance.createComponentPopupBuilder(panel, JBLabel()) //参数说明：内容对象,优先获取
//			.setTitle("->")
//			.setAdText(result)
//			.setMovable(true)
//			.setResizable(true)
//			.setNormalWindowLevel(false)
//			.setMinSize(Dimension(150, 170))
//			.createPopup()
//			.showInFocusCenter()
    }

    /**
     * 屏蔽多次选中
     */
    private fun isFastClick(timeMillis: Long): Boolean {
        val begin = System.currentTimeMillis()
        val end = begin - latestClickTime
        if (end in 1..(timeMillis - 1)) return true
        latestClickTime = begin
        return false
    }

	private val basePath = "/home/liujie/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea/ideaIC/2021.2/b0727ceddea2b62b16825db9308e14a470198e7f/ideaIC-2021.2/bin/home/liujie/translatePlugin/tts"
    private fun playSound(md5:String) {
        try {
        	println(File("$basePath/$md5.mp3").absoluteFile.absoluteFile)
            val audioInputStream = AudioSystem.getAudioInputStream(File("$basePath/$md5.mp3").absoluteFile)
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()
        } catch (ex: Exception) {
            println("Error with playing sound.")
            ex.printStackTrace()
        }
    }

	private fun downMp3(originWord:String){
		// 下载网络文件
		val md5 = toMd5(originWord)
//		var bytesum = 0
//		var byteread = 0
//		val url = URL(ttsUrl+originWord)
//		try {
//			val conn: URLConnection = url.openConnection()
//			val inStream: InputStream = conn.getInputStream()
//			val fs = FileOutputStream("home/liujie/translatePlugin/tts/$md5.mp3")
//			if (File("home/liujie/translatePlugin/tts/$md5.mp3").exists()){
//				if (md5 != null) {
//					playSound(md5)
//				}
//				return
//			}
//			val buffer = ByteArray(1204)
//			var length: Int
//			while (inStream.read(buffer).also { byteread = it } != -1) {
//				bytesum += byteread
//				fs.write(buffer, 0, byteread)
//			}
//		} catch (e: FileNotFoundException) {
//			e.printStackTrace()
//		} catch (e: IOException) {
//			e.printStackTrace()
//		}
		println(md5)
		val file = File("$basePath/$md5.mp3")
		println(file.absoluteFile)
		if (!File(basePath).exists()){
			File(basePath).mkdirs()
		}
		if (!file.exists()){
			file.createNewFile()
		}else{
			if (md5 != null) {
				playMp3(file.absolutePath)
//				playSound(md5)
			}
			return
		}
		var url: URL? = null
		try {
			url = URL(ttsUrl+originWord)

			// 打开连接
			val con = url.openConnection()
			// 输入流
			val isTr = con.getInputStream()
			// 1K的数据缓冲
			val bs = ByteArray(1024)
			// 读取到的数据长度
			var len: Int
			//图片的完整路径

			//是否存在目录
			val os = FileOutputStream(file, true)
			// 开始读取
			while (isTr.read(bs).also { len = it } != -1) {
				os.write(bs, 0, len)
			}
			// 完毕，关闭所有链接
			os.close()
			isTr.close()
			playMp3(file.absolutePath)
		} catch (e: java.lang.Exception) {
			e.printStackTrace()
		}
	}

	@Throws(NoSuchAlgorithmException::class)
	fun getMD5String(s: String): String? {
		// 创建md5实例
		val messagedigest = MessageDigest.getInstance("MD5")
		// 更新摘要
		messagedigest.update(s.toByteArray())
		// 计算哈希并返回
		val digest = messagedigest.digest()
		// 将摘要转换成16进制字符串
		val bigInteger = BigInteger(1, digest)
		return bigInteger.toString(16)
	}

	private val slat = "&%5123***&&%%$$#@"
	fun toMd5(dataStr: String): String? {
		var dataStr = dataStr
		try {
			dataStr += slat
			val m = MessageDigest.getInstance("MD5")
			m.update(dataStr.toByteArray(charset("UTF8")))
			val s = m.digest()
			var result = ""
			for (i in s.indices) {
				result += Integer.toHexString(0x000000FF and s[i].toInt() or -0x100).substring(6)
			}
			return result
		} catch (e: java.lang.Exception) {
			e.printStackTrace()
		}
		return ""
	}

	fun playMp3(filePath: String?) {

//		val hit = Media(File(filePath).toURI().toString())
//
//		val mediaPlayer = MediaPlayer(hit)
//
//		mediaPlayer.play()


		try {
			val fis = FileInputStream(filePath)
			val bis = BufferedInputStream(fis)
			val player = Player(bis)
			// System.out.println("Tocando Musica!");   // DEBUG NO CONSOLE
			player.play()
			// System.out.println("Terminado Musica!"); // DEBUG NO CONSOLE
			if (player.isComplete) {
				player.close()
			}
		} catch (e: java.lang.Exception) {
			// System.out.println("Problema ao tocar Musica" + mp3); // DEBUG NO CONSOLE
			e.printStackTrace()
		} finally {
		}



//		try {
//			// 文件流
//			var audioInputStream = AudioSystem.getAudioInputStream(File(filePath))
//			// 文件编码
//			var audioFormat = audioInputStream.format
//			// 转换文件编码
//			if (audioFormat.encoding !== AudioFormat.Encoding.PCM_SIGNED) {
//				println(audioFormat.encoding)
//				audioFormat = AudioFormat(
//					AudioFormat.Encoding.PCM_SIGNED,
//					audioFormat.sampleRate,
//					16,
//					audioFormat.channels,
//					audioFormat.channels * 2,
//					audioFormat.sampleRate,
//					false
//				)
//				// 将数据流也转换成指定编码
//				audioInputStream = AudioSystem.getAudioInputStream(audioFormat, audioInputStream)
//			}
//
//
//			// 打开输出设备
//			val dataLineInfo = DataLine.Info(SourceDataLine::class.java, audioFormat, AudioSystem.NOT_SPECIFIED)
//			// 使数据行得到一个播放设备
//			val sourceDataLine = AudioSystem.getLine(dataLineInfo) as SourceDataLine
//			// 将数据行用指定的编码打开
//			sourceDataLine.open(audioFormat)
//			// 使数据行得到数据时就开始播放
//			sourceDataLine.start()
//			val bytesPerFrame = audioInputStream.format.frameSize
//			// 将流数据逐渐写入数据行,边写边播
//			val numBytes = 1024 * bytesPerFrame
//			val audioBytes = ByteArray(numBytes)
//			while (audioInputStream.read(audioBytes) != -1) {
//				sourceDataLine.write(audioBytes, 0, audioBytes.size)
//			}
//			sourceDataLine.drain()
//			sourceDataLine.stop()
//			sourceDataLine.close()
//		} catch (e: java.lang.Exception) {
//			e.printStackTrace()
//		}


//		val file = File("F:\\KuGou\\张婉清、童英然、薛之谦、黄云龙 - 丑八怪.mp3")
//		val player: Player = Manager.createPlayer(file.toURL())
//		player.start() //开始播放

	}

}

