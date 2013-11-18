var subscriber = subscriber || (function(){

    var localPath = "http://";
    var subscriberId;

    // Registers a new subscriber, and returns the subscriber's unique ID
    var registerSubscriber = function(){
        //clear success messages on refresh
        $("#successAdd").empty();
        $("#successRemove").empty();

        var registrationUrl = "";
        $.ajax({
            data:$("#subscriberRegistrationJSON").val(),
            contentType:"application/json",
            type:"POST",
            url:"/streams-web/app/subscriberRegister",
            success:function(data){
                registrationUrl = data;
                $("#registrationUrl").val(registrationUrl);

                //extract path and subscriber id for later use
                var segments = registrationUrl.split("/");
                for(var i = 2; i <= 4; i++){
                    localPath += (segments[i] + "/");
                }
                subscriberId = segments[segments.length - 1];
            }
        });

        //TODO: Think about how to handle exception gracefully if registrationUrl is never obtained.
    };

    // Adds the filters specified by the user to the subscriber's account.
    // Adding and Removing filters can be done simultaneously, but we are declaring them separately
    // for the purpose of this demonstration.
    var addFilters = function(){
        $.ajax({
            data:$("#addFilters").val(),
            contentType:"application/json",
            type:"POST",
            url: localPath + "updateFilters/" + subscriberId,
            success:function(){
                $("#successAdd").html("Filter(s) added succssfully!");
            }
        });
    };

    // Removes the filters specified by the user from the subscriber's account.
    // Adding and Removing filters can be done simultaneously, but we are declaring them separately
    // for the purpose of this demonstration.
    var removeFilters = function(){
        $.ajax({
            data:$("#removeFilters").val(),
            contentType:"application/json",
            type:"POST",
            url: localPath + "updateFilters/" + subscriberId,
            success:function(){
                $("#successRemove").html("Filter(s) removed succssfully!");
            }
        });
    };

    // Returns the assigned filters from a subscriber's account
    var getFilters = function(){
        $.ajax({
            data:$("#removeFilters").val(),
            contentType:"application/json",
            type:"GET",
            url: localPath + "getFilters/" + subscriberId,
            success:function(data){
                $("#getFilters").val(JSON.stringify(data));
            }
        });
    };

    // Returns activity streams (JSON) that have the same tag specified in the subscriber's filters
    var getActivities = function(){
        var registrationUrl = $("#registrationUrl").val();

        $.ajax({
            type:"GET",
            url: localPath + "getActivity/" + subscriberId,
            success:function(data){
                $("#getActivities").val(JSON.stringify(data));
            }
        })
    };

    return{
        registerSubscriber: registerSubscriber,
        addFilters: addFilters,
        removeFilters: removeFilters,
        getFilters: getFilters,
        getActivities: getActivities
    };

})();