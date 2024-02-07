package com.sc.lesa.mediashar.jlib.server

import android.util.Log
import com.sc.lesa.mediashar.jlib.io.ByteObjectInputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

abstract class SocketClientThread(ip: String, port: Int) : Thread(TAG) {
    companion object{
        val TAG = "!@"
    }
    var dataPackList = DataPackList()
    private val client: Socket = Socket()
    private var exit = false
    var socketAddress: SocketAddress = InetSocketAddress(ip, port)
    lateinit var inputStream: InputStream
    lateinit var dataInput: ByteObjectInputStream

    /**
     * @throws Exception
     */
    fun connect() {
        Log.d("!@", "connect: 12312333${socketAddress}")
        client.connect(socketAddress)
        Log.d(TAG, "연결 성공")
    }

    override fun run() {
        try {
            inputStream = client.getInputStream()
            dataInput = ByteObjectInputStream(inputStream)
            while (!exit) {
                val bytes: ByteArray = dataInput.readObject()
                dataPackList.putDataPack(bytes)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            onError(e)
        } finally {
            dataInput.close()
            inputStream.close()
            client.close()
        }
        Log.d(TAG, "성공적으로 종료됨")
    }

    fun exit() {
        exit = true
        super.interrupt()
    }

    abstract fun onError(t:Throwable)

}