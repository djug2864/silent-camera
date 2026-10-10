package com.example.myapp

import android.content.ContentValues
import android.content.Context
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
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null

    private var isFrontCamera = false
    private var isStarting = false

    /**
     * カメラを起動する
     */
    fun startCamera() {
        if (isStarting) return

        isStarting = true

        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener(
            {
                try {
                    cameraProvider = providerFuture.get()
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
            val selector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            // 選択したカメラが利用できるか確認する
            if (!provider.hasCamera(selector)) {
                showMessage("選択したカメラは利用できません")
                isFrontCamera = !isFrontCamera
                return
            }

            // 以前の接続を解除する
            provider.unbindAll()

            // カメラ映像のプレビューを作成する
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            // 写真撮影機能を作成する
            val capture = ImageCapture.Builder()
                .setCaptureMode(
                    ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                )
                .build()

            imageCapture = capture

            // プレビューと撮影機能をカメラに接続する
            provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                capture
            )
        } catch (exception: Exception) {
            imageCapture = null
            showMessage("カメラの設定に失敗しました")
        }
    }

    /**
     * 前面カメラと背面カメラを切り替える
     */
    fun switchCamera() {
        val provider = cameraProvider

        if (provider == null) {
            showMessage("カメラを準備しています")
            return
        }

        isFrontCamera = !isFrontCamera
        bindCameraUseCases()
    }

    /**
     * 写真を撮影して DCIM/Camera に保存する
     */
    fun takePhoto() {
        val capture = imageCapture

        if (capture == null) {
            showMessage("カメラの準備ができていません")
            return
        }

        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.JAPAN
        ).format(Date())

        val fileName = "IMG_$timeStamp.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DCIM}/Camera"
                )
            }
        }

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()

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
     * カメラの接続を解除する
     */
    fun stopCamera() {
        cameraProvider?.unbindAll()
        imageCapture = null
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}