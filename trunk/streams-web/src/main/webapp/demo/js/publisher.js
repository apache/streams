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