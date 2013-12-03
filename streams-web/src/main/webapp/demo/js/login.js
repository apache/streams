var Login = Login || (function(){

    var appPath = "/streams-web/app";
    var demoPath = "/streams-web/demo";

    var loginPressed = function(){
        var username = $("#username").val();
        var registrationObject = {"username":username};

        $.ajax({
            url: appPath + "/subscriberRegister",
            contentType: 'application/json',
            type:"POST",
            data:JSON.stringify(registrationObject),
            success:function(data){
                var segments = data.split("/");
                var subscriberId = segments[segments.length - 1];
                $.cookie('subscriberId', subscriberId);
                window.location.href = demoPath + "/activityDemo.html";
            }
        })
    };

    // Submits form if Enter key is pressed
    var searchKeyPress = function(e){
        if(typeof e == 'undefined' && window.event){
            e = window.event;
        }
        if(e.keyCode == 13){
            e.preventDefault();
            loginPressed();
        }
    };

    return {
        searchKeyPress : searchKeyPress,
        loginPressed : loginPressed
    };

})();