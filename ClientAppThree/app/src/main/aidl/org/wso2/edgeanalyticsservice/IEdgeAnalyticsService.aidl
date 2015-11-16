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
package org.wso2.edgeanalyticsservice;

import org.wso2.edgeanalyticsservice.IEdgeAnalyticServiceCallback;

interface IEdgeAnalyticsService {

    /** Give the service according to the type of the app and the queries.
    *Can pass many quries at a time.
    *@param type String
    *@param streamDefinition String
    *@param stream String
    *@param query List<String>
    *@param callbackFunction List<String>
    *@param cb IEdgeAnalyticServiceCallback
    */
    void getService(String type,String streamDefinition,String stream,in List<String> query,in List<String> callbackFunction,IEdgeAnalyticServiceCallback cb);

    /**
    Send data to the service as a String
    *@param value String
    *@param stream String
    */
    void sendData(String value,String stream);

    /**
    Use to stop the service to the requested client
    */
    void stopService();
}
