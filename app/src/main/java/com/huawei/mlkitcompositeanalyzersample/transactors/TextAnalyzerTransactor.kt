package com.huawei.mlkitcompositeanalyzersample.transactors

import android.util.Log
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer

class TextAnalyzerTransactor(private var textTransactorResults: ((MLAnalyzer.Result<MLText.Block>?) -> Unit)?) :
    MLAnalyzer.MLTransactor<MLText.Block> {

    override fun transactResult(p0: MLAnalyzer.Result<MLText.Block>?) {
        textTransactorResults?.invoke(p0)
    }

    override fun destroy() {

    }

}