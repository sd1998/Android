/**
 *   ownCloud Android client application
 *
 *   @author Shashvat Kedia
 *   Copyright (C) 2018 ownCloud GmbH.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;

import com.owncloud.android.MainApp;
import com.owncloud.android.db.PreferenceManager;
import com.owncloud.android.db.UploadResult;
import com.owncloud.android.files.services.TransferRequester;
import com.owncloud.android.lib.common.utils.Log_OC;

public class PowerConnectionReceiver extends BroadcastReceiver {
    private final String TAG = PowerConnectionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent){
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        boolean isCharging = ( status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL );
        if(isCharging){
            chargerConnected(context);
        } else{
            Log_OC.d(TAG,"Charger disconnected");
        }
    }

    private void chargerConnected(Context context){
        if( (PreferenceManager.cameraPictureUploadEnabled(context) &&
                PreferenceManager.cameraVideoUploadWhileChargingOnly(context))
                || (PreferenceManager.cameraVideoUploadEnabled(context) &&
                PreferenceManager.cameraVideoUploadWhileChargingOnly(context)) ){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log_OC.d(TAG,"Requesting retry of camera uploads (& friends)");
                    TransferRequester requester = new TransferRequester();
    /*                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        requester.retryFailedUploads(
                                MainApp.getAppContext(),
                                null,
                                UploadResult.INTERRUPTED_WHEN_CHARGER_DISCONNECTED,
                                true
                        );
                    }*/
                    requester.retryFailedUploads(
                            MainApp.getAppContext(),
                            null,
                            UploadResult.DELAYED_FOR_NOT_CHARGING,
                            true
                    );
                }
            },500);
        }
    }
}
