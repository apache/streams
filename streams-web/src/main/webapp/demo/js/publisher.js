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
var publisher = publisher || (function(){

    //registers a new publisher, and returns the publisher's unique URL that you can use to POST activities
    var registerPublisher = function(){

        var registrationUrl;
        console.log($("#publisherRegistrationJSON").val());
        $.ajax({
            data: $("#publisherRegistrationJSON").val(),
            contentType:'application/json',
            type:'POST',
            url:'/streams-web/apps/publisher/register',
            success:function(data){
                console.log(data);
                console.log( $("#publisherRegistrationJSON").html());
                registrationUrl = data;
                $("#registrationUrl").val(registrationUrl);
            }
        });
    };

    //uses the publisher's unique URL to POST activities
    var postActivity = function(){
        var registrationUrl;
        $.ajax({
            data: $("#publisherActivityJSON").val(),
            url: $("#registrationUrl").val(),
            contentType:'application/json',
            type:'POST',
            success:function(data){
                console.log(data);
                $("#successMessage").html("Success! Activity Posted");
            }
        })
    };

    return{
        registerPublisher: registerPublisher,
        postActivity: postActivity
    };


})();