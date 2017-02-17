package ypsilon.app.cdn

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.NumberPicker
import android.widget.TextView

import kotlinx.android.synthetic.main.controler.*
//import kotlinx.android.synthetic.main.numberinput.*

/**
 * Main window for the timer.
 * @author Ypsilon
 * *
 * @version 0.51
 */

class Control : Activity(), ServiceConnection {

    // Preset button list
    private var buttonList: Array<Button?> = arrayOfNulls(6)
    private var text30secList: Array<TextView?> = arrayOfNulls(6)

    // Preset button time values
    private var buttonTimeList = IntArray(6)

    /**
     * Set time value.
     * Not change during coutdown.
     * (Second)
     */
    private var setTimeVal: Int = 0

    /**
     * Set time value in seconds for '3,2,1' call
     * (Second)
     */
    private var preTimeVal: Int = 0

    /**
     * Flag for timer is running or not.
     */
    private var timerRunning: Boolean = false


    /**
     * Flick params.
     */
    // start pos holder
    private var startXPos: Float = 0.toFloat()

    // flag : true while flicking
    private var flicked: Boolean = false

    /**
     * Bound to the timer service or not
     */
    private var bindToService: Boolean = false

    /**
     * Broadcast receiver for countdown service
     */
    private var bcReceiver: BroadcastReceiver? = null

    /**
     * I/F to countdown service
     */
    private var counterService: CounterSvcIF? = null


