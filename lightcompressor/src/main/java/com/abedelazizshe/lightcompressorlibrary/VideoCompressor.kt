package com.abedelazizshe.lightcompressorlibrary

import kotlinx.coroutines.*

class VideoCompressor(config: Config = Config()) : CoroutineScope by MainScope() {

    private var job: Job = Job()

    private val compressor: Compressor = Compressor(config)

   private fun doVideoCompression(srcPath: String, destPath: String, listener: CompressionListener) = launch {
        compressor.isRunning = true
        listener.onStart()
        val result = startCompression(srcPath, destPath, listener)

        // Runs in Main(UI) Thread
        if (result.succeeded) {
            listener.onSuccess()
        } else {
            listener.onFailure(result.errorMessage)
        }

    }

    fun start(srcPath: String, destPath: String, listener: CompressionListener){
       job =  doVideoCompression(srcPath, destPath, listener)
    }

    fun cancel(){
        job.cancel()
        compressor.isRunning = false
    }

    // To run code in Background Thread
    private suspend fun startCompression(srcPath: String, destPath: String, listener: CompressionListener) : CompressionResult = withContext(Dispatchers.IO){

        return@withContext compressor.compressVideo(srcPath, destPath, object : CompressionProgressListener {
            override fun onProgressChanged(percent: Float) {
                listener.onProgress(percent)
            }

            override fun onProgressCancelled() {
                listener.onCancelled()
            }
        })
    }

}