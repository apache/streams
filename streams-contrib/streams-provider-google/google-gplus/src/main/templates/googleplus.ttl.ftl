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
  <#assign profile = pp.loadData('json', 'Takeout/Profile/Profile.json')>
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

<#attempt>
<#assign raw=profile.name.formattedName>
<#assign id=raw?replace("\\W","","r")>
<#recover>
<#stop "NO_ID">
</#attempt>

# profile.json
:${id} a apst:GooglePlusProfile .

:${id}
  as:displayName "${profile.displayName}" ;
  vcard:fn "${profile.name.formattedName}" ;
  vcard:given-name "${profile.name.givenName}" ;
  vcard:family-name "${profile.name.familyName}" ;
  .

<#if profile.email??>
:${id}
  vcard:email "mailto:${profile.email}" ;
  .
</#if>

<#if profile.phone_number??>
:${id}	
  vcard:tel "tel:${profile.phone_number}" ;
  .
</#if>

<#--
Google+ Circles\*.csv
First Name,Last Name,Nickname,Display Name,Profile URL
-->

<#assign friends = pp.loadData('csv', 'Takeout/Google+ Circles/Friends.csv', {'separator':',', 'normalizeHeaders': true})>

<#if friends??>
<#list friends as friend>
<#assign fraw = "${friend.display_name}">
<#assign fid=fraw?replace("\\W","","r")>
:${fid} a apst:GooglePlusProfile ;
        as:displayName "${friend.display_name}" .

:${id}-connect-${fid} a as:Connect ;
  as:actor :${id} ;
  as:object :${fid}
  .

</#list>
</#if>

