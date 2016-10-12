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
CREATE TABLE `activity`
(
`id` STRING,
`actor` STRUCT
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
`verb` STRING,
`object` STRUCT
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
`target` STRUCT
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
`generator` STRUCT
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
`icon` STRUCT
<
`duration`:FLOAT,
`height`:INT,
`width`:INT,
`url`:STRING
>
,
`provider` STRUCT
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
`title` STRING,
`content` STRING,
`url` STRING,
`links` ARRAY<STRING>
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES ("ignore.malformed.json" = "true"
STORED AS TEXTFILE
LOCATION '${hiveconf:path}';
