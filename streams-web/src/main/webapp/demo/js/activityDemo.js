var activityDemo = activityDemo || (function(){
    var activityStream = "";
    var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ];
    var appPath = "/streams-web/app";
    //TODO: replace this with SSO
    var subscriberId = subscriberId || $.cookie("subscriberId");
    var addFilters = [];
    var removeFilters = [];

    // Submits form if Enter key is pressed
    var searchKeyPress = function(e){
        if(typeof e == 'undefined' && window.event){
            e = window.event;
        }
        if(e.keyCode == 13){
            e.preventDefault();
            document.getElementById('submitButton').click();
        }
    };

    var newFilters = function(){
        var filtersString = $("#addFilters").val();
        var splitAddFilters = filtersString.split(",");
        $.each(splitAddFilters, function(key, value){
            var trimmedFilter = value.trim();
            if(!$.inArray(trimmedFilter, addFilters)){
                addFilters.push(trimmedFilter);
            }
            if($.inArray(trimmedFilter, removeFilters)){
                removeFilters.pop(trimmedFilter);
            }
        });
    };

    var dropFilter = function(filter){
        if(!$.inArray(filter, addFilters)){
            addFilters.pop(filter);
        }
        if($.inArray(filter, removeFilters)){
            removeFilters.push(filter);
        }
    };

    // Sets the subscriber's filters based on input from the user.
    var setFilters = function(){

        //Obtain filter terms from user, split by space, and add to the filterobject
        var addFiltersArray = [];
        var addFilters = $("#addFilters").val();
        // filters are separated by space (continuum)
        var splitAddFilters = addFilters.split(" ");
        for(var i = 0; i < splitAddFilters.length; i++){
            addFiltersArray.push(splitAddFilters[i]);
        }

        var removeFiltersArray = [];
        var removeFilters = $("#removeFilters").val();
        // filters are separated by space (continuum)
        var splitRemoveFilters = removeFilters.split(" ");
        for(var j = 0; j < splitRemoveFilters.length; j++){
            removeFiltersArray.push(splitRemoveFilters[j]);
        }

        var filterObject = {"add":addFiltersArray, "remove":removeFiltersArray};
        $.ajax({
            contentType:"application/json",
            type:"POST",
            url: appPath + "/updateFilters/" + subscriberId,
            data:JSON.stringify(filterObject),
            success:function(data){
                getActivities();
            }
        });
    };


    // Gets activities streams array and sends to setTemplate function to display.
    var getActivities = function(){
        $.ajax({
            type:"GET",
            url: appPath + "/getActivity/" + subscriberId,
            success:function(data){
                setTemplate(data);
            }
        });
    };


    // Refreshes every 3 seconds to obtain the most recent Activity Stream
    setInterval(function(){
            if(!subscriberId && console){
                //console.log("Please enter a subscriber url first");
            }else{
                getActivities();
            }
    },3000);


    // Applies the array returned from Streams to the html Handlebars template to be displayed
    var setTemplate = function(activityStreamData){
        var source   = $("#activity-template").html();
        var template = Handlebars.compile(source);
        var html = template(activityStreamData);
        $("#activityStream").html(html);
    };


    // Format publish date
    Handlebars.registerHelper("formatDate", function(timestamp) {
        var date = new Date(timestamp);
        return months[date.getMonth()]  + " " + date.getDate() + " " +  date.getFullYear() + " " + date.getHours() + ":" + (date.getMinutes()<10?'0':'') + date.getMinutes()
    });


    return {
        searchKeyPress: searchKeyPress,
        getActivities: getActivities,
        setFilters: setFilters,
        newFilters: newFilters,
        dropFilter: dropFilter
        }

})();
