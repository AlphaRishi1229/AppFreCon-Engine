package com.example.rishivijaygajelli.appconengine.rootutil.BackgroundAppCheck;

import android.content.Context;
import android.content.pm.PackageManager;

public interface Detector {
    String getForegroundApp(Context context) throws PackageManager.NameNotFoundException;

}
