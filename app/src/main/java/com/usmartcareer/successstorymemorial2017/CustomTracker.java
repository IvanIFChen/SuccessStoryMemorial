package com.usmartcareer.successstorymemorial2017;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class CustomTracker {
    private FirebaseAnalytics mFirebaseAnalytics;

    public void initialize(Context instance, Activity activity, String name) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(instance);
        mFirebaseAnalytics.setCurrentScreen(activity, name, null);
    }


    public void sendAnalyticAction(String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, msg);
        mFirebaseAnalytics.logEvent("Action", bundle);
    }

    public void sendAnalyticScreen(String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, msg);
        mFirebaseAnalytics.logEvent("View", bundle);
    }
}
