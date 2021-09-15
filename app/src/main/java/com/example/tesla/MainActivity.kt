package com.example.tesla

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import kotlinx.coroutines.*
import android.widget.Toast

import kotlinx.coroutines.channels.Channel


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

        val channel = Channel<Int>()
        val buttonConnection: Button = findViewById(R.id.connect)
        buttonConnection.setOnClickListener {
            val ip = "192.168.1.170"
            val port = "55554"
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
