package com.example.myapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var statusText: android.widget.TextView

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    /*
     * カメラの使用許可を求める。
     * 許可されたらカメラの起動処理へ進む。
     */
    private val requestCameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                startCameraPreview()
            } else {
                showCameraPermissionDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.cameraPreview)
        statusText = findViewById(R.id.statusText)

        setupButtons()
        checkCameraPermission()
    }

    /*
     * 画面上の各ボタンを設定する。
     */
    private fun setupButtons() {

        // 設定ボタン
        findViewById<android.view.View>(R.id.settingsButton)
            .setOnClickListener {
                openSettingsScreen()
            }

        // 前面・背面カメラ切り替えボタン
        findViewById<android.view.View>(R.id.switchCameraButton)
            .setOnClickListener {
                switchCamera()
            }

        // 写真アプリを開くボタン
        findViewById<android.view.View>(R.id.galleryButton)
            .setOnClickListener {
                openPhotoApp()
            }

        // 撮影ボタン
        findViewById<android.view.View>(R.id.shutterButton)
            .setOnClickListener {
                takePhoto()
            }
    }

    /*
     * カメラの使用許可を確認する。
     */
    private fun checkCameraPermission() {

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            startCameraPreview()
        } else {
            requestCameraPermission.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    /*
     * カメラプレビューを開始する。
     *
     * 実際のプレビュー起動処理は、
     * CameraXの接続処理を追加する手順で実装する。
     */
    private fun startCameraPreview() {

        showStatus("カメラを準備しています…")

        /*
         * 現段階では、CameraXのプレビュー接続処理は
         * まだ追加されていない。
         *
         * 後の手順でカメラを起動する処理を完成させる。
         */
        cameraPreview.post {
            hideStatus()
        }
    }

    /*
     * 前面カメラと背面カメラを切り替える。
     */
    private fun switchCamera() {

        cameraSelector =
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

        /*
         * カメラの切り替えを実際に反映する処理は、
         * CameraXの接続処理を追加する手順で実装する。
         */
        Toast.makeText(
            this,
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                "前面カメラを選択しました"
            } else {
                "背面カメラを選択しました"
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    /*
     * 写真を撮影する。
     */
    private fun takePhoto() {

        /*
         * 写真の撮影・保存処理は、
         * CameraXの撮影機能を追加する手順で実装する。
         */
        Toast.makeText(
            this,
            "撮影機能は次の手順で設定します",
            Toast.LENGTH_SHORT
        ).show()
    }

    /*
     * Googleフォトなどの写真アプリを開く。
     */
    private fun openPhotoApp() {

        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
        }

        try {
            startActivity(
                Intent.createChooser(
                    intent,
                    "写真を見るアプリを選択"
                )
            )
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                "写真アプリを開けませんでした",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /*
     * 設定画面を開く。
     *
     * 設定画面は後の手順で追加する。
     */
    private fun openSettingsScreen() {

        Toast.makeText(
            this,
            "設定画面は後の手順で追加します",
            Toast.LENGTH_SHORT
        ).show()
    }

    /*
     * カメラの使用が許可されなかった場合の案内。
     */
    private fun showCameraPermissionDeniedDialog() {

        AlertDialog.Builder(this)
            .setTitle("カメラの使用許可が必要です")
            .setMessage(
                "このアプリで写真を撮影するには、" +
                    "カメラの使用を許可してください。"
            )
            .setPositiveButton("設定を開く") { _, _ ->

                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                ).apply {
                    data = Uri.fromParts(
                        "package",
                        packageName,
                        null
                    )
                }

                startActivity(intent)
            }
            .setNegativeButton("閉じる", null)
            .show()
    }

    /*
     * 画面に状態メッセージを表示する。
     */
    private fun showStatus(message: String) {
        statusText.text = message
        statusText.visibility = View.VISIBLE
    }

    /*
     * 状態メッセージを非表示にする。
     */
    private fun hideStatus() {
        statusText.visibility = View.GONE
    }
}