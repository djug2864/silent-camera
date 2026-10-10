package com.example.myapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var statusText: TextView

    private var cameraManager: CameraManager? = null

    private val requestCameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                showCameraPermissionDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.cameraPreview)
        statusText = findViewById(R.id.statusText)

        cameraManager = CameraManager(
            context = this,
            lifecycleOwner = this,
            previewView = cameraPreview
        )

        setupButtons()
        checkCameraPermission()
    }

    /**
     * 各ボタンの動作を設定する
     */
    private fun setupButtons() {
        findViewById<View>(R.id.settingsButton)
            .setOnClickListener {
                openSettingsScreen()
            }

        findViewById<View>(R.id.switchCameraButton)
            .setOnClickListener {
                cameraManager?.switchCamera()
            }

        findViewById<View>(R.id.galleryButton)
            .setOnClickListener {
                openPhotoPicker()
            }

        findViewById<View>(R.id.shutterButton)
            .setOnClickListener {
                cameraManager?.takePhoto()
            }
    }

    /**
     * カメラの使用許可を確認する
     */
    private fun checkCameraPermission() {
        val isGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            startCamera()
        } else {
            requestCameraPermission.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    /**
     * カメラを起動する
     */
    private fun startCamera() {
        showStatus("カメラを準備しています…")
        cameraManager?.startCamera()

        // プレビュー領域の準備後に案内表示を消す
        cameraPreview.postDelayed(
            {
                hideStatus()
            },
            1500
        )
    }

    /**
     * 写真選択画面を開く
     */
    private fun openPhotoPicker() {
        try {
            val intent = Intent(
                Intent.ACTION_GET_CONTENT
            ).apply {
                type = "image/*"
            }

            startActivity(
                Intent.createChooser(
                    intent,
                    "写真を見るアプリを選択"
                )
            )
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                "写真を開けませんでした",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 設定ボタン
     *
     * 専用の設定画面は、後の手順で追加する
     */
    private fun openSettingsScreen() {
        Toast.makeText(
            this,
            "設定画面はまだ作成していません",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * カメラの使用を拒否された場合の案内
     */
    private fun showCameraPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("カメラの使用許可が必要です")
            .setMessage(
                "このアプリで写真を撮影するには、" +
                    "カメラの使用を許可してください。\n\n" +
                    "許可する場合は、アプリの設定から" +
                    "カメラへのアクセスを有効にしてください。"
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

    private fun showStatus(message: String) {
        statusText.text = message
        statusText.visibility = View.VISIBLE
    }

    private fun hideStatus() {
        statusText.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()

        // 設定画面から戻ったとき、許可状態を再確認する
        if (::cameraPreview.isInitialized &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraManager?.startCamera()
        }
    }

    override fun onStop() {
        cameraManager?.stopCamera()
        super.onStop()
    }
}