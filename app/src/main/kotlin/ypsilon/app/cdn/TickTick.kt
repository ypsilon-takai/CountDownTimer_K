package ypsilon.app.cdn

import java.util.Timer
import java.util.TimerTask

import android.os.Handler

public open class TickTick {

    private var timer: Timer? = null

    var handler: Handler = android.os.Handler()

    public open fun onTick() {
    }

    public fun start() {
        timer = Timer(true)
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                        onTick()
                }
            }
        }, 0, 1000)
    }

    public fun cancel() {
        timer!!.cancel()
    }

}
