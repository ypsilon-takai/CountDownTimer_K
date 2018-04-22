package ypsilon.app.cdn

import java.util.HashMap

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.*
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

class Caller(private val parentContext: Context) {

    private val spool: SoundPool
    private val wordIdMap: HashMap<String, Int> = HashMap()
    private val numIdMap: HashMap<Int, Int> = HashMap()

    private val conv: Converter = Converter()

    init {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            spool = SoundPool(10, AudioManager.STREAM_MUSIC, 100)
        } else {
            val attr = AudioAttributes.Builder()
                    .setUsage(USAGE_MEDIA)
                    .setContentType(CONTENT_TYPE_MUSIC)
                    .build()
            spool = SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(10)
                    .build()
        }

        createIdMap()
    }

    private fun createIdMap() {

        for (entry in conv.numToResid.entries) {
            //numIdMap.put(entry.key, spool.load(parentContext, entry.value, 1))
            numIdMap[entry.key] = spool.load(parentContext, entry.value, 1)
        }

        for (entry in conv.wordToResid.entries) {
            wordIdMap[entry.key] = spool.load(parentContext, entry.value, 1)
            //wordIdMap.put(entry.key, spool.load(parentContext, entry.value, 1))
        }
    }

    fun say(num: Int) {
        val numberInt = Integer.valueOf(num)
        if (numIdMap.containsKey(numberInt)) {
            spool.play(numIdMap[numberInt] as Int, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun say(word: String) {
        if (wordIdMap.containsKey(word)) {
            spool.play(wordIdMap[word] as Int, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
}
