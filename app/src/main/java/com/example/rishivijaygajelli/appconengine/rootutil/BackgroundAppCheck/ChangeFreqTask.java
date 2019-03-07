package com.example.rishivijaygajelli.appconengine.rootutil.BackgroundAppCheck;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util;

public class ChangeFreqTask extends AsyncTaskLoader<Void> {

    String current_max,current_min;
    public ChangeFreqTask(@NonNull Context context, String current_max, String current_min) {
        super(context);
        this.current_max = current_max;
        this.current_min = current_min;
    }

    @Nullable
    @Override
    public Void loadInBackground() {
        Util.setFreq(current_max,current_min);
        return null;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
