package com.example.tesla

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar.OnSeekBarChangeListener
import kotlinx.coroutines.*

import kotlinx.coroutines.channels.Channel

// 2021.09.30 add
import android.content.SharedPreferences
import android.view.View
import android.widget.*

const val MSG_CONNECTION_SUCCESS = 111 // 接続成功
const val MSG_CONNECTION_FAILED = 222  // 接続失敗
const val MSG_IOEXCEPTION = 333        // 例外発生
var SlotD:Int = 0
var SterD:Int = 0
var sendD:String =""
class MainActivity : AppCompatActivity() {

    private var tcpcom: ComTcpClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sterBar = findViewById<SeekBar>(R.id.steer)
        val sterview = findViewById<TextView>(R.id.steer_text)
        val slotBar = findViewById<SeekBar>(R.id.slot)
        val slotview = findViewById<TextView>(R.id.accel_text)
        val connbtn = findViewById<Button>(R.id.connect)
        val buttonConnection: Button = findViewById(R.id.connect)

//        2021.09.30 added by shio
//        referenced: https://akira-watson.com/android/kotlin/sharedpreferences-datastorage.html
/*
       todo: 保存先のファイルを見るには以下の手順
        getSharedPreferences() の第1引数は、パラメータを保存しておくxmlファイル名 (拡張子なし)
        1. 表示 -> ツール・ウィンドウ -> デバイス・ファイル・エクスプローラーを開く
        2. Device File Explorer　の　/data/data/com.example.tesla/shared_prefs/ファイル名.xml に保存されている。
        *** スクリーンショットをベトナム・ラップトップのデスクトップに保存 device_file_explorer.png ***
*/
        val buttonWrite = findViewById<Button>(R.id.save)
        val buttonRead = findViewById<Button>(R.id.display)
        val editText = findViewById<EditText>(R.id.max_value)
        val textWrite = findViewById<TextView>(R.id.display_text)

        // パラメータの保存先にアクセスする
        val dataStore: SharedPreferences = getSharedPreferences("DataStore", Context.MODE_PRIVATE)

        // 初期状態を表示する
        val str1 = dataStore.getInt("sterMax", 0)
        val str2 = dataStore.getInt("sterNeutral", 0)
        val str3 = dataStore.getInt("sterMin", 0)
        val str4 = dataStore.getInt("throMax", 0)
        val str5 = dataStore.getInt("throNeutral", 0)
        val str6 = dataStore.getInt("throMin", 0)
        val str7 = dataStore.getInt("brakMax", 0)
        val str8 = dataStore.getInt("brakMin", 0)
        sterBar.max = str1.toString().toInt()
//        sterBar.progress = str2.toString().toInt()
        sterBar.min = str3.toString().toInt()
        slotBar.max = str4.toString().toInt()
//        slotBar.progress = str5.toString().toInt()
        slotBar.min = str6.toString().toInt()
//        brakMax.setText(str7.toString())
//        brakMin.setText(str8.toString())


        buttonWrite.setOnClickListener{
            val maxValue = editText.text.toString().toInt()
            val editor = dataStore.edit()
            editor.putInt("Input", maxValue)
//            editor.commit()
            editor.apply()
            slotBar.max = maxValue
        }

        buttonRead.setOnClickListener{
//            保存データが文字列のとき
//            val str = dataStore.getString("Input", "noData")
//            textWrite.text = str

//            数値のとき
            val str = dataStore.getInt("Input", 0)
            textWrite.text = str.toString()
        }


//        画面の遷移処理を追加
//        referenced: https://qiita.com/KNFK/items/4917e0af0f4321be5ab0
        val btnIntent = findViewById<Button>(R.id.btn_intent)
        btnIntent.setOnClickListener { // Intentクラスのオブジェクトを生成
            val intent = Intent(this@MainActivity, SubActivity::class.java)
            // 生成したオブジェクトを引数に画面を起動
            startActivity(intent)
        }

//      以下、2021.09.30以前のコード


        val channel = Channel<Int>()

        buttonConnection.setOnClickListener {
            val ip = "192.168.1.170"
            val port = "55555"
            if (!ip.isEmpty() && !port.isEmpty()) {
                tcpcom = ComTcpClient(ip, port.toInt(), channel)
                tcpcom?.connect()
//                connectionStatus.text = "接続中"
                connbtn.text ="接続中"
            }
        }
        GlobalScope.launch(Dispatchers.Main) {
            when (channel.receive()) {
                MSG_CONNECTION_SUCCESS -> {
                    connbtn.text = "成功"
                }
                MSG_CONNECTION_FAILED -> {
                    connbtn.text = "失敗"
                    // エラー処理
                }
                MSG_IOEXCEPTION -> {
                    connbtn.text = "エラー"
                    //エラー処理
                }
            }
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        slotBar.progress = 500//        seekTxt.setText(String.toString(seekBar.progress))

        slotBar.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                // 値が変更された時に呼ばれる
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    slotvalue: Int,
                    fromUser: Boolean
                ) {
                    SlotD = slotvalue
                    slotview.text = "$slotvalue"
                    sendD = SterD.toString()+","+SlotD.toString()+"."
                    try{
                        tcpcom?.sendOrReceive { outputStream, _ ->
                            outputStream.write(sendD.toByteArray())
                        }
                    } catch (e:Exception){}
                }

                // つまみがタッチされた時に呼ばれる
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                // つまみが離された時に呼ばれる
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar!!.setProgress(500, true)
                }
            }
        )
//        val textview = findViewById<TextView>(R.id.steer_text)
        sterBar.progress = 500
//        steertext.setText(String.toString(seekBar2.progress))
        sterBar.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                // 値が変更された時に呼ばれる
                override fun onProgressChanged(
                    seekBar2: SeekBar?,
                    stervalue: Int,
                    fromUser: Boolean
                ) {
                    SterD = stervalue
                    sterview.text = "$stervalue"
                    sendD = SterD.toString()+","+SlotD.toString()+"."
                    try{
                        tcpcom?.sendOrReceive { outputStream, _ ->
                            outputStream.write(sendD.toByteArray())
                        }
                    } catch (e:Exception){}

                }

                // つまみがタッチされた時に呼ばれる
                override fun onStartTrackingTouch(seekBar2: SeekBar?) {
                }

                // つまみが離された時に呼ばれる
                override fun onStopTrackingTouch(seekBar2: SeekBar?) {
                    seekBar2!!.setProgress(500, true)
                }
            }
        )
    }

}
