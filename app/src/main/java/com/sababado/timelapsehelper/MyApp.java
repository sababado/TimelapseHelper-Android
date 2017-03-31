package com.sababado.timelapsehelper;

import android.app.Application;
import android.content.Context;

import com.sababado.ezprovider.EasyProvider;
import com.sababado.timelapsehelper.models.TimeLapseItem;
import com.sababado.timelapsehelper.provider.DatabaseHelper;

/**
 * Created by robert on 3/31/17.
 */

public class MyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        EasyProvider.init(this, DatabaseHelper.class, TimeLapseItem.class);
    }
}
