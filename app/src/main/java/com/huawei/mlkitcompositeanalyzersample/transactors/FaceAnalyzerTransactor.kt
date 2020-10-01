package com.huawei.mlkitcompositeanalyzersample.transactors

import android.util.Log
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.face.MLFace


class FaceAnalyzerTransactor(private var faceTransactorResults: ((MLAnalyzer.Result<MLFace>?) -> Unit)?) :
    MLAnalyzer.MLTransactor<MLFace> {

    override fun transactResult(p0: MLAnalyzer.Result<MLFace>?) {
        faceTransactorResults?.invoke(p0)
    }

    override fun destroy() {

    }

}