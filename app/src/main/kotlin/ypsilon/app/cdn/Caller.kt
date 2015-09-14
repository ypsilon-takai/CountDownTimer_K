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

        for (entry in conv.numToResid.entrySet()) {
            numIdMap.put(entry.getKey(), spool.load(parentContext, entry.getValue().toInt(), 1).toInt())
        }

        for (entry in conv.wordToResid.entrySet()) {
            wordIdMap.put(entry.getKey(), spool.load(parentContext, entry.getValue().toInt(), 1).toInt())
        }
    }

    public fun say(num: Int) {
        val intnum = Integer.valueOf(num)
        if (numIdMap.containsKey(intnum)) {
            spool.play(numIdMap.get(intnum), 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    public fun say(word: String) {
        if (wordIdMap.containsKey(word)) {
            spool.play(wordIdMap.get(word), 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
}
