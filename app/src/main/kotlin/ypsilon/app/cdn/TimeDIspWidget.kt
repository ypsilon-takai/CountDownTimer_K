package ypsilon.app.cdn

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class TimeDIspWidget : AppWidgetProvider() {

//    public val bcReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, message: Intent) {
//            val csstate = message.getBooleanExtra("STATE", false)
//            val views = RemoteViews(context.packageName, R.layout.time_disp_widget)
//            if (csstate) {
//                val timeVal = message.getIntExtra("TIME", 0)
//                views.setTextViewText(R.id.txtTimeDispWidget, timeVal.toString())
//            } else {
//                views.setTextViewText(R.id.txtTimeDispWidget, "STANDBY")
//            }
//
//            var awm = AppWidgetManager.getInstance(context)
//            var cn = ComponentName(context, TimeDIspWidget::class.java)
//            awm.updateAppWidget(cn, views)
//        }
//    }



    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val csstate = intent.getBooleanExtra("STATE", false)
        if (csstate) {
            time_second = intent.getIntExtra("TIME", 0)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, TimeDIspWidget.javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)

            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }



    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    //public val filter = IntentFilter("YP_CDT_TIMECHANGE")

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        //context.getApplicationContext().registerReceiver(this, filter)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        var time_second : Int = 0

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            //val widgetText = context.getString(R.string.appwidget_text)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.time_disp_widget)
            views.setTextViewText(R.id.txtTimeDispWidget, time_second.toString())

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

