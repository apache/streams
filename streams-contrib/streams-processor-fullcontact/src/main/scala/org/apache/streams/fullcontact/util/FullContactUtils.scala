/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.fullcontact.util

import org.apache.juneau.collections.{JsonList, JsonMap}
import org.apache.juneau.json.JsonParser
import org.apache.streams.config.ComponentConfigurator

import scala.collection.mutable.ListBuffer

case class Employer(name : String, domain : String)

case class EmployerRelationship(personId : String, employerId : String)

case class InterestTopic(id : String, name : String, category : String)

case class TopicRelationship(child : String, parent : String)

case class Organization(name : String, domain : String)

case class OrganizationRelationship(personId : String, orgId : String)

case class SocialProfileRelationship(personId : String, profileUri : String)

case class ImageRelationship( entityUri : String, url : String, label : String)

case class UrlRelationship( entityUri : String, url : String, label : String)

object FullContactUtils {

  import org.apache.streams.fullcontact.api._
  import org.apache.streams.fullcontact.config._
  import org.apache.streams.fullcontact.pojo._

  import scala.collection.JavaConversions._
  import scala.util.Try

  final implicit val fullContactConfiguration = new ComponentConfigurator(classOf[FullContactConfiguration]).detectConfiguration()

  val apst_angellist_ns = "http://streams.apache.org/streams-contrib/streams-provider-angellist#"
  val apst_facebook_ns = "http://streams.apache.org/streams-contrib/streams-provider-facebook#"
  val apst_foursquare_ns = "http://streams.apache.org/streams-contrib/streams-provider-foursquare#"
  val apst_googleplus_ns = "http://streams.apache.org/streams-contrib/streams-provider-googleplus#"
  val apst_instagram_ns = "http://streams.apache.org/streams-contrib/streams-provider-instagram#"
  val apst_linkedin_ns = "http://streams.apache.org/streams-contrib/streams-provider-linkedin#"
  val apst_twitter_ns = "http://streams.apache.org/streams-contrib/streams-provider-twitter#"

  val fc_ns = "http://api.fullcontact.com/"
  val fc_image_ns = "http://api.fullcontact.com/image/"
  val fc_org_ns = "http://api.fullcontact.com/organization/"
  val fc_person_ns = "http://api.fullcontact.com/person/"
  val fc_profile_ns = "http://api.fullcontact.com/profile/"
  val fc_topic_ns = "http://api.fullcontact.com/topic/"
  val fc_url_ns = "http://api.fullcontact.com/url/"

  val fc_prefix = "fc"
  val fc_image_prefix = "fc_img"
  val fc_org_prefix = "fc_org"
  val fc_person_prefix = "fc_person"
  val fc_profile_prefix = "fc_profile"
  val fc_topic_prefix = "fc_topic"
  val fc_url_prefix = "fc_url"

  def imgId( url: String ) : String = url.hashCode.toString

  def urlId( url: String ) : String = url.hashCode.toString

  def companyId( company: CompanySummary ) : String = {
    company.getName.replaceAll("\\p{Punct}","-")
  }

  def orgId( organization: Organization ) : String = {
    organization.domain.replaceAll("\\p{Punct}", "-")
  }

  def orgLabel( input: String ) : String = {
    input.replaceAll("\\p{Punct}", " ")
  }

  def orgLabel( organization: Organization ) : String = {
    orgLabel(organization.name)
  }

  def personId( input : PersonSummary ) : String = {
    input.getFullName.replaceAll("\\W","")
  }

  def personSafeName( input : PersonSummary ) : String = {
    input.getFullName.replaceAll("\\W","")
  }

  def profileUsername( input : SocialProfile ) : String = {
    input.getUsername.replaceAll("\\W","")
  }

  def profileNamespaceAndId( profile : SocialProfile ) : (String,String) = {
    profile.getService match {
      case "angellist" => (apst_angellist_ns,profileId(profile))
      case "twitter" => (apst_twitter_ns,profileId(profile))
      case "facebook" | "facebookpage" => (apst_facebook_ns,profileId(profile))
      case "foursquare" => (apst_foursquare_ns,profileId(profile))
      case "linkedin" => (apst_linkedin_ns,profileId(profile))
      case "instagram" => (apst_instagram_ns,profileUsername(profile))
      case "googleplus" => (apst_googleplus_ns,profileId(profile))
    }
  }

  def uriFromNamespaceAndId( ns_id : (String, String) ) = s"${ns_id._1}${ns_id._2}"

