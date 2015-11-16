
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
package  org.wso2.clientappthree;

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

import java.util.Timer;
import java.util.TimerTask;

/**
 * This class reads the Intensity readings from the build in sensors and inject
 * to the EdgeAnalytics Service for processing on a query passed.
 * This don't use the stream definition and the data from th Edge Analytics Service
 */
public class MainActivity extends ActionBarActivity {

    private Context mContext=null;
    private EdgeAnalyticsManager mEdgeAnalyticsManager;
    private LightIntensitySensorService mSensorService=new LightIntensitySensorService();
    int mCallback=0;
    int mFlag=0,registerBClicked=0,startServiceBClicked=0;
    Camera mCamera;
    Camera.Parameters mCameraParameters;
    private String mIntensityValue;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEdgeAnalyticsManager = new EdgeAnalyticsManager(this);
        mContext=getApplicationContext();
        mCamera = Camera.open();
        mCameraParameters = mCamera.getParameters();
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
        mEdgeAnalyticsManager.defineAppName("org.wso2.edgeAnalaticsClientAppThree");
        mEdgeAnalyticsManager.defineClientType("TYPE1");
        mEdgeAnalyticsManager.defineStreamDefinition("define stream intensityStream (luxValue double); ");
        mEdgeAnalyticsManager.defineStreamName("intensityStream");
        mEdgeAnalyticsManager.defineQuery("from every a1 = intensityStream[luxValue > 15] -> a2 = intensityStream[luxValue <15 ] select a2.luxValue as lightVal insert into LightValueLowHandleCallback3;");
        mEdgeAnalyticsManager.defineQuery("from every a3 = intensityStream[luxValue < 15] -> a4 = intensityStream[luxValue >25 ] select a4.luxValue as lightVal insert into LightValueHighHandleCallback4;");
        mEdgeAnalyticsManager.defineCallback("LightValueLowHandleCallback3");
        mEdgeAnalyticsManager.defineCallback("LightValueHighHandleCallback4");
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
        mSensorService.startLightIntensitySensorService(mContext, this);
        sendData();
    }

    /**
     * do user prefer activities after get the callback from the service.Here it is vibrating the device
     * @param x -String - can catch the String vallues pass from the service
     */
    void handleCallback(String  x) {
        if (x.equalsIgnoreCase("callbackLightValueLowHandleCallback3")) {

            if (mCallback == 0) {
                Log.d("ClientAppFour", "Going to on the light!!");
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(mCameraParameters);
                mCamera.startPreview();
                mCallback = 1;
            }

        } else if (x.equalsIgnoreCase("callbackLightValueHighHandleCallback4")) {

            if (mCallback == 1) {
                Log.d("ClientAppFour", "Going to on the light!!");
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mCameraParameters);
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
        Toast.makeText(mContext, "You have Stop the service", Toast.LENGTH_LONG).show();
        Button mBstop = (Button) findViewById(R.id.button_stop);

        try {
            mBstop.setEnabled(false);
            mSensorService.stopLightIntensitySensorService();
            mEdgeAnalyticsManager.stopService(mContext);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mFlag = 1;

        //switch off the light if still it on before stop the service
        if (mCallback == 1) {
            Log.d("ClientAppFour", "Going to on the light!!");
            mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mCameraParameters);
            mCamera.stopPreview();
            mCallback = 0;
        }
    }

    /**
     * Send data to the Service to do the analytics.
     * In here I used a timer to periodically send data to the service.(in 3750 milisecond time interval)
     */
    public void sendData() throws RemoteException, InterruptedException {

        final double[] actualVal = new double[1];
        final Timer timer=new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                actualVal[0] =mSensorService.getSensordata();
                mIntensityValue = String.valueOf(actualVal[0]) + "-double";

                if(actualVal[0]==-99)
                {
                    timer.cancel();
                }

                else
                {
                    try {
                        mEdgeAnalyticsManager.passData(mIntensityValue);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 10, (long) 937.5);
    }

    /**
     * when the user in the onPause still send the data to the service to do the analytics.
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        if(registerBClicked==1 && startServiceBClicked==1) {

            try {
                sendData();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * when the user in the onStop stage stop the sensor.
     */
    @Override
    protected void onStop()
    {
        super.onStop();

        if(registerBClicked==1 && startServiceBClicked==1) {
            try {
                sendData();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
                mSensorService.stopLightIntensitySensorService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else {
            mFlag = 0;
        }
    }

    /**
     * Setting to work after the backbuton clicked.
     */
    @Override
    public void onBackPressed() {
        try {
            sendData();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
