var subscriber = subscriber || (function(){

    //registers a new subscriber, and returns the subscriber's unique URL
    var registerSubscriber = function(){

        var registrationUrl;
        console.log($("").val());
        $.ajax({
            data:$("#subscriberRegistrationJSON").val(),
            contentType:"application/json",
            type:"POST",
            url:"/streams-web/app/subscriberRegister",
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
