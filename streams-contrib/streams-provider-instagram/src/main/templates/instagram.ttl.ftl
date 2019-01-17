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
<#if ID??>
  <#assign id="${ID}">
<#else>
  <#attempt>
    <#assign id="${profile.username}">
    <#recover>
      <#stop "NO_ID">
  </#attempt>
</#if>
<#if BASE_URI??>
@base <${BASE_URI}> .
@prefix : <${BASE_URI}> .
<#else>
@base <http://streams.apache.org/streams-contrib/streams-provider-instagram/> .
@prefix : <http://streams.apache.org/streams-contrib/streams-provider-instagram/> .
</#if>
<#if PREFIXES??>
@prefix as: <http://www.w3.org/ns/activitystreams#> .
@prefix apst: <http://streams.apache.org/ns#> .
@prefix dc: <http://purl.org/dc/elements/1.1/#> .
@prefix dct: <http://purl.org/dc/terms/#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
</#if>

:${id} a apst:InstagramProfile .

:${id}
  as:displayName "${profile.username}" ;
  vcard:fn "${profile.name}" ;
<#attempt>
  <#assign joined_date = profile.date_joined?datetime.iso>
  <#assign joined_date_xsnz = joined_date?string.xs_nz>
  dct:created "${joined_date_xsnz}"^^xs:dateTime ;
  <#recover>
  # SINCE TIMESTAMP PROCESSING FAILED
  # profile.date_joined: ${profile.date_joined}
    <#if joined_date??>
  # joined_date: ${joined_date}
    </#if>
    <#if joined_date_xsnz??>
  # joined_date_xsnz: ${joined_date_xsnz}
    </#if>
</#attempt>
  vcard:email "mailto:${profile.email}" ;
<#if profile.phone_number??>
  vcard:tel "tel:${profile.phone_number}" ;
</#if>
  .

<#attempt>
<#assign connections = pp.loadData('json', 'connections.json')>
<#recover>
</#attempt>

<#if connections??>
<#list connections.followers as handle, since>
<#assign fid = "${handle}">
:${fid} a apst:InstagramProfile ;
        as:displayName "${handle}" .

:${fid}-follow-${id} a as:Follow ;
  as:actor :${fid} ;
  as:object :${id} ;
  <#attempt>
    <#assign since_date = since?datetime.iso>
    <#assign since_xsnz = since_date?string.xs_nz>
  as:published "${since_xsnz}"^^xs:dateTime ;
    <#recover>
  # SINCE TIMESTAMP PROCESSING FAILED
  # since: ${since}
      <#if since_date??>
  # since_date: ${since_date}
      </#if>
      <#if since_xsnz??>
  # since_xsnz: ${since_xsnz}
      </#if>
  </#attempt>
  .

</#list>

<#list connections.following as handle, since>
<#assign fid = "${handle}">
:${fid} a apst:InstagramProfile ;
        as:displayName "${handle}" .

:${id}-follow-${fid} a as:Follow ;
  as:actor :${id} ;
  as:object :${fid} ;
<#attempt>
  <#assign since_date = since?datetime.iso>
  <#assign since_xsnz = since_date?string.xs_nz>
  as:published "${since_xsnz}"^^xs:dateTime ;
  <#recover>
  # SINCE TIMESTAMP PROCESSING FAILED
  # since: ${since}
    <#if since_date??>
  # since_date: ${since_date}
    </#if>
    <#if since_xsnz??>
  # since_xsnz: ${since_xsnz}
    </#if>
</#attempt>
   .

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
  as:object :${fid} ;
<#attempt>
  <#assign createdAt_date = thread.created_at?datetime.iso>
  <#assign createdAt_xsnz = createdAt_date?string.xs_nz>
  as:published "${createdAt_xsnz}"^^xs:dateTime ;
  <#recover>
  # CREATED_AT TIMESTAMP PROCESSING FAILED
    <#if thread.created_at??>
  # thread.created_at: ${thread.created_at}
    </#if>
    <#if createdAt_date??>
  # createdAt_date: ${createdAt_date}
    </#if>
    <#if createdAt_xsnz??>
  # createdAt_xsnz: ${createdAt_xsnz}
    </#if>
</#attempt>
  .

</#if>
</#list>
</#list>
</#if>

