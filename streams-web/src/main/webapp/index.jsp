<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"></script>

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
<div class="col-sm-6">
    <h1>Help Page</h1>
    <p>The following URLs can be used to demo Activity Streams when running locally. They are also useful for debugging.</p>
    <div class="panel panel-info">
        <div class="panel-heading">
            <h3 class="panel-title"><b>Demo Publisher:</b> To see how a Publisher is registered and how an activity is POSTed</h3>
        </div>
        <div class="panel-body">localhost:8080/streams-web/demo/publisher.html</div>
    </div>
    <div class="panel panel-info">
        <div class="panel-heading">
            <h3 class="panel-title"><b>Demo Subscriber:</b> To see how a Subscriber is registered and what acitivities look like under the covers</h3>
        </div>
        <div class="panel-body">localhost:8080/streams-web/demo/subscriber.html</div>
    </div>
    <div class="panel panel-info">
        <div class="panel-heading">
            <h3 class="panel-title"><b>Demo Activity Stream by Filter:</b> To filter activity streams by a particular tag and to view a sample stream output</h3>
        </div>
        <div class="panel-body">localhost:8080/streams-web/demo/activityDemo.html</div>
    </div>
</div>

<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="js/bootstrap.min.js"></script>
</body>
</html>