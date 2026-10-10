package com.example.myapp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * カメラの起動・切り替え・撮影・写真保存を管理するクラス
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    /** CameraX のカメラ管理オブジェクト */
    private var cameraProvider: ProcessCameraProvider? = null

    /** 写真撮影に使用するオブジェクト */
    private var imageCapture: ImageCapture? = null

    /** 現在選択しているカメラ */
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    /** カメラの起動処理が進行中かどうか */
    private var isStarting = false

    /**
     * カメラを起動する
     */
    fun startCamera() {
        if (isStarting) return

        isStarting = true

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            {
                try {
                    cameraProvider = cameraProviderFuture.get()
                    bindCameraUseCases()
                } catch (exception: Exception) {
                    showMessage("カメラを起動できませんでした")
                } finally {
                    isStarting = false
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    /**
     * プレビューと撮影機能をカメラに接続する
     */
    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return

        try {
            // 以前のカメラ接続を解除する
            provider.unbindAll()

            // プレビューを作成する
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            // 写真撮影機能を作成する
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(
                    ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                )
                .build()

            // カメラをライフサイクルに接続する
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

        } catch (exception: Exception) {
            showMessage("カメラの設定に失敗しました")
        }
    }

    /**
     * 前面カメラと背面カメラを切り替える
     */
    fun switchCamera() {
        cameraSelector =
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

        // 選択したカメラでプレビューを再接続する
        bindCameraUseCases()
    }

    /**
     * 写真を撮影して保存する
     */
    fun takePhoto() {
        val capture = imageCapture

        if (capture == null) {
            showMessage("カメラの準備ができていません")
            return
        }

        // 保存する写真のファイル名を作成する
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.JAPAN
        ).format(Date())

        val fileName = "IMG_$timeStamp.jpg"

        // 写真の保存先情報を設定する
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            // Android 10 以降は DCIM/Camera に保存する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DCIM}/Camera"
                )
            }
        }

        // 保存先を指定する
        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()

        // 撮影して保存する
        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {
                    showMessage("写真を保存しました")
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {
                    showMessage("撮影に失敗しました")
                }
            }
        )
    }

    /**
     * メッセージを画面に表示する
     */
    private fun showMessage(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * カメラの接続を解除する
     */
    fun stopCamera() {
        cameraProvider?.unbindAll()
        imageCapture = null
    }
}