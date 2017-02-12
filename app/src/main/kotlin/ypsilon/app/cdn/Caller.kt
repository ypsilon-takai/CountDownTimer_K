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
    private val wordIdMap: HashMap<String, Int> = HashMap<String, Int>()
    private val numIdMap: HashMap<Int, Int> = HashMap<Int, Int>()

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
            numIdMap.put(entry.key, spool.load(parentContext, entry.value.toInt(), 1).toInt())
        }

        for (entry in conv.wordToResid.entries) {
            wordIdMap.put(entry.key, spool.load(parentContext, entry.value.toInt(), 1).toInt())
        }
    }

    fun say(num: Int) {
        val number_int = Integer.valueOf(num)
        if (numIdMap.containsKey(number_int)) {
            spool.play(numIdMap[number_int] as Int, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun say(word: String) {
        if (wordIdMap.containsKey(word)) {
            spool.play(wordIdMap[word] as Int, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
}
