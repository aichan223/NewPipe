package org.schabi.newpipe.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.R;

import java.io.File;
import java.util.Set;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/*
 * Created by k3b on 07.01.2016.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * NewPipeSettings.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Helper class for global settings.
 */
public final class NewPipeSettings {
    private NewPipeSettings() { }

    public static void initSettings(final Context context) {
        // check if there are entries in the prefs to determine whether this is the first app run
        Boolean isFirstRun = null;
        final Set<String> prefsKeys = PreferenceManager.getDefaultSharedPreferences(context)
                .getAll().keySet();
        for (final String key: prefsKeys) {
            // ACRA stores some info in the prefs during app initialization
            // which happens before this method is called. Therefore ignore ACRA-related keys.
            if (!key.toLowerCase().startsWith("acra")) {
                isFirstRun = false;
                break;
            }
        }
        if (isFirstRun == null) {
            isFirstRun = true;
        }

        PreferenceManager.setDefaultValues(context, R.xml.main_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.video_audio_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.download_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.appearance_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.history_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.content_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.notification_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.update_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.debug_settings, true);

        saveDefaultVideoDownloadDirectory(context);
        saveDefaultAudioDownloadDirectory(context);

        SettingMigrations.initMigrations(context, isFirstRun);
    }

    static void saveDefaultVideoDownloadDirectory(final Context context) {
        saveDefaultDirectory(context, R.string.download_path_video_key,
                Environment.DIRECTORY_MOVIES);
    }

    static void saveDefaultAudioDownloadDirectory(final Context context) {
        saveDefaultDirectory(context, R.string.download_path_audio_key,
                Environment.DIRECTORY_MUSIC);
    }

    private static void saveDefaultDirectory(final Context context, final int keyID,
                                             final String defaultDirectoryName) {
        if (!useStorageAccessFramework(context)) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final String key = context.getString(keyID);
            final String downloadPath = prefs.getString(key, null);
            if (!isNullOrEmpty(downloadPath)) {
                return;
            }

            final SharedPreferences.Editor spEditor = prefs.edit();
            spEditor.putString(key, getNewPipeChildFolderPathForDir(getDir(defaultDirectoryName)));
            spEditor.apply();
        }
    }

    @NonNull
    public static File getDir(final String defaultDirectoryName) {
        return new File(Environment.getExternalStorageDirectory(), defaultDirectoryName);
    }

    private static String getNewPipeChildFolderPathForDir(final File dir) {
        return new File(dir, "NewPipe").toURI().toString();
    }

    public static boolean useStorageAccessFramework(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        final String key = context.getString(R.string.storage_use_saf);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getBoolean(key, true);
    }
}
