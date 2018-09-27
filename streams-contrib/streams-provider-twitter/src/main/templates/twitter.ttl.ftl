<#ftl output_format="XML" auto_esc=true>
<#--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->
<#attempt>
  <#assign accounts = pp.loadData('json', 'account.js')>
  <#assign account = accounts[0].account>
  <#recover>
    <#stop "NO_PROFILE_INFORMATION">
</#attempt>
@prefix : <${namespace}#> .
@prefix as: <http://www.w3.org/ns/activitystreams#> .
@prefix apst: <http://streams.apache.org/ns#> .
@prefix dc: <http://purl.org/dc/elements/1.1/#> .
@prefix dct: <http://purl.org/dc/terms/#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
@base <${namespace}> .

# account.js

<#attempt>
<#assign id="${account.accountId}">
<#recover>
<#stop "NO_ID">
</#attempt>

:${id} a ap:TwitterProfile .

:${id}
  as:displayName "${account.accountDisplayName}" ;
  as:name "${account.username}" ;
  dct:created "${account.createdAt}" ;
  vcard:email "${account.email}" ;
  .

# profile.js
<#assign profiles = pp.loadData('json', '${root}/profile.js')>
<#assign profile = profiles[0].profile>

:${id}
  as:summary "${profile.description.bio}" ;
  as:url "${profile.description.website}" ;
  .

<#attempt>
<#assign followers = pp.loadData('json', '${root}/follower.js')>
<#recover>
</#attempt>

<#if followers??>
<#list followers as follower>
<#assign fid = "${follower.follower.accountId}">
:${fid} a ap:TwitterProfile .
:${fid}-follow-${id} a as:Follow ;
  as:actor :${fid} ;
  as:object :${id} .

</#list>
</#if>

<#attempt>
<#assign friends = pp.loadData('json', '${root}/following.js')>
<#recover>
</#attempt>

<#if friends??>
<#list friends as friend>
<#assign fid = "${friend.following.accountId}">
:${fid} a ap:TwitterProfile .
:${id}-follow-${fid} a as:Follow ;
  as:actor :${id} ;
  as:object :${fid} .

</#list>
</#if>
