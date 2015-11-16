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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.wso2.edgeanalyticsservice.IEdgeAnalyticServiceCallback;
import org.wso2.edgeanalyticsservice.IEdgeAnalyticsService;

import java.util.ArrayList;
import java.util.List;

public class EdgeAnalyticsManager {

    private Intent mIntent;
    private IEdgeAnalyticsService mRemoteService;
    private RemoteServiceConnection mRemoteServiceConnection = new RemoteServiceConnection();
    private String mName, mType,mStream, mStreamName;
    private List<String> mQuery = new ArrayList<>();
    private List<String> mCallb= new ArrayList<>();
    private MainActivity mainActivity;

    public EdgeAnalyticsManager(MainActivity mainActivity)
    {
        this.mainActivity=mainActivity;
    }

    /**
     * Connect to the service by making a separate intent
     * and initialize the vibrator variable.
     */
    void connecttheService(Context context) {
        mIntent = new Intent();
        mIntent.setClassName("org.wso2.edgeanalyticsservice", "org.wso2.edgeanalyticsservice.EdgeAnalyticsService");
        context.startService(mIntent);
        context.bindService(mIntent, mRemoteServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * unbind from the service by calling unbindService method.
     */
    void stopService(Context context) throws RemoteException {

        if(!mType.equalsIgnoreCase("TYPE1"))
            mRemoteService.stopService();

        context.unbindService(mRemoteServiceConnection);
    }

    /**
     * Create ServiceConnection object by implementing ServiceConnection interface
     * which is used by the bindService method.
     */
    class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,
                                       IBinder boundService) {
            mRemoteService = IEdgeAnalyticsService.Stub.asInterface((IBinder) boundService);
            Log.d(getClass().getSimpleName(), "onServiceConnected()");
        }

        public void onServiceDisconnected(ComponentName className) {
            mRemoteService = null;
            Log.d(getClass().getSimpleName(), "onServiceDisconnected");
        }
    }

    /**
     * Initialize the stream definition
     * @param define_stream :String
     */
    public void defineStreamDefinition(String define_stream) throws RemoteException {
        mStream=define_stream;
    }

    /**
     * Initialize the stream
     * @param stream_name :String
     */
    public void defineStreamName(String stream_name) throws RemoteException {
        mStreamName=stream_name;
    }

    /**
     * Initialize the Client type
     * @param client_type :String
     */
    public void defineClientType(String client_type) throws RemoteException {
        mType=client_type;
    }

    /**
     * Initialize the appName
     * @param appName :String
     */
    public void defineAppName(String appName) throws RemoteException {
        mName=appName;
    }

    /**
     * Initialize the queryone.If the user want to pass a pattern  as a query he/she has to use
     * defineQuery1 and defineQuery2.
     * In this client it hasn't use patterns.So only used deifneQuery1
     * @param queryDefinition :String
     */
    public void defineQuery(String queryDefinition) throws RemoteException {
        mQuery.add(queryDefinition);
    }

    /**
     * Initialize the callback method
     * @param callbackDef :String
     */
    public void defineCallback(String callbackDef) throws RemoteException {
        mCallb.add(callbackDef);
    }

    /**
     * Get service by passing queries to the EdgeAnalyticsService
     */
    public void getEdgeAnalyticsService() throws RemoteException {
        mRemoteService.getService(mType, mStream, mStreamName, mQuery, mCallb, callback);
    }

    /**
     * Send the values passed from the MainActivity/user to the service through the wrapper object
     * @param value:String
     */
    public void passData(String value) throws RemoteException, InterruptedException {
        mRemoteService.sendData(value, mStreamName);
    }

    /**
     * Implement the aidl service interface of IEdgeAnalyticsServiceCallback.aidl
     */
    private IEdgeAnalyticServiceCallback callback = new IEdgeAnalyticServiceCallback.Stub() {
        /**
         * Handles the callback and pass to the mainActivity
         */
        @Override
        public void addCallBack(String text) throws RemoteException {
            mainActivity.handleCallback(text);
        }
    };
}
