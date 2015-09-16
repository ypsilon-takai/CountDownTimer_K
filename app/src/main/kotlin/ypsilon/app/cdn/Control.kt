package ypsilon.app.cdn

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.ToggleButton
import android.widget.ViewFlipper
import java.util.prefs.PreferenceChangeEvent

import java.util.Arrays.asList

import kotlinx.android.synthetic.controler.*
import kotlinx.android.synthetic.numberinput.*

/**
 * Main window for the timer.
 * @author Ypsilon
 * *
 * @version 0.51
 */

public class Control : Activity(), ServiceConnection {

    // Time display
    private var tvTimeView: TextView? = null

    private var vfSelector: ViewFlipper? = null

    // Many button layout
    private var btStartStop: Button? = null
    private var bt00: Button? = null
    private var bt01: Button? = null
    private var bt02: Button? = null
    private var bt10: Button? = null
    private var bt11: Button? = null
    private var bt12: Button? = null
    private var buttonList: List<Button>? = null

    private var tgbImmediate: ToggleButton? = null
    private var tgbPrecall: ToggleButton? = null

    // Preset button time values
    private var btTimesecList: ShortArray? = null


    // One button layout
    private var btStartStopBig: Button? = null

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
        super<Activity>.onCreate(savedInstanceState)

        Log.d("HLGT Debug", "Control onCreate()")


        setTimeVal = 60
        preTimeVal = 5

        flicked = false

        bindToService = false

        // set volumecontrol to stream while running
        setVolumeControlStream(AudioManager.STREAM_MUSIC)

        // set entire layout
        setContentView(R.layout.controler)

        // Setup timer display area
        //tvTimeView = findViewById(R.id.tvTimeView) as TextView

        // -- font
        val dispfont = Typeface.createFromAsset(getAssets(), "fonts/Seg12Modern.ttf")
        tvTimeView!!.setTypeface(dispfont)

        // flipper
        //vfSelector = findViewById(R.id.vfSelector) as ViewFlipper

        // buttons
        //btStartStop = findViewById(R.id.btStartStop) as Button
        //bt00 = findViewById(R.id.bt00) as Button
        //bt01 = findViewById(R.id.bt01) as Button
        //bt02 = findViewById(R.id.bt02) as Button
        //bt10 = findViewById(R.id.bt10) as Button
        //bt11 = findViewById(R.id.bt11) as Button
        //bt12 = findViewById(R.id.bt12) as Button
        buttonList = asList<Button>(bt00, bt01, bt02, bt10, bt11, bt12)

        //tgbImmediate = findViewById(R.id.tgbImmediate) as ToggleButton
        //tgbPrecall = findViewById(R.id.tgbPrecall) as ToggleButton

        //btStartStopBig = findViewById(R.id.btStartStopBig) as Button