    /**
     * Called when the activity is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("HLGT Debug", "Control onCreate()")

        setTimeVal = 60
        preTimeVal = 5

        flicked = false

        bindToService = false

        // AUDIO
        // set volume control to stream while running
        setVolumeControlStream(AudioManager.STREAM_MUSIC)

        // set entire layout
        setContentView(R.layout.controler)

        // font
        val displayFont: Typeface? = Typeface.createFromAsset(getAssets(), "fonts/Seg12Modern.ttf")
        tvTimeView!!.setTypeface(displayFont)
        tvTimeView!!.setText(Converter.formatTimeMinSec(setTimeVal))

        // button list
        buttonList = arrayOf(bt00, bt01, bt02, bt10, bt11, bt12)
        text30secList = arrayOf(tv_btsec00, tv_btsec01, tv_btsec02,
                tv_btsec10, tv_btsec11, tv_btsec12)


        btStartStop!!.setText(getResources().getString(R.string.text_init))
        btStartStopBig!!.setText(getResources().getString(R.string.text_init))

        // ********
        // * Small start button

        // Start or stop countdown.
        btStartStop!!.setOnClickListener {
            if (!flicked) startOrStop()
        }

        // flick function
        btStartStop!!.setOnTouchListener { view, motionEvent ->
            flipTemplate(motionEvent)
            false
        }

        btStartStop!!.setEnabled(false)

        // ********
        // * Flip
        //
        // Flip button window.
        vfSelector!!.setOnTouchListener { view, motionEvent ->
            flipTemplate(motionEvent)
            true
        }

        // ********
        // * Big start button
        //
        // Big button NOT act with short click.
        btStartStopBig!!.setOnClickListener {
            // Do nothing
        }

        // Big button act with long click.
        btStartStopBig!!.setOnLongClickListener {
            startOrStop()
            true
        }

        // Flip button window when user wipe on big button.
        btStartStopBig!!.setOnTouchListener { view, motionEvent ->
            flipTemplate(motionEvent)
            false
        }

        btStartStopBig!!.setEnabled(false)

        /**
         * Restore app state
         */
        // Button state
        buttonTimeList = defaultButtonValues

    }

    private fun setStartButtonsColor(running: Boolean) {
        if (running) {
            btStartStop!!.setBackgroundResource(R.drawable.plastic_red_button)
            btStartStopBig!!.setBackgroundResource(R.drawable.plastic_red_button)
        } else {
            btStartStop!!.setBackgroundResource(R.drawable.plastic_button)
            btStartStopBig!!.setBackgroundResource(R.drawable.plastic_button)
        }
    }

    private fun setStartButtonsText(s: String) {
        btStartStop!!.setText(s)
        btStartStopBig!!.setText(s)
    }

    /*
    private fun setPresetButtonText(bt: Button, s: String) {
        bt.setText(s + getString(R.string.text_min))
    }
    */

    override fun onStart() {
        super.onStart()

        Log.d("HLGT Debug", "Control onStart()")

        timerRunning = false

        // ********
        // * Number buttons
        //
        // restore saved value
        val prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        for(idx in buttonList.indices) {
            buttonTimeList[idx] = prefs.getInt(buttonList[idx]!!.getTag().toString(), buttonTimeList[idx])
        }
        // Change button property
        // -Button text margin to 100
        // -Functions for number button clicked.
        for (idx in buttonList.indices) {
            val bt = buttonList[idx] as Button
            bt.text = Converter.buttonTimeMin(buttonTimeList[idx])

            val sec_txt = text30secList[idx] as TextView
            sec_txt.text = Converter.buttonTimeSec(buttonTimeList[idx])

            bt.setOnClickListener {
                if (!timerRunning) {
                    setTimeOnButtonPush(buttonTimeList[idx])
                }
            }
            bt.setOnLongClickListener {
                showTimeInputDialog(idx)
                true
            }
        }

        // **************
        //  Broadcast receiver
        if (bcReceiver == null) {
            bcReceiver = object : BroadcastReceiver() {

                override fun onReceive(context: Context, message: Intent) {

                    val csstate = message.getExtras().getBoolean("STATE")
                    //Log.d( "HLGT Debug", "Control onReceive()" + csstate + " " + time);

                    if (csstate) {
                        // Restore display state if start while timer service is running
                        if (!timerRunning) {
                            timerRunning = true
                            setStartButtonsColor(true)
                            setStartButtonsText(Converter.formatTimeMinSec(setTimeVal))
                        }

                        setTimeVal = message.getExtras().getInt("INIT", 0)
                        setStartButtonsText(Converter.formatTimeMinSec(setTimeVal))

                        val time = message.getExtras().getInt("TIME", 0)
                        tvTimeView!!.setText(Converter.formatTimeMinSec(time))

                    } else {
                        Log.d("HLGT Debug", "Control onReceive()" + csstate)
                        // countdown finished
                        timerRunning = false
                        resetDisp()
                    }
                }
            }

            val filter = IntentFilter("YP_CDT_TIMECHANGE")
            registerReceiver(bcReceiver, filter)

            // connect or create timer service here
            // service calls back when ready
            if (!bindToService) {
                Log.d("HLGT Debug", "Binding to service.")
                val intent = Intent(applicationContext, CountService::class.java)
                startService(intent)
                bindService(intent, this, Context.BIND_AUTO_CREATE)
            }
        }

    }

    override fun onWindowFocusChanged(hasFocus : Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            for (idx in buttonList.indices) {
                val bt = buttonList[idx] as Button
                bt.setPadding(bt.paddingLeft, bt.paddingTop, (bt.width * 0.5).toInt(), bt.paddingBottom)
                //Log.d("HLGT Debug", "Button pad ${bt.width}")

                val bt_second = text30secList[idx] as TextView
                bt_second.setPadding((bt_second.width * 0.52).toInt(), bt_second.paddingTop,
                        bt_second.paddingRight, bt_second.paddingBottom)
                //Log.d("HLGT Debug", "Button sec pad ${bt_second.width}")
            }
        }
    }

    override fun onPause() {
        // save button values
        val prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        val editor = prefs.edit()
        for(idx in buttonList.indices) {
            editor.putInt(buttonList[idx]!!.getTag().toString(), buttonTimeList[idx])
        }
        editor.apply()
        editor.commit()

        super.onPause()
    }


    override fun onStop() {

        Log.d("HLGT Debug", "Control onStop()")

        // disconnect timer service here
        if (bindToService) {
            unbindService(this)
            bindToService = false
            counterService = null

            unregisterReceiver(bcReceiver)
            bcReceiver = null
        }

        super.onStop()
    }

    override fun onDestroy() {

        Log.d("HLGT Debug", "Control onDestroy()")

        val sstate = getCounterServiceState()
        if (sstate != null) {
            if (!sstate.getBoolean("BUSY")) {
                try {

                    Log.d("HLGT Debug", "CountService Calling stopService()")

                    counterService!!.end()
                    val intent = Intent(applicationContext, CountService::class.java)
                    stopService(intent)
                } catch (e: Exception) {
                    // do nothing
                }

            }
        }

        super.onDestroy()
    }

    /**
     * Gereric function to setup number buttons.
     * @param time_second Setting time.
     */
    private fun setTimeOnButtonPush(time_second: Int) {
        setTimeVal = time_second
        setTimeDisp(setTimeVal)
        if (tgbImmediate!!.isChecked()) {
            startOrStop()
        }

    }

    /**
     * Count down start/stop function.
     */
    private fun startOrStop() {
        if (timerRunning) {
            // STOP
            timerRunning = false
            resetDisp()

            try {
                counterService!!.stop()
            } catch (e: Exception) {
                Log.d("HLGT Debug", e.toString())
            }

        } else {
            // START
            try {
                if (tgbPrecall!!.isChecked()) {
                    counterService!!.start(setTimeVal, preTimeVal)
                } else {
                    counterService!!.start(setTimeVal, 0)
                }

                timerRunning = true
                setStartButtonsColor(true)
                setStartButtonsText(Converter.formatTimeMinSec(setTimeVal))

            } catch (e: Exception) {
                tvTimeView!!.text = "ERROR!"
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    }

    /**
     * Set Time Dispplay.
     * @param time : time as seconds.
     */
    private fun setTimeDisp(time: Int) {
        tvTimeView!!.text = Converter.formatTimeMinSec(time)
    }

    /**
     * Reset display and button text.
     */
    private fun resetDisp() {
        tvTimeView!!.text = Converter.formatTimeMinSec(setTimeVal)
        setStartButtonsColor(false)
        setStartButtonsText(getResources().getString(R.string.text_start))
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    /**
     * Flip button pane with motion event
     * @param event : motion event
     */
    private fun flipTemplate(event: MotionEvent) {
        val endXPos: Float
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                flicked = false
                startXPos = event.getX()
            }
            MotionEvent.ACTION_UP -> {
                endXPos = event.getX()
                if (startXPos - endXPos > 30) {
                    flicked = true
                    vfSelector!!.showNext()
                } else if (startXPos - endXPos < -30) {
                    flicked = true
                    vfSelector!!.showPrevious()
                }
            }
        }
    }

    //**********************
    // Service related funcs

    override fun onServiceConnected(name: ComponentName, service: IBinder) {

        Log.d("HLGT Debug", "Control onServiceConnected()")

        counterService = CounterSvcIF.Stub.asInterface(service)
        bindToService = true

        btStartStop!!.setEnabled(true)
        btStartStopBig!!.setEnabled(true)

        setStartButtonsText(getResources().getString(R.string.text_start))
        setStartButtonsColor(false)

    }

    override fun onServiceDisconnected(name: ComponentName) {

        Log.d("HLGT Debug", "Control onServiceDisconnected()")

        bindToService = false
        counterService = null
    }

    private fun getCounterServiceState(): Bundle? {
        if (counterService != null) {
            try {
                return counterService!!.getState()
            } catch (e: RemoteException) {
                return null
            }

        } else {
            return null
        }
    }

    protected fun showTimeInputDialog(buttonIdx: Int) {

        val builder = AlertDialog.Builder(this)

        val inflater = LayoutInflater.from(this)
        //レイアウトファイルからビューを取得
        val dialog_view = inflater.inflate(R.layout.numberinput, null)
        val npMinutes: NumberPicker = dialog_view.findViewById(R.id.npMinutes) as NumberPicker
        npMinutes.setMaxValue(10)
        npMinutes.setMinValue(0)
        val cb30sec: CheckBox = dialog_view.findViewById(R.id.cb30sec) as CheckBox

        val txButtonName: TextView = dialog_view.findViewById(R.id.txButtonName) as TextView
        txButtonName.text = getString(R.string.dialog_button_name, buttonIdx + 1)

        //レイアウト、題名、OKボタンとキャンセルボタンをつけてダイアログ作成
        builder.setView(dialog_view)
        builder.setTitle(R.string.dialog_title)
        builder.setPositiveButton(R.string.dialog_set, { dialog: DialogInterface?, which: Int ->
            var timesec = npMinutes.value  * 60
            if (cb30sec.isChecked() && npMinutes.value != 10) {
                    timesec += 30
            } else if (timesec == 0) {
                timesec = 30  // minimum value is 30 sec
            }

            buttonTimeList[buttonIdx] = timesec

            buttonList[buttonIdx]!!.text = Converter.buttonTimeMin(timesec)
            text30secList[buttonIdx]!!.text = Converter.buttonTimeSec(timesec)
        })
        builder.setNegativeButton(R.string.dialog_cancel,{ dialog: DialogInterface, which: Int -> })

        val myDialog = builder.create()

        //ダイアログ画面外をタッチされても消えないようにする。
        myDialog.setCanceledOnTouchOutside(false)

        //ダイアログ表示
        myDialog.show()
    }

    companion object {

        // Dialog ids
        //val DIALOG_SET_TIME = 0

        // Default button times
        val defaultButtonValues = intArrayOf(600, 300, 180, 120, 60, 30)
    }

}