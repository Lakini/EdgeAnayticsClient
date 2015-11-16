/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.clientappthree;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

/**
 * This class reads the Intensity readings using the inbuilt Environment Sensor
 * of android device
 */
public class LightIntensitySensorService implements SensorEventListener {

    private Context mContext;
    private float mLightIntensitiveValue;
    private SensorManager mSensorManager;
    private Sensor mIntensity=null;
    private MainActivity mainActivity;

    /**
     * Start the TYPE_LIGHT sensor by passing context
     * @param mContext
     * @param mainActivity
     */
    void startLightIntensitySensorService(Context mContext,MainActivity mainActivity) {
        this.mContext=mContext;
        this.mainActivity=mainActivity;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mIntensity = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if(mIntensity==null)
        {
            Toast.makeText(mContext, "No In built Light Sensor in your device!!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mSensorManager.registerListener(this, mIntensity, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo.Assumed as the accuracy is not changing here.
    }

    /**
     * update the mLightIntensitiveValue variable when there is a sensor value change.
     */
    @Override
    public void onSensorChanged(final SensorEvent event) {
        mLightIntensitiveValue = event.values[0];
        Log.d("edge:intensity", String.valueOf(mLightIntensitiveValue));
    }

    /**
     * Stop the sensor by unregister the Listner
     */
    void stopLightIntensitySensorService()
    {
        mSensorManager.unregisterListener(this);
        mLightIntensitiveValue=-99;
    }

    /**
     * returns the mLightIntensitiveValue's values which was updated by onSensorChanged method
     * @return mLightIntensitiveValue:float value
     */
    public float getSensordata()
    {
        return mLightIntensitiveValue;
    }
}