  def profilePrefixAndType( profile : SocialProfile ) : (String,String) = {
    profile.getService match {
      case "angellist" => ("fc","Profile")
      case "twitter" => ("apst","TwitterProfile")
      case "facebook" | "facebookpage" => ("apst","FacebookProfile")
      case "foursquare" => ("apst","FoursquareProfile")
      case "linkedin" => ("apst","LinkedinProfile")
      case "instagram" => ("apst","InstagramProfile")
      case "googleplus" => ("apst","GooglePlusProfile")
    }
  }

  def profileId( profile : SocialProfile ) : String = {
    val idOrUsername = {
      if( profile.getUserid.nonEmpty ) profile.getUserid
      else if ( profile.getUsername.nonEmpty ) profile.getUsername
      else profile.getUrl.split("/").last
    }
    s"${profile.getService}/${idOrUsername}"
  }

  def topicId( topic : InterestTopic ) : String = {
    topic.id
  }

  def selectProfiles( profiles : SocialProfiles) : Seq[SocialProfile] = {
    val list = new ListBuffer[SocialProfile]
    list += profiles.getAngellist
    list += profiles.getFacebook
    list += profiles.getFacebookpage
    list += profiles.getFoursquare
    list += profiles.getGoogleplus
    list += profiles.getInstagram
    list += profiles.getLinkedin
    list += profiles.getTwitter
    list
  }

  def SocialProfileRelationships( summary : PersonSummary ) : Seq[SocialProfileRelationship] = {
    val list = new ListBuffer[SocialProfileRelationship]
    for( profile <- selectProfiles(summary.getDetails.getProfiles())) {
      val person_id = Try(personId(summary))
      val profile_uri = Try(uriFromNamespaceAndId(profileNamespaceAndId(profile)))
      if( person_id.isSuccess &&
          person_id.get != null &&
          !person_id.get.isEmpty &&
          profile_uri.isSuccess &&
          profile_uri.get != null &&
            !profile_uri.get.isEmpty ) {
        val rel = SocialProfileRelationship(person_id.get, profile_uri.get)
        list += rel
      }
    }
    list
  }

  def allInterestItems( input : Iterator[PersonSummary] ) : Iterator[PersonInterestItem] = {
    input.flatMap(person => person.getDetails.getInterests.toSeq)
  }

  def uniqueInterests( input : Iterator[PersonInterestItem] ) : Iterator[InterestTopic] = {
    input.map(interestItem => InterestTopic(interestItem.getId, interestItem.getName, interestItem.getCategory)).toSet.toIterator
  }

  def topicHierarchy( input : Iterator[PersonInterestItem] ) : Iterator[TopicRelationship] = {
    input.flatMap(item => item.getParentIds.map(parentId => TopicRelationship(item.getId, parentId))).toSet.toIterator
  }

  def allImageRelationships( input : Iterator[PersonSummary] ) : Iterator[ImageRelationship] = {
    input.flatMap(item => item.getDetails.getPhotos.flatMap(
      photo => Try(ImageRelationship(uriFromNamespaceAndId(fc_person_ns,personId(item)), photo.getValue, photo.getLabel)).toOption
    ))
  }

  def allUrlRelationships( input : Iterator[PersonSummary] ) : Iterator[UrlRelationship] = {
    input.flatMap(item => item.getDetails.getUrls.flatMap(
      url => Try(UrlRelationship(uriFromNamespaceAndId(fc_person_ns,personId(item)), url.getValue, url.getLabel)).toOption
    ))
  }

  def allOrganizationItems( input : Iterator[PersonSummary] ) : Iterator[Organization] = {
    input.flatMap(person => person.getDetails.getEmployment.map(employment => Organization(employment.getName, employment.getDomain))).toSet.toIterator
  }

  def allProfiles( input : Iterator[PersonSummary] ) : Iterator[SocialProfile] = {
    input.flatMap(summary => selectProfiles(summary.getDetails.getProfiles))
  }

  def allProfileRelationships( input : Iterator[PersonSummary] ) : Iterator[SocialProfileRelationship] = {
    input.flatMap(SocialProfileRelationships)
  }

  def allEmploymentItems( input : Iterator[PersonSummary] ) : Iterator[PersonEmploymentItem] = {
    input.flatMap(person => person.getDetails.getEmployment.toSeq)
  }

  def uniqueEmployers( input : Iterator[PersonEmploymentItem] ) : Iterator[Employer] = {
    input.map(employmentItem => Employer(employmentItem.getName, employmentItem.getDomain)).toSet.toIterator
  }

