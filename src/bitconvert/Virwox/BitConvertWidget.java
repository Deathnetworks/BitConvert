package bitconvert.Virwox;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

public class BitConvertWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) 
	{
		
		ComponentName BitConvertWidget;
		BitConvertWidget = new ComponentName( context, BitConvertWidget.class );
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);
			String rate = Main.widgetrate();
			views.setTextViewText(R.id.TextView01, rate);
			appWidgetManager.updateAppWidget(BitConvertWidget, views);
				
	}
	
}
