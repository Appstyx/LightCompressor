package com.abedelazizshe.lightcompressorlibrary

import kotlinx.coroutines.*

class VideoCompressor private constructor(config: Config) : CoroutineScope by MainScope() {

    companion object{
        @Volatile
        private var INSTANCE: VideoCompressor? = null

        @JvmStatic
        fun getInstance(
            config: Config = Config()
        ): VideoCompressor = INSTANCE ?: synchronized(this) {
            INSTANCE ?: VideoCompressor(config).also { INSTANCE = it }
        }

        fun destroy() {
            INSTANCE = null
        }
    }

    private var job: Job = Job()

    private val compressor: Compressor = Compressor.getInstance(config)

   private fun doVideoCompression(srcPath: String, destPath: String, listener: CompressionListener) = launch {
        compressor.isRunning = true
        listener.onStart()
        val result = startCompression(srcPath, destPath, listener)

        // Runs in Main(UI) Thread
        if (result) {
            listener.onSuccess()
        } else {
            listener.onFailure()
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
    private suspend fun startCompression(srcPath: String, destPath: String, listener: CompressionListener) : Boolean = withContext(Dispatchers.IO){

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