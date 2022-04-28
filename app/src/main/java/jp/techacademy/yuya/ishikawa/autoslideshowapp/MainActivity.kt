package jp.techacademy.yuya.ishikawa.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    var picList = mutableListOf<String>()
    var selectedIndex = 0

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


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
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
        println(picList.size)
    }




}