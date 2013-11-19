<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
    <link rel="stylesheet" type="text/css" href="demo/css/helpStyles.css" />
    <!-- highlight.js for Code example snippets-->
    <link rel="stylesheet" href="http://yandex.st/highlightjs/7.4/styles/default.min.css">
    <script src="http://yandex.st/highlightjs/7.4/highlight.min.js"></script>

    <!-- Bootstrap -->
    <link href="demo/css/bootstrap.min.css" rel="stylesheet" media="screen">
    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="demo/js/html5shiv.js"></script>
    <script src="demo/js/respond.min.js"></script>
    <![endif]-->

    <title>Activity Streams Demo</title>
</head>
<body>
    <div id="wrap" style="background-color: #fff">
        <div class="container">
            <div class="page-header">
                <h1><b>Activity Streams Help Page</b></h1>
            </div>
            <h3>What is Activity Streams?</h3>
            <p><a href="http://streams.incubator.apache.org/">Activity Streams</a> is a web service that allows you to publish Activities and subscribe to Publishers.</p><br>
            <p><b>There are six main entry points into the application, via the Controller:</b><br>*All example links include localhost:8080 and project name, "streams-web". These will need to be changed to correspond with your local setup.</p>
            <p>publisherRegister (POST):<br>Registers a new Publisher, and returns the new Publisher's unique token URL. This URL will be used to POST Activities by that Publisher.<br>(Example URL: <i>http://localhost:8080/streams-web/app/postActivity/134c555f-5047-49ec-80f3-a81749d32a0c</i>)</p>
            <ul>
                <li>localhost:8080/streams-web/app/publisherRegister</li>
            </ul>
            <p>subscriberRegister (POST):<br>Registers a new Subscriber, and returns the new Subscriber's unique token URL. This URL will be used to GET an Activity Stream, based on the Subscriber's filters.<br>Note: If you register a Subscriber that already exists in the database (by "username"), this call will return the Subscriber's already-existing unique URL token.<br>(Example URL: <i>http://localhost:8080/streams-web/app/getActivity/7348210e-088c-4fa1-829d-22d405bdb3df</i>)</p>
            <ul>
                <li>localhost:8080/streams-web/app/subscriberRegister</li>
            </ul>
            <p>postActivity (POST):<br>Posts a Publisher's Activity to the database.</p>
            <ul>
                <li>localhost:8080/streams-web/app/postActivity/<i>{publisher's unique token}</i></li>
            </ul>
            <p>getActivity (GET):<br>Returns a Subscriber's Activity Stream, which consists of all posts that correspond with the Subscriber's filter(s).</p>
            <ul>
                <li>localhost:8080/streams-web/app/getActivity/<i>{subscriber's unique token}</i></li>
            </ul>
            <p>updateFilters (POST):<br>Updates a Subscriber's filters (which determine what Activities a Subscriber is subscribed to) by adding and/or removing filters.</p>
            <ul>
                <li>localhost:8080/streams-web/app/updateFilters/<i>{subscriber's unique token}</i></li>
            </ul>
            <p>getFilters (GET):<br>Returns a Subscriber's list of filters they are subscribed to, in the form of an array.</p>
            <ul>
                <li>localhost:8080/streams-web/app/getFilters/<i>{subscriber's unique token}</i></li>
            </ul>
            <p>These six entry points can be demo'd using the Web Interface below.</p><br>
            <h3>Web Interface</h3>
            <p>The Web Interface demos the following...</p>
            <ul>
                <li>How Streams outputs Publisher/Subscriber registration URLs</li>
                <li>JSON input for registering a new Publisher/Subscriber</li>
                <li>JSON input for POSTing an Activity</li>
                <li>JSON output for a Subscriber's Activity Stream</li>
                <li>JSON output of a Subscriber's filters</li>
                <li>How to update a Subscriber's filters</li>
                <li>Styled Activity Streams for a particular Subscriber</li>
            </ul><br>
            <h3>How to access the Web Interface</h3>
            <p>Run Streams locally, and navigate to the following URLs...</p><br>
            <p>To demo registering a Publisher and POSTing an Activity:</p>
            <ul>
                <li>localhost:8080/streams-web/demo/publisher.html</li>
            </ul>
            <p>To demo registering a Subscriber and view raw JSON output of the Subscriber's Activity Stream and filters:
            <ul>
                <li>localhost:8080/streams-web/demo/subscriber.html</li>
            </ul>
            <p>To log in as a particular Subscriber, view their stylized Activity Stream, and update filters (this demo is more user-centric):</p>
            <ul>
                <li>localhost:8080/streams-web/demo/activityDemo.html</li>
            </ul>
            <p>Sample JSON input can be found on the <a href="http://streams.incubator.apache.org/streams-usage.html">Activity Streams Usage page</a></p><br>

            <h3>Sample JSON input</h3>
            <p><b>Register a Publisher</b></p>
            <pre>
                <code>
                    {"src":"www.example.com"}

                </code>
            </pre>
            <p><b>Post an Activity</b></b><br>(filters can be based on the following keys: actor.displayName, object.displayName, target.displayName, verb, provider.displayName)</p>
            <pre>
                <code>
                    {
                    "id": "id",
                    "verb": "verb",
                    "provider": {
                    "displayName":"name",
                    "url": "http.example.com:8888"
                    },
                    "actor": {
                    "id": "actorid",
                    "objectType": "actorobject",
                    "displayName": "actorname",
                    "url": "www.actorexampleurl.com"
                    },
                    "target": {
                    "id": "targetid",
                    "displayName": "targetname",
                    "url": "www.targeturl.com"
                    },
                    "object": {
                    "id": "objectid",
                    "displayName": "objectname",
                    "objectType": "object",
                    "url": "www.objecturl.org"
                    }
                    }

                </code>
            </pre>
            <p><b>Register a Subscriber</b><br>If a Subscriber is registered and already exists in the database, this call will return the Subscriber's already-existing unique URL token.</p>
            <pre>
                <code>
                    {"username":"janedoe"}
                </code>
            </pre>
            <p><b>Update a Subscriber's filter(s)</b><br>Both the "add" and "remove" keys must be included. Just use an empty array if either are not needed.</p>
            <pre>
                <code>
                    {"add":["tag1","tag2"], "remove":["tag3"]}
                </code>
            </pre>
        </div>
    </div>
    <div id="footer">
        <div class="container">
            <p class="text-muted credit">
            <a href="http://streams.incubator.apache.org/">Activity Streams Home site</a>
            </p>
        </div>
    </div>

<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="js/bootstrap.min.js"></script>
</body>
</html>
