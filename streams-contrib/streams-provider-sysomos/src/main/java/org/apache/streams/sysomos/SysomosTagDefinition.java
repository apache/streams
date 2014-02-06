package org.apache.streams.sysomos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 5/1/13
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SysomosTagDefinition {

    private String tagName;
    private String displayName;
    private List<String> queries;

    public SysomosTagDefinition(String tagName, String displayName) {
        this.tagName = tagName;
        this.displayName = displayName;
        this.queries = new ArrayList<String>();
    }

    public String getTagName() {
        return this.tagName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getQueries() {
        List<String> result = new ArrayList<String>();
        result.addAll(this.queries);
        return result;
    }

    public void addQuery(String query) {
        this.queries.add(query);
    }

    public boolean hasTagName(String tagName) {
        return this.tagName.equals(tagName);
    }

    public boolean hasQuery(String query) {
        return this.queries.contains(query);
    }

    public boolean hasDisplayName(String displayName) {
        return this.displayName.equals(displayName);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof SysomosTagDefinition)) {
            return false;
        }
        SysomosTagDefinition that = (SysomosTagDefinition) o;
        if(!this.tagName.equals(that.tagName)) {
            return false;
        }
        if(!this.displayName.equals(that.displayName)) {
            return false;
        }
        if(this.queries.size() != that.queries.size()) {
            return false;
        }
        for(int i=0; i < this.queries.size(); ++i) {
            if(!that.queries.contains(this.queries.get(i))) {
                return false;
            }
        }
        return true;
    }
}
