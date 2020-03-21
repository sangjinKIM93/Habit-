package com.sangjin.habit.Activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sangjin.habit.R;

public class WidgetListView extends AppWidgetProvider {
    public static final String TOAST_ACTION = "com.example.android.stackwidget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";

    SharedPreferences sharedprefer;

    /**
     * 위젯의 크기 및 옵션이 변경될 때마다 호출되는 함수
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //여기부분 다 사용할 일 없어져서 주석처리함!
        //CharSequence widgetText = context.getString("example");
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_listview);
        //views.setTextViewText(R.id.widget_test_textview, widgetText);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }

    /**
     * 위젯이 바탕화면에 설치될 때마다 호출되는 함수
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            // RemoteViewsService 실행 등록시키는 함수 -> Factory작동을 위해서     -> Factory는 위젯에 담기는 뷰들을 생성해줌.(listview가 아닌 간단한 텍스트뷰라면 필요없음.)
            Intent serviceIntent = new Intent(context, MyRemoteViewsService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);   //위젯ID를 담는다. 왜? 나중에 appWidgetManager를 작동시킬때 필요할듯.
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));    //무시당하지 않기 위해서라는데... 2개 이상의 인텐트가 있을때 extra 데이터를 잃을 수도 있나?  지금은 2개 이상의 인텐트가 아닌데 그럼 없어도 되는 코드인가?
            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_listview);
            widget.setRemoteAdapter(R.id.widget_listview, serviceIntent);

            //Listview 클릭 이벤트를 위한 코드. -> 원리는 pendingIntent 부여 -> 하나씩 부여하기에는 부담이 되어서 각 항목의 클릭이 아닌 위젯 자체에 대한 클릭
            Intent toastIntent = new Intent(context, WidgetListView.class);     //WidgetListView를 호출하고
            toastIntent.setAction(WidgetListView.TOAST_ACTION);                 //TOAST_ACTION을 발생시켜 onReceive가 해당 작업을 수행할 수 있도록 한다.
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));        //같은 인텐트가 대기하고 있는데 또 오면 기존 인텐트의 extra값이 변할 수 있다. 그렇기 때문에 setData에 따로 지정해주는 것이다.
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);                                                         //특정시점에 toastIntent를 실행해 -> 특정시점은 pendingIntentTemplate과 연관이 있을 것 같은데....
            widget.setPendingIntentTemplate(R.id.widget_listview, toastPendingIntent);              //listView의 collection에 하나하나 pendingIntent를 부여하는 것은 메모리 부담이 많이 됨. 그래서 콜렉션에 하나의 펜딩인텐트만 부여한다.

            //보내기
            appWidgetManager.updateAppWidget(appWidgetIds, widget);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(TOAST_ACTION)) {

            Intent i = new Intent(context, MainBottomActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}
