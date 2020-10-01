package com.huawei.mlkitcompositeanalyzersample

import android.content.res.Configuration
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.valueIterator
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLCompositeAnalyzer
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import com.huawei.mlkitcompositeanalyzersample.transactors.FaceAnalyzerTransactor
import com.huawei.mlkitcompositeanalyzersample.transactors.TextAnalyzerTransactor
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val mTextAnalyzer by lazy {
        MLTextAnalyzer.Factory(this).create().apply {
            setTransactor(TextAnalyzerTransactor(textTransactorResults))
        }
    }

    private val mFaceAnalyzer by lazy {
        val setting = MLFaceAnalyzerSetting.Factory()
            .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
            .allowTracing()
            .create()
        MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting).apply {
            setTransactor(FaceAnalyzerTransactor(faceTransactorResults))
        }
    }

    private val mCompositeAnalyzer by lazy {
        MLCompositeAnalyzer.Creator()
            .add(mTextAnalyzer)
            .add(mFaceAnalyzer)
            .create()
    }

    private val lensEngine by lazy {
        val creator = LensEngine.Creator(this, mCompositeAnalyzer)
            .setLensType(LensEngine.BACK_LENS)
            .applyFps(20.0f)
            .enableAutomaticFocus(true)

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT ->
                creator.applyDisplayDimension(
                    resources.displayMetrics.heightPixels,
                    resources.displayMetrics.widthPixels
                ).create()
            else ->
                creator.applyDisplayDimension(
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels
                ).create()
        }
    }


    private val surfaceHolderCallback by lazy {
        object : SurfaceHolder.Callback {

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                lensEngine.close()
                runLensEngine()
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                lensEngine.release()
            }

            override fun surfaceCreated(p0: SurfaceHolder) {
                runLensEngine()
            }
        }
    }

    private fun runLensEngine() {
        try {
            if (mCompositeAnalyzer.isAvailable) {
                lensEngine.run(sv_cameraView.holder)
            }
        } catch (e: Exception) {
            val exception = e as MLException
            val errorCode = exception.errCode
            val message = exception.message

            Log.e("MLCompositeTransactor", "Message: $message , Code: $errorCode")
        }
    }

    private var textTransactorResults: (MLAnalyzer.Result<MLText.Block>?) -> Unit = {
        it?.analyseList?.let { list ->
            Log.d("Transactor", "Text Transactor size: ${list.size()}")
            setText(list)
        }
    }

    private var faceTransactorResults: (MLAnalyzer.Result<MLFace>?) -> Unit = {
        it?.analyseList?.let { list ->
            Log.d("Transactor", "Face Transactor size: ${list.size()}")
            draw(list)
        }
    }


    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (mCompositeAnalyzer.isAvailable) {
                lensEngine.run(sv_cameraView.holder)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPermission.launch(android.Manifest.permission.CAMERA)

        sv_drawView.holder.setFormat(PixelFormat.TRANSPARENT)
        sv_cameraView.holder.addCallback(surfaceHolderCallback)
    }


    private fun draw(faces: SparseArray<MLFace>) {
        val canvas = sv_drawView.holder.lockCanvas()

        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        val paint = Paint().also {
            it.color = Color.YELLOW
            it.style = Paint.Style.STROKE
            it.strokeWidth = 8F
        }

        faces.valueIterator().forEach {
            canvas.drawRect(it.border, paint)
        }

        sv_drawView.holder.unlockCanvasAndPost(canvas)
    }

    private fun setText(texts: SparseArray<MLText.Block>) {
        tv_detectedText.text = texts.valueIterator()
            .asSequence()
            .joinToString {
                "${it.stringValue}\n"
            }
    }

}