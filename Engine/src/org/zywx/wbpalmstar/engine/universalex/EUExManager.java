/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.engine.universalex;

import android.content.Context;
import android.os.Build;
import android.webkit.WebView;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.ELinkedList;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

public class EUExManager {

	private Context mContext;
	private ELinkedList<EUExBase> mThirdPlugins;

	public EUExManager(Context context) {
		mContext = context;
		mThirdPlugins = new ELinkedList<EUExBase>();
	}

	public void addJavascriptInterface(EBrowserView brwView) {
		EUExWidgetOne widgetOne = new EUExWidgetOne(mContext, brwView);
		widgetOne.setUexName(EUExWidgetOne.tag);
		EUExWindow window = new EUExWindow(mContext, brwView);
		window.setUexName(EUExWindow.tag);
		EUExWidget widget = new EUExWidget(mContext, brwView);
		widget.setUexName(EUExWidget.tag);
		EUExAppCenter appCenter = new EUExAppCenter(mContext, brwView);
		appCenter.setUexName(EUExAppCenter.tag);
//		EUExDataAnalysis dataAnalysis = new EUExDataAnalysis(mContext, brwView);
//		dataAnalysis.setUexName(EUExDataAnalysis.tag);
		brwView.addJavascriptInterface(widgetOne, EUExWidgetOne.tag);
		brwView.addJavascriptInterface(window, EUExWindow.tag);
		brwView.addJavascriptInterface(widget, EUExWidget.tag);
		brwView.addJavascriptInterface(appCenter, EUExAppCenter.tag);
//		brwView.addJavascriptInterface(dataAnalysis, EUExDataAnalysis.tag);
		mThirdPlugins.add(widgetOne);
		mThirdPlugins.add(window);
		mThirdPlugins.add(widget);
		mThirdPlugins.add(appCenter);
//		mThirdPlugins.add(dataAnalysis);
		// third-party plugin
		WidgetOneApplication app = (WidgetOneApplication)mContext.getApplicationContext();
		ThirdPluginMgr tpm = app.getThirdPlugins();
		Map<String, ThirdPluginObject> thirdPlugins = tpm.getPlugins();
		String symbol = "_";
		Set<Map.Entry<String, ThirdPluginObject>> pluginSet = thirdPlugins.entrySet();
		for (Map.Entry<String, ThirdPluginObject> entry : pluginSet) {
			String uName = entry.getKey();
			ThirdPluginObject scriptObj = entry.getValue();
			EUExBase objectIntance = null;
			try {
				Constructor<?> init = scriptObj.jobject;
				objectIntance = (EUExBase)init.newInstance(mContext, brwView);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (null != objectIntance) {
				String uexName = uName + symbol;
				objectIntance.setUexName(uexName);
				brwView.addJavascriptInterface(objectIntance, uexName);
				mThirdPlugins.add(objectIntance);
			}
		}
	}
	
	public void notifyReset() {
		for (EUExBase uex : mThirdPlugins) {
			uex.reset();
		}
	}

	public void notifyDocChange() {
		for (EUExBase uex : mThirdPlugins) {
			uex.clean();
		}
	}
	
	public void notifyStop() {
		notifyDocChange();
		for (EUExBase uex : mThirdPlugins) {
			uex.stop();
		}
	}

	public void notifyDestroy(WebView view) {
		notifyDocChange();
		for(EUExBase uex : mThirdPlugins){
			if(Build.VERSION.SDK_INT >= 11){
				String uexName = uex.getUexName();
				view.removeJavascriptInterface(uexName);
			}
			uex.destroy();
		}
		mThirdPlugins.clear();
		mThirdPlugins = null;
		mContext = null;
	}
}
