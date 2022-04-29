package jp.techacademy.yuya.ishikawa.autoslideshowapp

import android.Manifest
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Handler
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    var picList = mutableListOf<String>()
    var selectedIndex = 0
    var isStarted = false
    var mTimer: Timer? = null
    var mTimerSec = 0.0
    var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        if (picList.size != 0) {
            imageView.setImageURI(this.picList[0].toUri())
            selectedIndex = 0
        }


        prev_button.setOnClickListener {
            if (selectedIndex == 0) {
                imageView.setImageURI(this.picList[picList.size - 1].toUri())
                selectedIndex = picList.size - 1
            } else {
                imageView.setImageURI(this.picList[selectedIndex - 1].toUri())
                selectedIndex -= 1

            }
        }

        next_button.setOnClickListener {
            if (picList.size - 1 == selectedIndex) {
                imageView.setImageURI(this.picList[0].toUri())
                selectedIndex = 0
            } else {
                imageView.setImageURI(this.picList[selectedIndex + 1].toUri())
                selectedIndex += 1

            }
        }

        start_button.setOnClickListener {
            if (isStarted == false) {

                isStarted = true

                mTimer = Timer()
                mTimerSec = 0.0
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 0.1
                        mHandler.post {
                            if (mTimerSec >= 2.0) {
                                if (picList.size - 1 == selectedIndex) {
                                    imageView.setImageURI(picList[0].toUri())
                                    selectedIndex = 0
                                } else {
                                    imageView.setImageURI(picList[selectedIndex + 1].toUri())
                                    selectedIndex += 1

                                }

                                mTimerSec = 0.0
                            }
                        }
                    }
                }, 0, 100) // 最初に始動させるまで0ミリ秒、ループの間隔を100ミリ秒 に設定

                start_button.text = "停止"
                prev_button.isEnabled = false
                next_button.isEnabled = false


            } else {

                isStarted = false
                mTimer!!.cancel()
                start_button.text = "再生"
                prev_button.isEnabled = true
                next_button.isEnabled = true


            }
        }



    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                    imageView.setImageURI(this.picList[0].toUri())
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("INFO")
                        .setMessage("アクセスを許可しないとアプリを使用できません。　終了します。")
                        .setPositiveButton("OK"){ dialog, which -> finish()}
                        .show()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                this.picList.add(imageUri.toString())


                Log.d("ANDROID", "URI : " + imageUri.toString())
            } while (cursor.moveToNext())
        }
        cursor.close()
        //println(picList.size)
    }




}