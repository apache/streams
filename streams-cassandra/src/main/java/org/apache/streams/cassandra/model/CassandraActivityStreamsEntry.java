package org.apache.streams.cassandra.model;

import org.apache.rave.model.ActivityStreamsEntry;
import org.apache.rave.model.ActivityStreamsMediaLink;
import org.apache.rave.model.ActivityStreamsObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

@Entity
public class CassandraActivityStreamsEntry implements ActivityStreamsEntry, Serializable{
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(CassandraActivityStreamsEntry.class.getName());

    @Id
    private String id;

    @Column
    private ActivityStreamsObject actor;

    @Column
    private String content;

    @Column
    private ActivityStreamsObject generator;

    @Column
    private ActivityStreamsMediaLink icon;

    @Column
    private ActivityStreamsObject object;

    @Column
    private ActivityStreamsObject provider;

    @Column
    private ActivityStreamsObject target;

    @Column
    private String title;

    @Column
    private String verb;

    //The user who verb'd this activity
    @Column
    private String userId;

    //If this activity was generated as part of a group, this indicates the group's id
    @Column
    private String groupId;

    //The id of the application that published this activity
    @Column
    private String appId;

    @Column
    private String bcc;

    @Column
    private String bto;

    @Column
    private String cc;

    @Column
    private String context;

    @Column
    private String dc;

    private Date endTime;

    @Column
    private String geojson;

    @Column
    private String inReplyTo;

    @Column
    private String ld;

    @Column
    private String links;

    @Column
    private String location;

    @Column
    private String mood;

    @Column
    private String odata;

    @Column
    private String opengraph;

    @Column
    private String priority;

    @Column
    private String rating;

    @Column
    private String result;

    @Column
    private String schema_org;

    @Column
    private String source;

    @Column
    private Date startTime;

    @Column
    private String tags;

    @Column
    private String to;

    @Column
    private Date published;

    @Column
    private Date updated;

    @Column
    private String url;

    @Column
    private String objectType;

    private Map openSocial;

    private Map extensions;

    /**
     * Create a new empty DeserializableActivityEntry
     */
    public CassandraActivityStreamsEntry() {
    }

    /** {@inheritDoc} */

    public Date getPublished() {
        return published;
    }

    /** {@inheritDoc} */
    public void setPublished(Date published) {
        this.published = published;
    }

    public Date getUpdated(){
        return updated;
    }

    public void setUpdated(Date updated){
        this.updated=updated;
    }

    public String getUrl(){
        return this.url;
    }

    public void setUrl(String url){
        this.url=url;
    }

    public String getObjectType(){
        return this.objectType;
    }

    public void setObjectType(String objectType){
        this.objectType=objectType;
    }

    /** {@inheritDoc} */

    public Map getOpenSocial() {
        return openSocial;
    }

    /**
     * {@inheritDoc}
     */
    public void setOpenSocial(Map openSocial) {

        this.openSocial = openSocial;
    }

    /** {@inheritDoc} */

    public Map getExtensions() {
        return extensions;
    }

    /** {@inheritDoc} */
    public void setExtensions(Map extensions) {


        this.extensions = extensions;
    }

    public ActivityStreamsObject getActor() {
        return actor;
    }

    /** {@inheritDoc} */
    public void setActor(ActivityStreamsObject actor) {
        this.actor = actor;
    }

    /** {@inheritDoc} */
    public String getContent() {
        return content;
    }

    /** {@inheritDoc} */
    public void setContent(String content) {
        this.content = content;
    }


    /** {@inheritDoc} */
    public ActivityStreamsObject getGenerator() {
        return generator;
    }

    /** {@inheritDoc} */
    public void setGenerator(ActivityStreamsObject generator) {
        this.generator = generator;
    }

    /** {@inheritDoc} */
    public ActivityStreamsMediaLink getIcon() {
        return icon;
    }

    /** {@inheritDoc} */
    public void setIcon(ActivityStreamsMediaLink icon) {
        this.icon = icon;
    }

    /** {@inheritDoc} */

    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    public void setId(String id) {
        this.id = id;
    }

    /** {@inheritDoc} */
    public ActivityStreamsObject getObject() {
        return object;
    }

    /** {@inheritDoc} */
    public void setObject(ActivityStreamsObject object) {
        this.object = object;
    }



    /** {@inheritDoc} */
    public ActivityStreamsObject getProvider() {
        return provider;
    }

    /** {@inheritDoc} */
    public void setProvider(ActivityStreamsObject provider) {
        this.provider = provider;
    }

    /** {@inheritDoc} */
    public ActivityStreamsObject getTarget() {
        return target;
    }

    /** {@inheritDoc} */
    public void setTarget(ActivityStreamsObject target) {
        this.target = target;
    }

    /** {@inheritDoc} */

    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    public void setTitle(String title) {
        this.title = title;
    }


    /** {@inheritDoc} */

    public String getVerb() {
        return verb;
    }

    /** {@inheritDoc} */
    public void setVerb(String verb) {
        this.verb = verb;
    }

