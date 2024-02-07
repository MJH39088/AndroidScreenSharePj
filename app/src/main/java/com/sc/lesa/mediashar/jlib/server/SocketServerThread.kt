package com.sc.lesa.mediashar.jlib.server

import android.util.Log
import com.sc.lesa.mediashar.jlib.io.ByteObjectOutputStream
import com.sc.lesa.mediashar.jlib.io.DataPack
import com.sc.lesa.mediashar.jlib.io.Writable

import com.sc.lesa.mediashar.jlib.util.toByteArray

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue

abstract class SocketServerThread(var port: Int) : Thread(TAG) {

    companion object{
        val TAG = SocketServerThread::class.java.name
        fun buildVideoPack(writable:Writable):ByteArray{
            Log.d("!@", "video에 bufferListVideo의 머리 요소 반환")
            val pack = DataPack(DataPack.TYPE_VIDEO,writable.toByteArray())
            return pack.toByteArray()
        }

        fun buildVoicePack(writable:Writable):ByteArray{
            val pack = DataPack(DataPack.TYPE_VOICE,writable.toByteArray())
            return pack.toByteArray()
        }
    }

    lateinit var serverSocket: ServerSocket
    var exit = false
    var bufferListVideo: LinkedBlockingQueue<Writable> = LinkedBlockingQueue(100)
    var bufferListVoice: LinkedBlockingQueue<Writable> = LinkedBlockingQueue(100)
    lateinit var socket: Socket
    lateinit var socketout: OutputStream
    lateinit var dataOutput: ByteObjectOutputStream


    override fun run() {
        try {
            Log.d("!@", "port open")
            serverSocket = ServerSocket(port)
            serverSocket.soTimeout=3000
        }catch (t:Throwable){
            t.printStackTrace()
            //处理返回
            onError(t)
            return
        }
        while (!exit) {
            try {
                try {
                    socket = serverSocket.accept()
                }catch (t:Throwable){
                    continue
                }
                Log.d("!@", "client connected")
                Log.d("!@", "getoutputStream")
                socketout = socket.getOutputStream()
                Log.d("!@", "dataOutput 직렬화 시도")
                dataOutput = ByteObjectOutputStream(socketout)
                while (!exit) {
                    Log.d("!@", "video에 bufferListVideo의 머리 요소 반환")
                    val video: Writable? = bufferListVideo.peek ()
                    if (video != null) {
                        Log.d("!@", "DataPack.kt에 Video 타입과 전송중인 ByteArray 반환")
                        val tmp = buildVideoPack(video)
                        Log.d("!@", "ByteObjectOutputStream.java의 bufferedOutputStream 변수에 직렬화 데이터 저장")
                        dataOutput.writeObject(tmp)
                        Log.d("!@", "has send video pack")
                        Log.d("!@", "다 보냈으면 queue 비움")
                        bufferListVideo.remove(video)
                        Log.d("!@", "연결이 끊길 때 까지 반복")
                    }
                    val voice: Writable? = bufferListVoice.peek()
                    if (voice != null) {
                        val tmp = buildVoicePack(voice)
                        dataOutput.writeObject(tmp)
                        Log.d("!@", "has send voice pack")
                        bufferListVoice.remove(voice)
                    }
                }
                Log.d("!@", "연결이 끊겨 직렬화 데이터 전송 중지")
                dataOutput.close()
            } catch (e: Throwable) {
                e.printStackTrace()
                socket.close()
                Log.d(TAG, "client has disconnect")

            }
        }
        close()
    }

    fun putVoicePack(writable: Writable) {
        bufferListVoice.offer(writable)
    }

    fun putVideoPack(writable: Writable?) {
        Log.d("!@", "bufferListVideo에 있는 요소를 반환하고 true 반환")
        bufferListVideo.offer(writable)
    }

    private fun close() {
        Log.d("!@", "port close")
        serverSocket.close()

    }

    fun exit() {
        Log.d("!@", "退出中")
        exit = true
        interrupt()
    }

    abstract fun onError(t:Throwable)

}