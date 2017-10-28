-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

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