    /** {@inheritDoc} */
    public String getUserId() {
        return userId;
    }

    /** {@inheritDoc} */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** {@inheritDoc} */
    public String getGroupId() {
        return groupId;
    }

    /** {@inheritDoc} */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    /** {@inheritDoc} */
    public String getAppId() {
        return appId;
    }

    /** {@inheritDoc} */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /** {@inheritDoc} */
    public String getBcc() {
        return bcc;
    }

    /** {@inheritDoc} */
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    /** {@inheritDoc} */
    public String getBto() {
        return bto;
    }

    /** {@inheritDoc} */
    public void setBto(String bto) {
        this.bto = bto;
    }

    /** {@inheritDoc} */
    public String getCc() {
        return cc;
    }

    /** {@inheritDoc} */
    public void setCc(String cc) {
        this.cc = cc;
    }

    /** {@inheritDoc} */
    public String getContext() {
        return context;
    }

    /** {@inheritDoc} */
    public void setContext(String context) {
        this.context = context;
    }

    /** {@inheritDoc} */
    public String getDc() {
        return dc;
    }

    /** {@inheritDoc} */
    public void setDc(String dc) {
        this.dc = dc;
    }

    /** {@inheritDoc} */
    public Date getEndTime() {
        return endTime;
    }

    /** {@inheritDoc} */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /** {@inheritDoc} */
    public String getGeojson() {
        return geojson;
    }

    /** {@inheritDoc} */
    public void setGeojson(String geojson) {
        this.geojson = geojson;
    }

    /** {@inheritDoc} */
    public String getInReplyTo() {
        return inReplyTo;
    }

    /** {@inheritDoc} */
    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    /** {@inheritDoc} */
    public String getLd() {
        return ld;
    }

    /** {@inheritDoc} */
    public void setLd(String ld) {
        this.ld = ld;
    }

    /** {@inheritDoc} */
    public String getLinks() {
        return links;
    }

    /** {@inheritDoc} */
    public void setLinks(String links) {
        this.links = links;
    }

    /** {@inheritDoc} */
    public String getLocation() {
        return location;
    }

    /** {@inheritDoc} */
    public void setLocation(String location) {
        this.location = location;
    }

    /** {@inheritDoc} */
    public String getMood() {
        return mood;
    }

    /** {@inheritDoc} */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /** {@inheritDoc} */
    public String getOdata() {
        return odata;
    }

    /** {@inheritDoc} */
    public void setOdata(String odata) {
        this.odata = odata;
    }

    /** {@inheritDoc} */
    public String getOpengraph() {
        return opengraph;
    }

    /** {@inheritDoc} */
    public void setOpengraph(String opengraph) {
        this.opengraph = opengraph;
    }

    /** {@inheritDoc} */
    public String getPriority() {
        return priority;
    }

    /** {@inheritDoc} */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /** {@inheritDoc} */
    public String getRating() {
        return rating;
    }

    /** {@inheritDoc} */
    public void setRating(String rating) {
        this.rating = rating;
    }

    /** {@inheritDoc} */
    public String getResult() {
        return result;
    }

    /** {@inheritDoc} */
    public void setResult(String result) {
        this.result = result;
    }

    /** {@inheritDoc} */
    public String getSchema_org() {
        return schema_org;
    }

    /** {@inheritDoc} */
    public void setSchema_org(String schema_org) {
        this.schema_org = schema_org;
    }

    /** {@inheritDoc} */
    public String getSource() {
        return source;
    }

    /** {@inheritDoc} */
    public void setSource(String source) {
        this.source = source;
    }

    /** {@inheritDoc} */
    public Date getStartTime() {
        return startTime;
    }

    /** {@inheritDoc} */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /** {@inheritDoc} */
    public String getTags() {
        return tags;
    }

    /** {@inheritDoc} */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /** {@inheritDoc} */
    public String getTo() {
        return to;
    }

    /** {@inheritDoc} */
    public void setTo(String to) {
        this.to = to;
    }



    /**
     * Sorts ActivityEntries in ascending order based on publish date.
     *
     * @param that
     *            is the DeserializableActivityEntry to compare to this DeserializableActivityEntry
     *
     * @return int represents how the ActivityEntries compare
     */