  def personSummaryAsTurtle(root: PersonSummary): String = {
    val id = personId(root)
    val sb = new StringBuilder()
    sb.append(s"""|${fc_person_prefix}:${id}
                  |      a ${fc_prefix}:Person ;
                  |      as:displayName "${root.getFullName}" ;
                  |      as:url "${root.getWebsite}" ;
                  |      dct:modified "${root.getUpdated}" ;
                  |      vcard:fn "${root.getFullName}" ;
                  |      vcard:org "${orgLabel(root.getOrganization)}" ;
                  |      vcard:title "${root.getTitle}" ;
                  |      .
                  |
                  |""".stripMargin)
    if( !root.getGender.isEmpty)
      sb.append(s"""${fc_person_prefix}:${id} vcard:gender "${root.getGender}" . """).append("\n")
    if( root.getDetails.getEmails != null && !root.getDetails.getEmails.isEmpty)
      root.getDetails.getEmails.foreach (
        email => {
          sb.append(s"""${fc_person_prefix}:${id} vcard:email "mailto:${email.getValue}" . """).append("\n")
        }
      )
    if( root.getDetails.getPhones != null && !root.getDetails.getPhones.isEmpty)
      root.getDetails.getPhones.foreach (
        tel => {
          sb.append(s"""${fc_person_prefix}:${id} vcard:tel "tel:${tel.getValue}" . """).append("\n")
        }
      )
    if( root.getDetails.getUrls != null && !root.getDetails.getUrls.isEmpty)
      root.getDetails.getUrls.foreach (
        url => {
          sb.append(s"""${fc_person_prefix}:${id} vcard:url "${url.getValue}" . """).append("\n")
        }
      )
    sb.toString
  }

  def safe_personSummaryAsTurtle(root: PersonSummary): Option[String] = {
    Try(personSummaryAsTurtle(root)).toOption
  }

  def urlRelationshipAsTurtle(item: UrlRelationship) : String = {
    val uri = fc_url_ns + urlId(item.url)
    val sb = new StringBuilder()
    sb.append(s"""|<${uri}>
                  |   a as:Link ;
                  |   as:href "${item.url}" ;
                  |   as:rel "${item.label}" .
                  |
                  |<${item.entityUri}> as:link <${uri}> .
                  |
                  |""".stripMargin)
    sb.toString
  }

  def safe_urlRelationshipAsTurtle(item: UrlRelationship) : Option[String] = {
    Try(urlRelationshipAsTurtle(item)).toOption
  }

  def imageRelationshipAsTurtle(item: ImageRelationship) : String = {
    val uri = fc_url_ns + urlId(item.url)
    val sb = new StringBuilder()
    sb.append(s"""|<${uri}>
                  |   a as:Image ;
                  |   as:href "${item.url}" ;
                  |   as:rel "${item.label}" .
                  |
                  |<${item.entityUri}> as:image <${uri}> .
                  |
                  |""".stripMargin)
    sb.toString
  }

  def safe_imageRelationshipAsTurtle(item: ImageRelationship) : Option[String] = {
    Try(imageRelationshipAsTurtle(item)).toOption
  }

  def interestTopicAsTurtle(topic: InterestTopic): String = {
    val id = topic.id
    val name = topic.name.replaceAll("\\p{Punct}"," ");
    val sb = new StringBuilder()
    sb.append(s"""|${fc_topic_prefix}:${id} a skos:Concept .
                  |${fc_topic_prefix}:${id} skos:prefLabel "${name}" .
                  |
                  |""".stripMargin)
    sb.toString
  }

  def safe_interestTopicAsTurtle(topic: InterestTopic): Option[String] = {
    Try(interestTopicAsTurtle(topic)).toOption
  }

  def topicRelationshipAsTurtle(relationship: TopicRelationship): String = {
    s"${fc_topic_prefix}:${relationship.child} skos:broader ${fc_topic_prefix}:${relationship.parent} ."
  }

  def personInterestsAsTurtle(root: PersonSummary): String = {
    val id = personId(root)
    val sb = new StringBuilder()
    if( root.getDetails.getInterests != null && !root.getDetails.getInterests.isEmpty)
      root.getDetails.getInterests.foreach (
        interest => {
          sb.append(s"""${fc_person_prefix}:${id} foaf:interest ${fc_topic_prefix}:${interest.getId} . """).append("\n")
        }
      )
    sb.toString
  }

