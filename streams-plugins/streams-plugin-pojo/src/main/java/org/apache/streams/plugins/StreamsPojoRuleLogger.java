package org.apache.streams.plugins;

import org.jsonschema2pojo.RuleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamsPojoRuleLogger implements RuleLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamsPojoRuleLogger.class);

    @Override
    public void debug(String s) {
        LOGGER.debug(s);
    }

    @Override
    public void error(String s) {
        LOGGER.error(s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        LOGGER.error(s, throwable);
    }

    @Override
    public void info(String s) {
        LOGGER.info(s);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void trace(String s) {
        LOGGER.trace(s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        LOGGER.warn(s, throwable);
    }

    @Override
    public void warn(String s) {
        LOGGER.warn(s);
    }
}
