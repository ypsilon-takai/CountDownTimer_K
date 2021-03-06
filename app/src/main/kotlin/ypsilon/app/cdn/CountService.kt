package ypsilon.app.cdn

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class CountService : Service() {

    private var remainTime: Int = 0
    private var remainPreTime: Int = 0
    private var initialTime: Int = 0

    private var isLoop: Boolean = false

    private var counting: Boolean = false

    private var caller: Caller? = null
    private var ticktick: TickTick? = null

    private var refCount: Int = 0

    override fun onCreate() {
        remainTime = 0
        counting = false

        Log.d("HLGT CS", "CountService create()")

        caller = Caller(this)

        ticktick = object : TickTick() {
            override fun onTick() {
                countDown()
            }
        }

        refCount = 0
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("HLGT CS", "CountService onStartCommand()")

        return Service.START_NOT_STICKY
    }

    override fun onBind(arg0: Intent): IBinder? {

        Log.d("HLGT CS", "CountService onBind()")

        refCount++

        return csifImplement
    }

    override fun onUnbind(arg0: Intent): Boolean {
        Log.d("HLGT CS", " onUnbind()")

        if (refCount > 0) {
            refCount--
        }

        return false
    }


    private var csifImplement: CounterSvcIF.Stub = object : CounterSvcIF.Stub() {

        override fun setTime(time: Int, pretime: Int, is_loop: Boolean): Boolean {
            return this@CountService.setTime(time, pretime, is_loop)
        }

        override fun setloop(is_loop: Boolean) {
            this@CountService.setLoop(is_loop)
        }

        override fun start(time: Int, pretime: Int, is_loop: Boolean) {
            this@CountService.start(time, pretime, is_loop)
        }

        override fun stop() {
            this@CountService.stop()
        }

        override fun end() {
            this@CountService.end()
        }

        override fun pause() {
            this@CountService.pause()
        }

        override fun restart() {
            this@CountService.restart()
        }

        override fun getState(): Bundle {
            return this@CountService.getState()
        }
    }


    // Control functions
    private fun setTime(time: Int, pretime: Int, is_loop: Boolean): Boolean {
        Log.d("HLGT CS", "CountService setTime()")

        return if (!counting) {
            remainTime = time
            initialTime = time
            remainPreTime = pretime
            isLoop = is_loop
            true
        } else {
            false
        }
    }

    private fun setLoop(is_loop: Boolean) {
        this.isLoop = is_loop
    }

    private fun start(time: Int, pretime: Int, is_loop: Boolean) {
        Log.d("HLGT CS", "CountService start()")
        this.setTime(time, pretime, is_loop)
        counting = true
        caller?.say("bless")
        Thread.sleep(150)
        ticktick?.start()
    }


    private fun stop() {

        Log.d("HLGT CS", "CountService stop()")

        ticktick?.cancel()

        remainPreTime = -1
        remainTime = -1

        isLoop = false

        counting = false
    }

    private fun pause() {
        Log.d("HLGT CS", "CountService pause()")

        counting = false
    }


    private fun restart() {
        Log.d("HLGT CS", "CountService restart()")

        counting = true
    }


    private fun end() {
        Log.d("HLGT CS", "CountService end()")

        ticktick?.cancel()

        this.stopSelf()
    }


    private fun getState(): Bundle {
        Log.d("HLGT CS", "CountService getState()")

        val res = Bundle()

        res.putBoolean("BUSY", counting)

        if (remainPreTime > 0) {
            res.putInt("REMAIN", remainPreTime)
        } else {
            res.putInt("REMAIN", remainTime)
        }

        return res
    }


    fun countDown() {
        val message = Intent("YP_CDT_TIMECHANGE")

        if (remainPreTime > 0) {
            //Log.d("CountService", "cd 01");
            caller?.say(remainPreTime)
            // *** call controler
            message.putExtra("STATE", counting)
            message.putExtra("TIME", remainPreTime)
            message.putExtra("INIT", initialTime)
            remainPreTime--
        } else if (remainTime > 0) {
            //Log.d("CountService", "cd 02");
            caller?.say(remainTime)
            // *** call controler
            message.putExtra("STATE", counting)
            message.putExtra("TIME", remainTime)
            message.putExtra("INIT", initialTime)
            remainTime--
        } else if (remainTime <= 0) {
            Log.d("CountService", "cd finish")

            if (isLoop) {
                // looping
                caller!!.say("chime")
                remainTime = initialTime - 1
                message.putExtra("STATE", counting)
                message.putExtra("TIME", initialTime)
                message.putExtra("INIT", initialTime)
            } else {
                // finish
                caller!!.say("finished")
                ticktick!!.cancel()
                counting = false
                // *** call controler
                message.putExtra("STATE", counting)
                if (refCount <= 0) {
                    Log.d("HLGT CS", "CountService Calling stopSelf()")
                    this.stopSelf()
                }
            }
        } else {
            //Log.d("CountService", "cd error");
            caller?.say(remainTime)
            // *** call controler
            message.putExtra("STATE", counting)
            message.putExtra("TIME", remainTime)
            message.putExtra("INIT", initialTime)
            remainTime--
        }

        //Log.d( "CountService", "CountService " + counting + " : rem=" + remainPreTime +  " rem=" + remainTime);

        // send broadcast message
        sendBroadcast(message)

    }
}
