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
    <div id="wrap" style="background-color: #ffffff">
        <div class="container">
            <div class="page-header">
                <h1><b>Activity Streams Help Page</b></h1>
            </div>
            <h3>What is Activity Streams?</h3>
            <p><a href="http://streams.incubator.apache.org/">Activity Streams</a> is a web service that allows you to publish Activities and subscribe to Publishers.</p><br>
            <p><b>There are four main entry points into the application, via the Controller:</b><br>*All example links include localhost:8080 and project name, "streams-web". These will need to be changed to correspond with your local setup.</p>
            <p>publisherRegister (POST):<br>Registers a new Publisher, and returns the new Publisher's unique token URL. This URL will be used to POST Activities by that Publisher.<br>(Example URL: <i>http://localhost:8080/streams-web/app/postActivity/134c555f-5047-49ec-80f3-a81749d32a0c</i>)</p>
            <ul>
                <li>localhost:8080/streams-web/app/publisherRegister</li>
            </ul>
            <p>subscriberRegister (POST):<br>Registers a new Subscriber, and returns the new Subscriber's unique token URL. This URL will be used to GET an Activity Stream, based on the Subscriber's filter tag(s).<br>(Example URL: <i>http://localhost:8080/streams-web/app/getActivity/7348210e-088c-4fa1-829d-22d405bdb3df</i>)</p>
            <ul>
                <li>localhost:8080/streams-web/app/subscriberRegister</li>
            </ul>
            <p>postActivity (POST):<br>Posts a Publisher's Activity to the database.</p>
            <ul>
                <li>localhost:8080/streams-web/app/postActivity/<i>{publisher's unique token}</i></li>
            </ul>
            <p>getActivity (GET):<br>Returns a Subscriber's Activity Stream, which consists of all posts that have the same tag as the Subscriber's filter.</p>
            <ul>
                <li>localhost:8080/streams-web/app/getActivity/<i>{subscriber's unique token}</i></li>
            </ul>
            <p>These four entry points can be demo'd using the Web Interface below.</p><br>
            <h3>Web Interface</h3>
            <p>The Web Interface demos the following...</p>
            <ul>
                <li>How Streams outputs Publisher/Subscriber registration URLs</li>
                <li>JSON input for registering a new Publisher/Subscriber</li>
                <li>JSON input for POSTing an Activity</li>
                <li>JSON output for a Subscriber's Activity Stream</li>
                <li>Activity Streams filtered by tag</li>
            </ul><br>
            <h3>How to access the Web Interface</h3>
            <p>Run Streams locally, and navigate to the following URLs...</p><br>
            <p>To demo registering a Publisher and POSTing an Activity:</p>
            <ul>
                <li>localhost:8080/streams-web/demo/publisher.html</li>
            </ul>
            <p>To demo registering a Subscriber and view raw JSON output of the Subscriber's Activity Stream:
            <ul>
                <li>localhost:8080/streams-web/demo/subscriber.html</li>
            </ul>
            <p>To view ActivityStream for a Subscriber, filtered by a tag:</p>
            <ul>
                <li>localhost:8080/streams-web/demo/activityDemo.html</li>
            </ul>
            <p>Sample JSON input can be found on the <a href="http://streams.incubator.apache.org/streams-usage.html">Activity Streams Usage page</a></p><br>

            <h3>Sample JSON input</h3>
            <p>Register a Publisher</p>
            <pre>
                <code>
                    {"src":"www.example.com"}

                </code>
            </pre>
            <p>Post an Activity<br>(filters are controlled using the "displayName" field)</p>
            <pre>
                <code>
                    {
                    "id": "id",
                    "verb": "verb",
                    "provider": {
                    "displayName":"danny",
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
            <p>Register a Subscriber<br>To EDIT:(control the Activities a Subscriber sees by using the "filters" field. In this example, the Subscriber's Activity Stream will contain all Activities posted with the tag, "tags".</p>
            <pre>
                <code>
                    {"username":"chansen"}
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