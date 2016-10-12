CREATE TABLE `objectTypes_place`
(
`id` STRING,
`image` STRUCT
<
`duration`:FLOAT,
`height`:INT,
`width`:INT,
`url`:STRING
>
,
`displayName` STRING,
`summary` STRING,
`content` STRING,
`url` STRING,
`objectType` STRING,
`author` STRUCT
<
`id`:STRING,
`image`:STRUCT
<
`duration`:FLOAT,
`height`:INT,
`width`:INT,
`url`:STRING
>
,
`displayName`:STRING,
`summary`:STRING,
`content`:STRING,
`url`:STRING,
`objectType`:STRING,
`author`:STRUCT
<
`id`:STRING,
`displayName`:STRING,
`summary`:STRING,
`content`:STRING,
`url`:STRING,
`objectType`:STRING,
`published`:STRING,
`updated`:STRING,
`upstreamDuplicates`:ARRAY<STRING>,
`downstreamDuplicates`:ARRAY<STRING>
>
,
`published`:STRING,
`updated`:STRING,
`upstreamDuplicates`:ARRAY<STRING>,
`downstreamDuplicates`:ARRAY<STRING>
>
,
`published` STRING,
`updated` STRING,
`upstreamDuplicates` ARRAY<STRING>,
`downstreamDuplicates` ARRAY<STRING>,
`address` STRUCT
<
`post-office-box`:STRING,
`extended-address`:STRING,
`street-address`:STRING,
`locality`:STRING,
`region`:STRING,
`postal-code`:STRING,
`country-name`:STRING
>
,
`position` STRUCT
<
`altitude`:FLOAT,
`latitude`:FLOAT,
`longitude`:FLOAT
>

)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES ("ignore.malformed.json" = "true"
STORED AS TEXTFILE
LOCATION '${hiveconf:path}';
