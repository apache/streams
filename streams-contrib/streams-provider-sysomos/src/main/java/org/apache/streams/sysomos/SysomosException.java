package org.apache.streams.sysomos;

public class SysomosException extends Exception {

    private int errorCode = -1;

    public SysomosException() {
        // TODO Auto-generated constructor stub
    }

    public SysomosException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public SysomosException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public SysomosException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public SysomosException(String arg0, int errorCode) {
        super(arg0);
        this.errorCode = errorCode;
    }

    public SysomosException(String arg0, Throwable arg1, int errorCode) {
        super(arg0, arg1);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }


}
