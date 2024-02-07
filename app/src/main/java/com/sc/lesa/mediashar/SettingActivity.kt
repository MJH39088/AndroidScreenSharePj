package com.sc.lesa.mediashar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat

class SettingActivity : AppCompatActivity() {
    private lateinit var Button_Back : AppCompatButton
    private var fromActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity_main)

      //  val tutorialButton = findViewById<Button>(R.id.turtu)
        val shareButton = findViewById<Button>(R.id.share)
        val sendEmailButton = findViewById<Button>(R.id.comm)
        val reviewButton = findViewById<Button>(R.id.review)
        val actionBar = supportActionBar
        actionBar?.hide()
        Button_Back = findViewById(R.id.re)

        // 이전 활동 정보 가져오기
        fromActivity = intent.getStringExtra("from_activity")
/*
        tutorialButton.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
        }
*/

        shareButton.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "테스트입니다.")
            startActivity(Intent.createChooser(shareIntent, "공유할 앱을 선택하세요"))
        }

        Button_Back.setOnClickListener {
            navigateBack()
        }

        sendEmailButton.setOnClickListener {
            val email = Intent(Intent.ACTION_SEND)
            email.type = "plain/text"
            val address = arrayOf("idry123454@gmail.com")
            email.putExtra(Intent.EXTRA_EMAIL, address)
            email.putExtra(Intent.EXTRA_SUBJECT, "의견 보내기");
            email.putExtra(Intent.EXTRA_TEXT, "안녕하세요!\n소중한 의견을 주셔서 감사합니다!\n고객님이 주신 소중한 의견\n신중하게 검토 후 답변드리겠습니다:)" +
                    "\n-------------------------------------\n앱 버전 : "+ BuildConfig.VERSION_NAME +"\n기기명 : "+ Build.MODEL +
                    "\n안드로이드 OS : "+ Build.VERSION.RELEASE +"\n-------------------------------------");
            startActivityForResult(email, 1)
        }

        reviewButton.setOnClickListener {
            val uri : Uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
            }
        }
    }

    private fun navigateBack() {
        when (fromActivity) {
            "MediaProjectionActivity" -> {
                val intent = Intent(this, MediaProjectionActivity::class.java)
                startActivity(intent)
            }
            "MainActivity" -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 설정 액티비티에서 돌아왔을 때, 설정 액티비티를 종료하고 현재 액티비틋를 다시 표시
        if (requestCode == 0) {
            finish()
        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

            } else {

            }
        }
    }
}
