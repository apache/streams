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
  <#assign profiles=pp.loadData('csv', 'Profile.csv', {'separator':',', 'normalizeHeaders': true})>
  <#recover>
    <#stop "NO_PROFILES">
</#attempt>
<#attempt>
  <#assign profile=profiles[0]>
  <#recover>
    <#stop "NO_PROFILE">
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

<#--
# Profile.csv 
# First Name,Last Name,Maiden Name,Created Date,Address,Birth Date,Contact Instructions,Marital Status,Headline,Summary,Industry,Association
# <#list profiles as profile> <#list profile as key, value>${key}: ${value} </#list> </#list>
-->
<#attempt>
<#assign raw="${profile.first_name}${profile.last_name}">
<#assign id=raw?replace("\\W","","r")>
<#recover>
<#stop "NO_ID">
</#attempt>

:${id} a apst:LinkedinProfile .

:${id}
<#-- first row only -->
  as:displayName "${profile.first_name}" ;
  vcard:fn "${profile.first_name} ${profile.last_name}" ;
  vcard:given-name "${profile.first_name}" ;
  vcard:family-name "${profile.last_name}" ;
  .

<#--
# Registration.csv
# Registration Date,Registration IP,Subscription Type,Inviter First Name,Inviter Last Name
# <#list registration_infos as registration_info> <#list registration_info as key, value>${key}: ${value} </#list> </#list>
-->

<#attempt>
<#assign registrations = pp.loadData('csv', 'Registration.csv', {'separator':',', 'normalizeHeaders': true})>
<#recover>
</#attempt>

<#if registrations??>
:${id}
<#list registrations as registration>
  dct:createdAt "${registration.registration_date}" ;
</#list>
  .
</#if>

<#--  
# Email Addresses.csv 
# Email Address,Confirmed,Is primary,Status Updated On
# <#list email_addresses as email_address> <#list email_address as key, value>${key}: ${value} </#list> </#list>
-->

<#attempt>
<#assign email_addresses = pp.loadData('csv', 'Email Addresses.csv', {'separator':',', 'normalizeHeaders': true})>
<#recover>
</#attempt>

<#if email_addresses??>
:${id}
<#list email_addresses as email_address>
  vcard:email "mailto:${email_address.email_address}" ;
</#list>
  .
</#if>

<#--
# PhoneNumbers.csv
# Number,Extension,Type
# <#list phone_numbers as phone_number> <#list phone_number as key, value>${key}: ${value} </#list> </#list>
-->

<#attempt>
<#assign phone_numbers = pp.loadData('csv', 'Phone Numbers.csv', {'separator':',', 'normalizeHeaders': true})>
<#recover>
</#attempt>

<#if phone_numbers??>
:${id}
<#list phone_numbers as phone_number>
  vcard:tel "tel:${phone_number.number}" ;
</#list>
  .
</#if>

<#--
# Imported Contacts.csv
# First Name,Last Name,Companies,Title,Email Address,Phone Numbers,Created At,Instant Message Handles,Addresses,Sites,Full Name,Birthday,Location,Bookmarked At,Profiles
-->

<#attempt>
  <#assign contacts = pp.loadData('csv', 'Imported Contacts.csv', {'separator':',', 'normalizeHeaders': true})>
  <#recover>
</#attempt>

<#--
# Connections.csv
# First Name,Last Name,Address,Email Address,Company,Position,Connected On,Websites,Instant Messengers
-->

<#attempt>
<#assign connections = pp.loadData('csv', 'Connections.csv', {'separator':',', 'normalizeHeaders': true})>
<#recover>
</#attempt>

<#if connections??>
<#list connections as connection>
<#assign craw = "${connection.first_name}${connection.last_name}">
<#assign cid=craw?replace("\\W","","r")>
:${cid}
  a apst:LinkedinProfile ;
  vcard:fn "${connection.first_name} ${connection.last_name}" ;
  vcard:given-name "${connection.first_name}" ;
  vcard:family-name "${connection.last_name}" ;
  vcard:email "mailto:${connection.email_address}" ;
  vcard:org "${connection.company?replace("\\W"," ","r")}" ;
  vcard:title "${connection.position?replace("\\W"," ","r")}" ;
<#list contacts as contact>
  <#if (contact.first_name == connection.first_name ) && (contact.last_name == connection.last_name)>
  <#attempt>
    <#list contact.email_address?split(",") as email_address>
      <#if email_address != connection.email_address>
  vcard:email "mailto:${email_address}" ;
      </#if>
    </#list>
  <#recover>
  </#attempt>
  <#attempt>
    <#list contact.phone_numbers?split(",") as phone_number>
  vcard:tel "tel:${phone_number}" ;
    </#list>
    <#recover>
  </#attempt>
  </#if>
</#list>
  .

:${id}-connect-${cid}
  a as:Connect ;
  as:actor :${id} ;
  as:object :${cid} ;
  as:published "${connection.connected_on}" ;
  .

</#list>
</#if>

<#--
Messages.csv
From,To,Date,Subject,Content,Direction,Folder
-->

<#attempt>
<#assign messages = pp.loadData('csv', 'Messages.csv', {'separator':',', 'normalizeHeaders': true})>
<#recover>
</#attempt>

<#if messages??>
<#list messages as message>
<#assign aidraw = "${message.from}">
<#assign aid=aidraw?replace("\\W","","r")>
<#assign oidraw = "${message.to}">
<#assign oid=oidraw?replace("\\W","","r")>
<#assign subjectraw = "${message.subject}">
<#assign subjectid=subjectraw?replace("\\W","","r")> 
:${aid}-message-${oid}-${subjectid}
  a as:Note ;
  as:actor :${aid} ;
  as:object :${oid} ;
  .
</#list>
</#if>

<#--
Recommendations Given.csv
First Name,Last Name,Company,Job Title,Text,Creation Date,Status
-->

<#attempt>
<#assign recommendations_given = pp.loadData('csv', 'Recommendations Given.csv', {'separator':',', 'normalizeHeaders': true})>
<#recover>
</#attempt>

<#if recommendations_given??>
<#list recommendations_given as recommendation>
<#assign oidraw = "${recommendation.first_name}${recommendation.last_name}">
<#assign oid=oidraw?replace("\\W","","r")>
:${id}-like-${oid}
  a as:Like ;
  as:actor :${id} ;
  as:object :${oid} ;
  as:published "${recommendation.creation_date}" ;
  .
</#list>
</#if>

<#--
Recommendations Received.csv
First Name,Last Name,Company,Job Title,Text,Creation Date,Status
-->

<#attempt>
<#assign recommendations_received = pp.loadData('csv', 'Recommendations Received.csv', {'separator':',', 'normalizeHeaders': true})>
<#recover>
</#attempt>

<#if recommendations_given??>
<#list recommendations_given as recommendation>
<#assign aidraw = "${recommendation.first_name}${recommendation.last_name}">
<#assign aid=aidraw?replace("\\W","","r")>
:${aid}-like-${id}
  a as:Like ;
  as:actor :${aid} ;
  as:object :${id} ;
  as:published "${recommendation.creation_date}" ;
  .
</#list>
</#if>

