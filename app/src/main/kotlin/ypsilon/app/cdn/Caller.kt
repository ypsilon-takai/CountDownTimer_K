package ypsilon.app.cdn

import java.util.HashMap

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

public class Caller(private val parentContext: Context) {

    private val spool: SoundPool
    private val wordIdMap: HashMap<String, Int>
    private val numIdMap: HashMap<Int, Int>

    private val conv: Converter

    init {
        conv = Converter()

        spool = SoundPool(10, AudioManager.STREAM_MUSIC, 100)

        wordIdMap = HashMap<String, Int>()
        numIdMap = HashMap<Int, Int>()

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

    public fun say(num: Int) {
        val number_int = Integer.valueOf(num)
        if (numIdMap.containsKey(number_int)) {
            spool.play(numIdMap[number_int] as Int, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    public fun say(word: String) {
        if (wordIdMap.containsKey(word)) {
            spool.play(wordIdMap[word] as Int, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
}
