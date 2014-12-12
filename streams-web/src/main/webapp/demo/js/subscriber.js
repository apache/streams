/*
 ~ Licensed to the Apache Software Foundation (ASF) under one
 ~ or more contributor license agreements.  See the NOTICE file
 ~ distributed with this work for additional information
 ~ regarding copyright ownership.  The ASF licenses this file
 ~ to you under the Apache License, Version 2.0 (the
 ~ "License"); you may not use this file except in compliance
 ~
 ~   http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 */
var subscriber = subscriber || (function(){

    //registers a new subscriber, and returns the subscriber's unique URL
    var registerSubscriber = function(){

        var registrationUrl;
        console.log($("").val());
        $.ajax({
            data:$("#subscriberRegistrationJSON").val(),
            contentType:"application/json",
            type:"POST",
            url:"/streams-web/apps/subscriber/register",
            success:function(data){
                console.log(data);
                registrationUrl = data;
                $("#registrationUrl").val(registrationUrl);
            }
        });
    };

    //returns activity streams (JSON) that have the same tag specified in the subscriber's filters
    var getActivities = function(){
        var registrationUrl;
        $.ajax({
            contentType: "text/plain",
            type:"GET",
            url: $("#registrationUrl").val(),
            success:function(data){
                console.log(data);
                $("#successMessage").val(data);
            }
        })
    };

    return{
        registerSubscriber: registerSubscriber,
        getActivities: getActivities
    };

})();