package com.ttpod.loadIp;

import java.io.IOException;

/**
 * Created by wolf on 14-5-11.
 */
public interface ReadInterface {

    public boolean init(Config conf);
    public Location read();

    public boolean hasNext();
    public void close() throws IOException;
}
