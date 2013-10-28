var activityDemo = activityDemo || (function(){
    var subscriberURL = "";
    var registerUrl = "/streams-web/app/subscriberRegister";
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
            "@class":"org.apache.streams.persistence.model.cassandra.CassandraSubscription",
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
