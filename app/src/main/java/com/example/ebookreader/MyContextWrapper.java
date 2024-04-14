package com.example.ebookreader;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;

import java.util.Locale;

public class MyContextWrapper extends ContextWrapper {
    public MyContextWrapper(Context base) {
        super(base);
    }
    public static ContextWrapper wrap(Context context, Locale newLocale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(newLocale);
        return new MyContextWrapper(context.createConfigurationContext(configuration));
    }
}
