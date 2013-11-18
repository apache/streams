var publisher = publisher || (function(){

    // Registers a new publisher, and returns the publisher's unique URL that you can use to POST activities
    var registerPublisher = function(){
        //clear success message on refresh
        $("#successMessage").empty();

        var registrationUrl;
        $.ajax({
            data: $("#publisherRegistrationJSON").val(),
            contentType:'application/json',
            type:'POST',
            url:'/streams-web/app/publisherRegister',
            success:function(data){
                registrationUrl = data;
                $("#registrationUrl").val(registrationUrl);
            }
        });
    };

    // Uses the publisher's unique URL to POST activities
    var postActivity = function(){
        var registrationUrl;
        $.ajax({
            data: $("#publisherActivityJSON").val(),
            url: $("#registrationUrl").val(),
            contentType:'application/json',
            type:'POST',
            success:function(){
                $("#successMessage").html("Success! Activity Posted");
            }
        })
    };

    return{
        registerPublisher: registerPublisher,
        postActivity: postActivity
    };


})();