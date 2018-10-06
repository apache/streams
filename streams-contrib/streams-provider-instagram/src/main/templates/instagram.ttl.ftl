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
  <#assign profile = pp.loadData('json', 'profile.json')>
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
<#assign id="${profile.username}">
<#recover>
<#stop "NO_ID">
</#attempt>

:${id} a apst:InstagramProfile .

:${id}
  as:displayName "${profile.username}" ;
  vcard:fn "${profile.name}" ;
  dct:created "${profile.date_joined}" ;
  .

:${id}
  vcard:email "mailto:${profile.email}" ;
  .

<#if profile.phone_number??>
:${profile.username}	
  vcard:tel "tel:${profile.phone_number}" ;
  .

</#if>

<#attempt>
<#assign connections = pp.loadData('json', 'connections.json')>
<#recover>
</#attempt>

<#if connections??>
<#list connections.followers as handle, since>
<#assign fid = "${handle}">
:${fid} a apst:InstagramProfile ;
        as:displayName "${handle}" .
:${fid}follow${id} a as:Follow ;
  as:actor :${fid} ;
  as:object :${id} ;
  as:published "${since}" .

</#list>

<#list connections.following as handle, since>
<#assign fid = "${handle}">
:${fid} a apst:InstagramProfile ;
        as:displayName "${handle}" .
:${id}-follow-${fid} a as:Follow ;
  as:actor :${id} ;
  as:object :${fid} ;
  as:published "${since}" .

</#list>
</#if>

<#attempt>
<#assign messages = pp.loadData('json', 'messages.json')>
<#recover>
</#attempt>

<#if messages??>
<#list messages as thread>
<#assign fids = thread.participants![]>
<#list fids as fid>
<#if fid != profile.username>
:${fid} a apst:InstagramProfile ;
        as:displayName "${fid}" .
:${id}-message-${fid} a as:Note ;
  as:actor :${id} ;
  as:object :${fid} .

</#if>
</#list>
</#list>
</#if>