    public int compareTo(ActivityStreamsEntry that) {
        if (this.getPublished() == null && that.getPublished() == null) {
            return 0; // both are null, equal
        } else if (this.getPublished() == null) {
            return -1; // this is null, comes before real date
        } else if (that.getPublished() == null) {
            return 1; // that is null, this comes after
        } else { // compare publish dates in lexicographical order
            return this.getPublished().compareTo(that.getPublished());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraActivityStreamsEntry that = (CassandraActivityStreamsEntry) o;

        if (actor != null ? !actor.equals(that.actor) : that.actor != null) return false;
        if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
        if (bcc != null ? !bcc.equals(that.bcc) : that.bcc != null) return false;
        if (bto != null ? !bto.equals(that.bto) : that.bto != null) return false;
        if (cc != null ? !cc.equals(that.cc) : that.cc != null) return false;
        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (dc != null ? !dc.equals(that.dc) : that.dc != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (extensions != null ? !extensions.equals(that.extensions) : that.extensions != null) return false;
        if (generator != null ? !generator.equals(that.generator) : that.generator != null) return false;
        if (geojson != null ? !geojson.equals(that.geojson) : that.geojson != null) return false;
        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
        if (icon != null ? !icon.equals(that.icon) : that.icon != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (inReplyTo != null ? !inReplyTo.equals(that.inReplyTo) : that.inReplyTo != null) return false;
        if (ld != null ? !ld.equals(that.ld) : that.ld != null) return false;
        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (mood != null ? !mood.equals(that.mood) : that.mood != null) return false;
        if (object != null ? !object.equals(that.object) : that.object != null) return false;
        if (objectType != null ? !objectType.equals(that.objectType) : that.objectType != null) return false;
        if (odata != null ? !odata.equals(that.odata) : that.odata != null) return false;
        if (openSocial != null ? !openSocial.equals(that.openSocial) : that.openSocial != null) return false;
        if (opengraph != null ? !opengraph.equals(that.opengraph) : that.opengraph != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (provider != null ? !provider.equals(that.provider) : that.provider != null) return false;
        if (published != null ? !published.equals(that.published) : that.published != null) return false;
        if (rating != null ? !rating.equals(that.rating) : that.rating != null) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        if (schema_org != null ? !schema_org.equals(that.schema_org) : that.schema_org != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;
        if (updated != null ? !updated.equals(that.updated) : that.updated != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (verb != null ? !verb.equals(that.verb) : that.verb != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = id != null ? id.hashCode() : 0;
        result1 = 31 * result1 + (actor != null ? actor.hashCode() : 0);
        result1 = 31 * result1 + (content != null ? content.hashCode() : 0);
        result1 = 31 * result1 + (generator != null ? generator.hashCode() : 0);
        result1 = 31 * result1 + (icon != null ? icon.hashCode() : 0);
        result1 = 31 * result1 + (object != null ? object.hashCode() : 0);
        result1 = 31 * result1 + (provider != null ? provider.hashCode() : 0);
        result1 = 31 * result1 + (target != null ? target.hashCode() : 0);
        result1 = 31 * result1 + (title != null ? title.hashCode() : 0);
        result1 = 31 * result1 + (verb != null ? verb.hashCode() : 0);
        result1 = 31 * result1 + (userId != null ? userId.hashCode() : 0);
        result1 = 31 * result1 + (groupId != null ? groupId.hashCode() : 0);
        result1 = 31 * result1 + (appId != null ? appId.hashCode() : 0);
        result1 = 31 * result1 + (bcc != null ? bcc.hashCode() : 0);
        result1 = 31 * result1 + (bto != null ? bto.hashCode() : 0);
        result1 = 31 * result1 + (cc != null ? cc.hashCode() : 0);
        result1 = 31 * result1 + (context != null ? context.hashCode() : 0);
        result1 = 31 * result1 + (dc != null ? dc.hashCode() : 0);
        result1 = 31 * result1 + (endTime != null ? endTime.hashCode() : 0);
        result1 = 31 * result1 + (geojson != null ? geojson.hashCode() : 0);
        result1 = 31 * result1 + (inReplyTo != null ? inReplyTo.hashCode() : 0);
        result1 = 31 * result1 + (ld != null ? ld.hashCode() : 0);
        result1 = 31 * result1 + (links != null ? links.hashCode() : 0);
        result1 = 31 * result1 + (location != null ? location.hashCode() : 0);
        result1 = 31 * result1 + (mood != null ? mood.hashCode() : 0);
        result1 = 31 * result1 + (odata != null ? odata.hashCode() : 0);
        result1 = 31 * result1 + (opengraph != null ? opengraph.hashCode() : 0);
        result1 = 31 * result1 + (priority != null ? priority.hashCode() : 0);
        result1 = 31 * result1 + (rating != null ? rating.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (schema_org != null ? schema_org.hashCode() : 0);
        result1 = 31 * result1 + (source != null ? source.hashCode() : 0);
        result1 = 31 * result1 + (startTime != null ? startTime.hashCode() : 0);
        result1 = 31 * result1 + (tags != null ? tags.hashCode() : 0);
        result1 = 31 * result1 + (to != null ? to.hashCode() : 0);
        result1 = 31 * result1 + (published != null ? published.hashCode() : 0);
        result1 = 31 * result1 + (updated != null ? updated.hashCode() : 0);
        result1 = 31 * result1 + (url != null ? url.hashCode() : 0);
        result1 = 31 * result1 + (objectType != null ? objectType.hashCode() : 0);
        result1 = 31 * result1 + (openSocial != null ? openSocial.hashCode() : 0);
        result1 = 31 * result1 + (extensions != null ? extensions.hashCode() : 0);
        return result1;
    }
}
