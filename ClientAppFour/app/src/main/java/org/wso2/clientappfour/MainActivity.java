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
package org.wso2.clientappfour;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * This class get the data and the stream definitions from the Edge Analytics Service and use
 * those data to do the analytics
 * based on the queries
 */
public class MainActivity extends ActionBarActivity {

    private Context mContext=null;
    private EdgeAnalyticsManager mEdgeAnalyticsManager;
    int mCallback=0;
    int mFlag=0,registerBClicked=0,startServiceBClicked=0;
    Camera mCamera;
    Camera.Parameters mP;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEdgeAnalyticsManager = new EdgeAnalyticsManager(this);
        mContext=getApplicationContext();
        mCamera = Camera.open();
        mP = mCamera.getParameters();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Pass values to the wrapper object-EdgeAnalyticsManager object to register to the Service
     */
    public void registerToService() throws RemoteException {
        mEdgeAnalyticsManager.defineAppName("org.wso2.edgeAnalaticsClientAppFour");
        mEdgeAnalyticsManager.defineClientType("INTENSITY_SERVICE");
        mEdgeAnalyticsManager.defineStreamDefinition(null);
        mEdgeAnalyticsManager.defineStreamName(null);
        mEdgeAnalyticsManager.defineQuery("from every a1 = lightIntensityStream[lightValue > 15] -> a2 = lightIntensityStream[lightValue <15 ] select 'on'  as action insert into LightActionCallback;");
        mEdgeAnalyticsManager.defineQuery("from every a3 = lightIntensityStream[lightValue < 15] -> a4 = lightIntensityStream[lightValue >25 ] select 'off' as action insert into LightActionCallback;");
        mEdgeAnalyticsManager.defineCallback("LightValueLowHandleCallback12");
        mEdgeAnalyticsManager.defineCallback("LightValueLowHandleCallback22");
        mEdgeAnalyticsManager.getEdgeAnalyticsService();
    }

    /**
     * call the registerToService through the button click and call send data method
     */
    public void register(View view) throws RemoteException, InterruptedException {
        registerBClicked=1;
        Button mBregister = (Button) findViewById(R.id.button_register);
        registerToService();
        mBregister.setEnabled(false);
    }

    /**
     * do user prefer activities after get the callback from the service.Here it is vibrating the device
     * @param x -String - can catch the String values pass from the service
     */
    void handleCallback(String  x) {

        if (x.equalsIgnoreCase("callbackLightValueLowHandleCallback12")) {
            
            if (mCallback == 0) {
                Log.d("ClientAppFour", "Going to on the light!!");
                mP.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(mP);
                mCamera.startPreview();
                mCallback = 1;
            }

        } else if (x.equalsIgnoreCase("callbackLightValueLowHandleCallback22")) {

            if (mCallback == 1) {
                Log.d("ClientAppFour", "Going to off the light!!");
                mP.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mP);
                mCamera.stopPreview();
                mCallback = 0;
            }
        }
    }

    /** start the start service */
    public void startService(View view)
    {
        startServiceBClicked=1;
        Button mBstart = (Button) findViewById(R.id.button_start);
        mEdgeAnalyticsManager.connecttheService(mContext);
        mBstart.setEnabled(false);
        mFlag=0;
    }

    /** unbind from the service
     * and stop the sensor
     */
    public void stopService(View view) throws RemoteException {
        Log.d("ClientAppFour", "Service  stop button called!!!");
        Toast.makeText(mContext, "You have Stop the service", Toast.LENGTH_LONG).show();
        try {
            //mEdgeAnalyticsManager.stopService(mContext);
            mEdgeAnalyticsManager.stopService(getApplicationContext());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mFlag = 1;
    }

    /**
     * when the user in the onDestroy stage stop the sensor.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(mFlag==0)
        {
            try {
                mEdgeAnalyticsManager.stopService(mContext);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else {
            mFlag = 0;
           }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}
