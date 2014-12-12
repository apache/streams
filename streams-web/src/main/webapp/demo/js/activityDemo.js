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
var activityDemo = activityDemo || (function(){
    var subscriberURL = "";
    var registerUrl = "/streams-web/apps/subscriber/register";
    var activityStream = "";

    var getActivitiesForGivenTag = function(){

        registerSubscriber();
    };

    // Registers a new subscriber and sets the subscriber's filter(s)
    // based on tag entered by the user on the demo webpage
    var registerSubscriber = function(){
        var tag = $("#tag").val();
        console.log("tag = " + tag);
        var registrationObject =
        {
            "authToken": "token",
            "@class":"org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriptionImpl",
            "filters": [
                tag
            ]
        };

        $.ajax({
            url:registerUrl,
            contentType: 'application/json',
            type:"POST",
            data:JSON.stringify(registrationObject),
            success:function(data){
                console.log(data);
                subscriberURL = data;
                getActivitiesStream();
            }
        })
    };

    // Gets activities streams array
    var getActivitiesStream = function(){
        if(!subscriberURL){
            alert("Please enter a subscriber url first");
        }

        $.ajax({
            contentType: "application/json",
            type:"GET",
            url: subscriberURL,
            success:function(data){
                setTemplate(data);
            }
        })
    };

    // Applies the array returned from Streams to the html template to be displayed
    var setTemplate = function(activityStreamData){
        var source   = $("#activity-template").html();
        var template = Handlebars.compile(source);
        var html = template(activityStreamData);
        $("#activityStream").html(html);
    };

    return {
            getActivitiesForGivenTag: getActivitiesForGivenTag
        }

})();
