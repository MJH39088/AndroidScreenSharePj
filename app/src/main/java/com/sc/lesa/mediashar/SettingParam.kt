package com.sc.lesa.mediashar

import android.graphics.Color
import androidx.databinding.DataBindingUtil
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import com.sc.lesa.mediashar.config.Config
import com.sc.lesa.mediashar.databinding.ActivitySettingParamBinding

class SettingParam : AppCompatActivity(), View.OnClickListener {
    lateinit var config:Config
    lateinit var binding:ActivitySettingParamBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_setting_param)
        config= Config.getConfig(this)
        binding.config=config
    }

    override fun onResume() {
        super.onResume()
//        val decorView = window.decorView
//        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        val actionBar = supportActionBar
//        actionBar!!.hide()
        val actionBar = supportActionBar
        actionBar!!.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            window.statusBarColor = Color.TRANSPARENT
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN)

        }
    }



    override fun onClick(v: View) {
        if (v.id == R.id.setting_button) {
            try {
                config.width.toInt()
                config.height.toInt()
                config.videoBitrate.toInt()
                config.videoFrameRate.toInt()


                if (config.channelCount.toInt()==1){
                    config.channelMode=AudioFormat.CHANNEL_IN_MONO
                }else if (config.channelCount.toInt()==2){
                    config.channelMode=AudioFormat.CHANNEL_IN_STEREO
                }else{
                    throw Exception(getString(R.string.error_channel_count))
                }
                config.voiceByteRate.toInt()
                config.save(this)
            }catch (t:Throwable){
                t.printStackTrace()
                Toast.makeText(this,"${getString(R.string.error_save)}:${t.message}",Toast.LENGTH_SHORT).show()
            }

        }
    }
}