  def safe_personInterestsAsTurtle(root: PersonSummary): Option[String] = {
    Try(personInterestsAsTurtle(root)).toOption
  }

  def organizationAsTurtle(organization: Organization): String = {
    val ns = fc_org_ns
    val id = orgId(organization)
    val sb = new StringBuilder()
    sb.append(s"""|${fc_org_prefix}:${id}
                  |      a ${fc_prefix}:Organization ;
                  |      as:displayName "${orgLabel(organization)}" ;
                  |      as:url "${organization.domain}" ;
                  |      .
                  |
                  |""".stripMargin)
    sb.toString
  }

  def safe_organizationAsTurtle(organization: Organization): Option[String] = {
    Try(organizationAsTurtle(organization)).toOption
  }

  def SocialProfileRelationshipAsTurtle(relationship: SocialProfileRelationship): String = {
    s"<${relationship.profileUri}> as:describes ${fc_person_prefix}:${relationship.personId} ."
  }

  def safe_SocialProfileRelationshipAsTurtle(relationship: SocialProfileRelationship): Option[String] = {
    import scala.util.Try
    Try(SocialProfileRelationshipAsTurtle(relationship)).toOption
  }

  def profileAsTurtle(profile: SocialProfile): String = {
    val id = profileId(profile)
    val uri = uriFromNamespaceAndId(profileNamespaceAndId(profile))
    val prefix_type = profilePrefixAndType(profile)
    val bio = profile.getBio.replaceAll("\\p{Punct}"," ");
    val sb = new StringBuilder()
    sb.append(s"""|<$uri>
                  |      a ${prefix_type._1}:${prefix_type._2} ;
                  |      as:id "${id}" ;
                  |      as:name "${profile.getUsername}" ;
                  |      as:displayName "${profile.getUsername}" ;
                  |      as:summary "${bio}" ;
                  |      as:url "${profile.getUrl}" ;
                  |      as:provider "${profile.getService}" ;
                  |""".stripMargin)
    if( profile.getFollowers != null)
      sb.append(s"""      apst:followers "${profile.getFollowers}"^^xs:integer ;""").append("\n")
    if( profile.getFollowing != null)
      sb.append(s"""      apst:following "${profile.getFollowing}"^^xs:integer ;""").append("\n")
    sb.append("").append("\n")
    sb.toString
  }

  def safe_profileAsTurtle(profile: SocialProfile): Option[String] = {
    Try(profileAsTurtle(profile)).toOption
  }


  def companySummaryAsTurtle(root: CompanySummary): String = {
    import scala.collection.JavaConversions._
    val ns = fc_org_ns
    val id = companyId(root)
    val bio = root.getBio.replaceAll("\\p{Punct}"," ");
    val sb = new StringBuilder()
    sb.append(s"""|${fc_org_prefix}:${id}
                  |      a ${fc_prefix}:Organization ;
                  |      as:displayName "${root.getName}" ;
                  |      as:url "${root.getWebsite}" ;
                  |      as:summary "${bio}" ;
                  |      ${fc_prefix}:employees "${root.getEmployees}"^^xs:integer ;
                  |      ${fc_prefix}:founded "${root.getFounded}"^^xs:integer ;
                  |      vcard:category "${root.getCategory}" ;
                  |      .
                  |""".stripMargin)
    if( root.getDetails.getEmails != null && !root.getDetails.getEmails.isEmpty)
      root.getDetails.getEmails.foreach (
        email => {
          sb.append(s"""${fc_org_prefix}:${id} vcard:email "mailto:${email.getValue}" . """).append("\n")
        }
      )
    if( root.getDetails.getPhones != null && !root.getDetails.getPhones.isEmpty)
      root.getDetails.getPhones.foreach (
        phone => {
          sb.append(s"""${fc_org_prefix}:${id} vcard:tel "tel:${phone.getValue}" . """).append("\n")
        }
      )
    if( root.getDetails.getUrls != null && !root.getDetails.getUrls.isEmpty)
      root.getDetails.getUrls.foreach (
        url => {
          sb.append(s"""${fc_org_prefix}:${id} vcard:url "${url.getValue}" . """).append("\n")
        }
      )
    sb.toString
  }

  def safe_companySummaryAsTurtle(profile: CompanySummary): Option[String] = {
    Try(companySummaryAsTurtle(profile)).toOption
  }

  def normalize_age_range(ageRange: String)  : Option[Int] = {
    val avg = Try {
      val seq = ageRange.split("-").map(_.toLong).toSeq
      (seq.sum / seq.length).toInt
    }.toOption
    avg
  }

