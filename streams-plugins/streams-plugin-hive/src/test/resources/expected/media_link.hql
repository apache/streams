CREATE TABLE `media_link`
(
`duration` FLOAT,
`height` INT,
`width` INT,
`url` STRING
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES ("ignore.malformed.json" = "true"
STORED AS TEXTFILE
LOCATION '${hiveconf:path}';
