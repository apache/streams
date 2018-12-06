<#ftl output_format="XML" auto_esc=true>
<#setting output_encoding="UTF-8">
<#setting url_escaping_charset="UTF-8">
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
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@base <${namespace}> .

# account.js

<#attempt>
<#assign id="${account.accountId}">
<#recover>
<#stop "NO_ID">
</#attempt>

:${id} a apst:TwitterProfile .

:${id}
  as:displayName "${account.accountDisplayName}" ;
  as:name "${account.username}" ;
  vcard:email "${account.email}" ;
<#attempt>
  <#assign createdAt_date = account.createdAt?datetime.iso>
  <#assign createdAt_xsnz = createdAt_date?string.xs_nz>
  dct:created "${createdAt_xsnz}"^^xs:dateTime ;
  <#recover>
    # CREATED_AT TIMESTAMP PROCESSING FAILED
    # account.createdAt: ${account.createdAt}
    <#if createdAt_date??>
    # createdAt_date: ${createdAt_date}
    </#if>
    <#if createdAt_xsnz??>
    # createdAt_xsnz: ${createdAt_xsnz}
    </#if>
</#attempt>
  .

# profile.js
<#assign profiles = pp.loadData('json', 'profile.js')>
<#assign profile = profiles[0].profile>

:${id}
  as:summary "${profile.description.bio}" ;
  as:url "${profile.description.website}" ;
  .

<#assign idshash = {} />

<#attempt>
<#assign followers = pp.loadData('json', 'follower.js')>
<#recover>
</#attempt>

<#if followers??>
<#list followers as follower>
<#assign fid = "${follower.follower.accountId}">
<#assign idshash += { fid: fid } />
:${fid} a apst:TwitterProfile .
:${fid}-follow-${id} a as:Follow ;
  as:actor :${fid} ;
  as:object :${id} .

</#list>
</#if>

<#attempt>
<#assign friends = pp.loadData('json', 'following.js')>
<#recover>
</#attempt>

<#if friends??>
<#list friends as friend>
<#assign fid = "${friend.following.accountId}">
<#if idshash[fid]??>
:${fid} a apst:TwitterProfile .
<#else>
  <#assign idshash += { fid: fid } />
</#if>

:${id}-follow-${fid} a as:Follow ;
  as:actor :${id} ;
  as:object :${fid} .

</#list>
</#if>

<#attempt>
  <#assign directmessageheaders = pp.loadData('json', 'direct-message-headers.js')>
  <#recover>
</#attempt>

<#if directmessageheaders??>
  <#list directmessageheaders as directmessageheader>
    <#assign conversation = directmessageheader.dmConversation/>
    <#list conversation.messages as message>
      <#if message.messageCreate??>
        <#assign senderId = "${message.messageCreate.senderId}">
        <#assign recipientId = "${message.messageCreate.recipientId}">
        <#assign messageId = "${message.messageCreate.id}">
        <#if idshash[senderId]??>
        <#else>
:${senderId} a apst:TwitterProfile .
          <#assign idshash += { senderId: senderId } />
        </#if>
        <#if idshash[recipientId]??>
        <#else>
:${recipientId} a apst:TwitterProfile .
          <#assign idshash += { recipientId: recipientId } />
        </#if>
:${senderId}-message-${recipientId}-${messageId}
  a as:Note ;
  as:actor :${senderId} ;
  as:object :${recipientId} ;
        <#attempt>
        <#assign createdAt_datetime = message.messageCreate.createdAt?datetime.iso>
        <#assign createdAt_datetime_xsnz = createdAt_datetime?string.xs_nz>
  as:published "${createdAt_datetime_xsnz}"^^xs:dateTime ;
        <#recover>
  # DIRECT MESSAGE TIMESTAMP PROCESSING FAILED
          <#if message.messageCreate.createdAt??>
  # message.messageCreate.createdAt: ${message.messageCreate.createdAt}
          </#if>
          <#if createdAt_datetime??>
  # createdAt_datetime: ${createdAt_datetime}
          </#if>
          <#if createdAt_datetime_xsnz??>
  # createdAt_datetime_xsnz: ${createdAt_datetime_xsnz}
          </#if>
        </#attempt>
  .
      </#if>
    </#list>
  </#list>
</#if>

<#assign usernameshash = {} />

<#attempt>
  <#assign tweets = pp.loadData('json', 'tweet.js')>
  <#recover>
</#attempt>

<#if tweets??>
  <#list tweets as tweet>
:${id}-note-${tweet.id}
  a as:Note ;
  as:actor :${id} ;
    <#attempt>
      <#assign createdAt_datetime = tweet.created_at?datetime("EEE MMM dd HH:mm:ss Z yyyy")>
      <#assign createdAt_datetime_xsnz = createdAt_datetime?string.xs_nz>
  as:published "${createdAt_datetime_xsnz}"^^xs:dateTime ;
      <#recover>
  # TWEET TIMESTAMP PROCESSING FAILED
        <#if tweet.created_at??>
  # tweet.created_at: ${tweet.created_at}
        </#if>
        <#if createdAt_datetime??>
  # createdAt_datetime: ${createdAt_datetime}
        </#if>
        <#if createdAt_datetime_xsnz??>
  # createdAt_datetime_xsnz: ${createdAt_datetime_xsnz}
        </#if>
    </#attempt>
  .

    <#if tweet.entities.user_mentions??>
      <#list tweet.entities.user_mentions as user_mention>
        <#if ( user_mention.id?? && user_mention.id?number gt 0 )>
          <#if idshash[user_mention.id]??>
          <#else>
:${user_mention.id} a apst:TwitterProfile .

            <#assign idshash += { user_mention.id_str: user_mention.id_str } />
          </#if>
          <#if usernameshash[user_mention.id]??>
          <#else>
:${user_mention.id} as:name "${user_mention.screen_name}" .

            <#assign usernameshash += { user_mention.id: user_mention.screen_name } />
          </#if>
:${id}-note-${tweet.id} as:cc :${user_mention.id} .

        </#if>
      </#list>
    </#if>
  </#list>
</#if>