  def callEnrichPerson(request : EnrichPersonRequest)(implicit config : FullContactConfiguration) : String = {
    import org.apache.http.client.utils.URIBuilder
    import org.apache.juneau.json.{JsonParser, JsonSerializer}
    import org.apache.juneau.rest.client.RestClient

    import java.net.URI
    val auth_header = s"Bearer ${config.getToken()}"
    val url = "https://api.fullcontact.com/v3/person.enrich"
    val uri : URI = new URIBuilder(url).build()
    lazy val parser = JsonParser.create().
      debug().
      ignoreUnknownBeanProperties().
      build()
    lazy val serializer = JsonSerializer.create().
      debug().
      keepNullProperties(false).
      trimEmptyCollections(true).
      trimEmptyMaps(true).
      build()
    val restClientBuilder = RestClient.
      create().
      header("Authorization", auth_header).
      beansRequireSerializable().
      debug().
      disableAutomaticRetries().
      disableCookieManagement().
      disableRedirectHandling().
      json().
      parser(parser).
      rootUrl(uri).
      serializer(serializer)

    val post = restClientBuilder.
      build().
      post(uri, request)

    Thread.sleep(1000)

    post.getResponseAsString
  }

  def safeCallEnrichPerson(request : EnrichPersonRequest) : Option[String] = {
    Try(callEnrichPerson(request)).toOption
  }

  def parseEnrichPersonResponse(response : String): PersonSummary = {
    import org.apache.juneau.json.JsonParser
    val result = JsonParser.DEFAULT.parse(response, classOf[PersonSummary])
    result
  }

  def safeParseEnrichPersonResponse(response : String): Option[PersonSummary] = {
    Try(parseEnrichPersonResponse(response)).toOption
  }

  def educationItem(row : JsonMap) : Option[String] = Try(f"""${row.getString("name","")}%s ${row.getString("degree","")}%s (${row.getAt("start/year",classOf[String])}%s - ${row.getAt("end/year",classOf[String])}%s)""").toOption
  def educationSummary(json : String) : List[String] = {
    val list = JsonParser.DEFAULT.parse(json, classOf[JsonList])
    val summaries = Try(list.elements(classOf[JsonMap]).flatMap( row => educationItem(row) ))
    if( summaries.isFailure || summaries.get.size == 0 ) return List() else return summaries.get.toList
  }

  def employmentItem(row : JsonMap) : Option[String] = Try(f"""${row.getString("name","")}%s ${row.getString("title","")}%s (${row.getAt("start/year",classOf[String])}%s - ${row.getAt("end/year",classOf[String])})""").toOption
  def employmentSummary(json : String) : List[String] = {
    val list = JsonParser.DEFAULT.parse(json, classOf[JsonList])
    val summaries = Try(list.elements(classOf[JsonMap]).flatMap( row => employmentItem(row)))
    if( summaries.isFailure || summaries.get.size == 0 ) return List() else summaries.get.toList
  }

  def interestItem(row : JsonMap) : Option[String] = Try(f"""${row.getString("name","")}%s (${row.getString("affinity","")}%s)""").toOption
  def interestSummary(json : String) : List[String] = {
    val list = JsonParser.DEFAULT.parse(json, classOf[JsonList])
    val summaries = Try(list.elements(classOf[JsonMap]).toSeq.sorted(AffinityOrdering).flatMap( row => interestItem(row)))
    if( summaries.isFailure || summaries.get.size == 0 ) return List() else summaries.get.toList
  }

  def profileItem(row : JsonMap) : Option[String] = Try(f"""${row.get("url")}%s""").toOption
  def profileSummary(json : String) : List[String] = {
    val map = JsonParser.DEFAULT.parse(json, classOf[JsonMap])
    val summaries = Try(map.entrySet().flatMap( row => profileItem(row.getValue().asInstanceOf[JsonMap])).toSeq.sorted)
    if( summaries.isFailure || summaries.get.size == 0 ) return List() else summaries.get.toList
  }

  def urlItem(row : JsonMap) : Option[String] = Try(f"""${row.get("value")}%s""").toOption
  def urlSummary(json : String) : List[String] = {
    val list = JsonParser.DEFAULT.parse(json, classOf[JsonList])
    val summaries = Try(list.elements(classOf[JsonMap]).flatMap( row => urlItem(row)).toSeq.sorted)
    if( summaries.isFailure || summaries.get.size == 0 ) return List() else summaries.get.toList
  }

}
