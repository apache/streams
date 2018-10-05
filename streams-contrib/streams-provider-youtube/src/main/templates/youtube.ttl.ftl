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
@prefix apst: <http://streams.apache.org/ns#> .
@prefix as: <http://www.w3.org/ns/activitystreams#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix xml: <http://www.w3.org/XML/1998/namespace> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
@prefix prov: <http://www.w3.org/ns/prov#> .
@prefix dc: <http://purl.org/dc/elements/1.1/#> .
@prefix dct: <http://purl.org/dc/terms/#> .
@base <http://graph.bluesquad.co/youtube> .

<#attempt>
<#assign raw=profile.name.formattedName>
<#assign id=raw?replace("\\W","","r")>
<#recover>
<#stop "NO_ID">
</#attempt>

# profile.json
:${id} a apst:YouTubeProfile .

:${id}
  as:displayName "${profile.displayName}" ;
  vcard:fn "${profile.name.formattedName}" ;
  vcard:given-name "${profile.name.givenName}" ;
  vcard:fn "${profile.name.familyName}" ;
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



