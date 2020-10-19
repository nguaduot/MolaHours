package com.nguaduot.molathird.hours.fragment;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.CheckBoxPreference;

import java.util.List;
import java.util.Locale;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.nguaduot.molathird.hours.MainService;
import com.nguaduot.molathird.hours.R;

public class PreferencesFragment extends PreferenceFragment {
    private CheckBoxPreference cbpService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        cbpService.setChecked(isAccessibilityServiceEnabled());
    }

    private void init() {
        cbpService = (CheckBoxPreference) findPreference("service");
        cbpService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                gotoSettingsAccessibility();
                return true;
            }
        });

        findPreference("app").setSummary(getVersionInfo());
    }

    private void gotoSettingsAccessibility() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(getContext().getPackageName())
                    && enabledServiceInfo.name.equals(MainService.class.getName()))
                return true;
        }

        return false;
    }

    private String getVersionInfo() {
        try {
            PackageInfo pInfo = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            return String.format(Locale.US, "v%s(%d)", pInfo.versionName, pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "null";
    }
}
