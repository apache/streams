package org.apache.streams.sysomos;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 11/19/13
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SysomosResponse extends Iterator<String> {


    public int getNumResults();
    public boolean hasError();
    public String getErrorMessage();
    public String getXMLResponseString();

}
