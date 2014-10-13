package com.ttpod.storm.ipstat; /**
 * Created with IntelliJ IDEA.
 * User: andy
 * Date: 13-11-26
 * Time: 下午8:31
 * To change this template use File | Settings | File Templates.
 */

import java.io.IOException;

/**
 * Signals that there was an issue reading from the MaxMind DB file due to
 * unexpected data formatting. This generally suggests that the database is
 * corrupt or otherwise not in a format supported by the reader.
 */
public class InvalidDatabaseException extends IOException {

    private static final long serialVersionUID = 6161763462364823003L;

    /**
     * @param message A message describing the reason why the exception was thrown.
     */
    public InvalidDatabaseException(String message) {
        super(message);
    }

    /**
     * @param message A message describing the reason why the exception was thrown.
     * @param cause   The cause of the exception.
     */
    public InvalidDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