        // Converter class provides format exchange functionality.
        tvTimeView!!.setText(Converter.formatTimeSec(setTimeVal))
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
        btStartStopBig!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                flipTemplate(event)
                return false
            }
        })
        btStartStopBig!!.setOnTouchListener { view, motionEvent ->
            flipTemplate(motionEvent)
            false
        }

        btStartStopBig!!.setEnabled(false)


        // restore state
        if (savedInstanceState != null) {
            // restore button value
            val strdBtValues: ShortArray? = savedInstanceState.getShortArray("buttonValue")
            if (strdBtValues != null) {
                btTimesecList = strdBtValues
            } else {
                btTimesecList = shortArrayOf(600, 300, 180, 120, 60, 30)
            }
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
            btTimesecList = ShortArray(6)
            btTimesecList = shortArrayOf(
                    (prefs.getInt("bt0", 600)).toShort(),
                    (prefs.getInt("bt1", 600)).toShort(),
                    (prefs.getInt("bt2", 600)).toShort(),
                    (prefs.getInt("bt3", 600)).toShort(),
                    (prefs.getInt("bt4", 600)).toShort(),
                    (prefs.getInt("bt5", 600)).toShort())
        }

        // ********
        // * Number buttons
        //
        // Functions for number button clicked.
        val it = buttonList!!.listIterator()
        while (it.hasNext()) {
            val idx = it.nextIndex()
            val bt = it.next()
            bt.setText(Converter.buttonTimeSec(btTimesecList!![idx].toInt(), getApplicationContext()))
            bt.setOnClickListener {
                if (!timerRunning) {
                    setTimeOnButtonPush(btTimesecList!![idx].toInt())
                }
            }
            bt.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View): Boolean {
                    showTimeInputDialog(idx)
                    return true
                }
            })
        }

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

    private fun setPresetButtonText(bt: Button, s: String) {
        bt.setText(s + getString(R.string.text_min))
    }

    override fun onStart() {
        super<Activity>.onStart()

        Log.d("HLGT Debug", "Control onStart()")

        timerRunning = false

        // **************
        //  Broadcast receiver
        if (bcReceiver == null) {
            bcReceiver = object : BroadcastReceiver() {

                override fun onReceive(context: Context, message: Intent) {

                    val csstate = message.getExtras().getBoolean("STATE")
                    if (csstate) {

                        //        				Log.d( "HLGT Debug", "Control onReceive()" + csstate + " " + time);

                        if (!timerRunning) {
                            timerRunning = true
                            setStartButtonsColor(true)
                            setStartButtonsText(Converter.formatTimeSec(setTimeVal))
                        }

                        setTimeVal = message.getExtras().getInt("INIT", 0)
                        setStartButtonsText(Converter.formatTimeSec(setTimeVal))

                        val time = message.getExtras().getInt("TIME", 0)
                        tvTimeView!!.setText(Converter.formatTimeSec(time))

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
                val intent = Intent(this, javaClass<CountService>())
                startService(intent)
                bindService(intent, this, Context.BIND_AUTO_CREATE)
            }
        }

    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putShortArray("buttonValue", btTimesecList)

        super<Activity>.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super<Activity>.onRestoreInstanceState(savedInstanceState)

        // restore button value
        val strdBtValues = savedInstanceState.getShortArray("buttonValue")
        if (strdBtValues != null) {
            btTimesecList = strdBtValues
        } else {
            btTimesecList = shortArrayOf(600, 300, 180, 120, 60, 30)
        }

        val it = buttonList!!.listIterator()
        while (it.hasNext()) {
            val idx = it.nextIndex()
            val bt = it.next()
            bt.setText(Converter.buttonTimeSec(btTimesecList!![idx].toInt(), this))
        }
    }

    override fun onStop() {

        Log.d("HLGT Debug", "Control onStop()")

        // disconnecttimer service here
        if (bindToService) {
            unbindService(this)
            bindToService = false
            counterService = null

            unregisterReceiver(bcReceiver)
            bcReceiver = null
        }

        // save button values
        val prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        val edit = prefs.edit()
        edit.putInt("bt0", btTimesecList!![0].toInt())
        edit.putInt("bt1", btTimesecList!![1].toInt())
        edit.putInt("bt2", btTimesecList!![2].toInt())
        edit.putInt("bt3", btTimesecList!![3].toInt())
        edit.putInt("bt4", btTimesecList!![4].toInt())
        edit.putInt("bt5", btTimesecList!![5].toInt())
        edit.commit()

        super<Activity>.onStop()
    }

    override fun onDestroy() {

        Log.d("HLGT Debug", "Control onDestroy()")

        val sstate = getCounterServiceState()
        if (sstate != null) {
            if (!sstate.getBoolean("BUSY")) {
                try {

                    Log.d("HLGT Debug", "CountService Calling stopService()")

                    counterService!!.end()
                    val intent = Intent(this, javaClass<CountService>())
                    stopService(intent)
                } catch (e: Exception) {
                    // do nothing
                }

            }
        }

        super<Activity>.onDestroy()
    }

    /**
     * Gereric function to setup number buttons.
     * @param timesec Setting time.
     */
    private fun setTimeOnButtonPush(timesec: Int) {
        setTimeVal = timesec
        setTimeDisp(setTimeVal)
        if (tgbImmediate!!.isChecked()) {
            startOrStop()
        }

    }

    /**
     * Count down start/stop function.
     */
    public fun startOrStop() {
        if (timerRunning) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            timerRunning = false
            resetDisp()

            try {
                counterService!!.stop()
            } catch (e: Exception) {
                Log.d("HLGT Debug", e.toString())
            }

        } else {
            try {
                if (tgbPrecall!!.isChecked()) {
                    counterService!!.start(setTimeVal, preTimeVal)
                } else {
                    counterService!!.start(setTimeVal, 0)
                }

                timerRunning = true
                setStartButtonsColor(true)
                setStartButtonsText(Converter.formatTimeSec(setTimeVal))

            } catch (e: Exception) {
                tvTimeView!!.setText("ERROR!")
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    }

    /**
     * Set Time Dispplay.
     * @param time : time as seconds.
     */
    public fun setTimeDisp(time: Int) {
        val st = Converter.formatTimeSec(time)
        tvTimeView!!.setText(st)
    }

    /**
     * Reset display and button text.
     */
    private fun resetDisp() {
        tvTimeView!!.setText(Converter.formatTimeSec(setTimeVal))
        setStartButtonsColor(false)
        setStartButtonsText(getResources().getString(R.string.text_start))
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

    // time input dialog
    /*
    override fun onCreateDialog(id: Int): Dialog {
        val dialog: Dialog?
        when (id) {
            DIALOG_SET_TIME -> // do the work to define the pause Dialog
                dialog = null
            else -> dialog = null
        }
        return dialog
    }
    */

    protected fun showTimeInputDialog(buttonIdx: Int) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        //レイアウトファイルからビューを取得
        val dialog_view = inflater.inflate(R.layout.numberinput, null)

        //val npMinutes = dialog_view.findViewById(R.id.npMinutes) as NumberPicker
        npMinutes.setMaxValue(10)
        npMinutes.setMinValue(0)

        //val txButtonName = dialog_view.findViewById(R.id.txButtonName) as TextView
        txButtonName.setText(getString(R.string.dialog_button_name, buttonIdx + 1))

        //レイアウト、題名、OKボタンとキャンセルボタンをつけてダイアログ作成
        builder.setView(dialog_view)
        builder.setTitle(R.string.dialog_title)
        builder.setPositiveButton(R.string.dialog_set, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                //val npMinutes = dialog_view.findViewById(R.id.npMinutes) as NumberPicker
                var timesec = (npMinutes.getValue() * 60).toShort()
                if (timesec.equals(0)) {
                    timesec = 30
                }
                //if (cb30sec.isChecked() && numPicker.getValue() < 10) {
                //    timesec += 30;
                //}
                btTimesecList!!.set(buttonIdx, timesec)

                buttonList!!.get(buttonIdx).setText(Converter.buttonTimeSec(timesec.toInt(), getApplicationContext()))

            }
        })
        builder.setNegativeButton(R.string.dialog_cancel, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                /*キャンセルされたときの処理*/
            }
        })

        val myDialog = builder.create()

        //ダイアログ画面外をタッチされても消えないようにする。
        myDialog.setCanceledOnTouchOutside(false)

        //ダイアログ表示
        myDialog.show()
    }

    companion object {

        // Dialog ids
        val DIALOG_SET_TIME = 0
    }

}