package ypsilon.app.cdn

import android.content.Context
import java.util.HashMap


class Converter {

    var numToResid: HashMap<Int, Int>
    var wordToResid: HashMap<String, Int>

    init {

        wordToResid = HashMap<String, Int>()
        numToResid = HashMap<Int, Int>()

        initSoundData()

    }

    private fun initSoundData() {

        wordToResid.put("start", R.raw.voice_start)
        wordToResid.put("finished", R.raw.horn)

        numToResid.put(0, R.raw.voice_000)
        numToResid.put(1, R.raw.voice_001)
        numToResid.put(2, R.raw.voice_002)
        numToResid.put(3, R.raw.voice_003)
        numToResid.put(4, R.raw.voice_004)
        numToResid.put(5, R.raw.voice_005)
        numToResid.put(6, R.raw.voice_006)
        numToResid.put(7, R.raw.voice_007)
        numToResid.put(8, R.raw.voice_008)
        numToResid.put(9, R.raw.voice_009)
        numToResid.put(10, R.raw.voice_010)
        numToResid.put(11, R.raw.voice_011)
        numToResid.put(12, R.raw.voice_012)
        numToResid.put(13, R.raw.voice_013)
        numToResid.put(14, R.raw.voice_014)
        numToResid.put(15, R.raw.voice_015)
        numToResid.put(16, R.raw.voice_016)
        numToResid.put(17, R.raw.voice_017)
        numToResid.put(18, R.raw.voice_018)
        numToResid.put(19, R.raw.voice_019)
        numToResid.put(20, R.raw.voice_020)
        numToResid.put(21, R.raw.voice_silent_05)
        numToResid.put(25, R.raw.voice_025)
        numToResid.put(26, R.raw.voice_silent_05)
        numToResid.put(30, R.raw.voice_030)
        numToResid.put(31, R.raw.voice_silent_05)
        numToResid.put(35, R.raw.voice_035)
        numToResid.put(36, R.raw.voice_silent_05)
        numToResid.put(40, R.raw.voice_040)
        numToResid.put(41, R.raw.voice_silent_05)
        numToResid.put(45, R.raw.voice_045)
        numToResid.put(46, R.raw.voice_silent_05)
        numToResid.put(50, R.raw.voice_050)
        numToResid.put(51, R.raw.voice_silent_05)
        numToResid.put(55, R.raw.voice_055)
        numToResid.put(56, R.raw.voice_silent_05)
        numToResid.put(60, R.raw.voice_01_00)
        numToResid.put(61, R.raw.voice_silent_05)
        numToResid.put(90, R.raw.voice_01_30)
        numToResid.put(91, R.raw.voice_silent_05)
        numToResid.put(120, R.raw.voice_02_00)
        numToResid.put(121, R.raw.voice_silent_05)
        numToResid.put(150, R.raw.voice_02_30)
        numToResid.put(151, R.raw.voice_silent_05)
        numToResid.put(180, R.raw.voice_03_00)
        numToResid.put(181, R.raw.voice_silent_05)
        numToResid.put(210, R.raw.voice_03_30)
        numToResid.put(211, R.raw.voice_silent_05)
        numToResid.put(240, R.raw.voice_04_00)
        numToResid.put(241, R.raw.voice_silent_05)
        numToResid.put(270, R.raw.voice_04_30)
        numToResid.put(271, R.raw.voice_silent_05)
        numToResid.put(300, R.raw.voice_05_00)
        numToResid.put(301, R.raw.voice_silent_05)
        numToResid.put(330, R.raw.voice_05_30)
        numToResid.put(331, R.raw.voice_silent_05)
        numToResid.put(360, R.raw.voice_06_00)
        numToResid.put(361, R.raw.voice_silent_05)
        numToResid.put(390, R.raw.voice_06_30)
        numToResid.put(391, R.raw.voice_silent_05)
        numToResid.put(420, R.raw.voice_07_00)
        numToResid.put(421, R.raw.voice_silent_05)
        numToResid.put(450, R.raw.voice_07_30)
        numToResid.put(451, R.raw.voice_silent_05)
        numToResid.put(480, R.raw.voice_08_00)
        numToResid.put(481, R.raw.voice_silent_05)
        numToResid.put(510, R.raw.voice_08_30)
        numToResid.put(511, R.raw.voice_silent_05)
        numToResid.put(540, R.raw.voice_09_00)
        numToResid.put(541, R.raw.voice_silent_05)
        numToResid.put(570, R.raw.voice_09_30)
        numToResid.put(571, R.raw.voice_silent_05)
        numToResid.put(600, R.raw.voice_10_00)
    }

    fun getResId(num: Int): Int {
        return numToResid.getOrElse(num){return -1}
    }

    fun getResId(word: String): Int? {
        return wordToResid.getOrElse(word){return -1}
    }

    companion object {

        private fun Int.format(digits: Int): String {
            return java.lang.String.format("%0${digits}d", this)
        }

        fun formatTimeMinSec(seconds: Int): String {
            //Log.d( "HLGT Debug", "seconds = " + seconds );

            val min: Int = seconds / 60
            val sec: Int = seconds % 60

            return "${min.format(2)}:${sec.format(2)}"
        }

        fun formatTimeSec(seconds: Int): kotlin.String {
            val sec: Int = seconds / 60

            return "${sec.format(2)}"
        }

        fun formatTimeMin(seconds: Int): kotlin.String {
            val min: Int = seconds % 60

            return "${min.format(2)}"
        }


        fun buttonTimeMin(seconds: Int): kotlin.String {
            val min = seconds / 60

            return "${min}"
        }
        fun buttonTimeSec(seconds: Int): kotlin.String {
            val sec = seconds % 60

            return ":${sec.format(2)}"
        }

        /*
        fun buttonTimeSec(seconds: Int, ct: Context): kotlin.String {
            val output: kotlin.String

            //Log.d( "HLGT Debug", "seconds = " + seconds );

            val min = seconds / 60
            val sec = seconds % 60

            val secondsD = sec.toInt()
            val minutesD = min.toInt()

            if (seconds < 60) {
                //output = "$secondsD${ct.getString(R.string.text_sec)}"
                output = "$secondsD"
            } else {
                //output = "$minutesD${ct.getString(R.string.text_min)}"
                output = "$minutesD"
            }

            return output
        }
        */
    }
}
