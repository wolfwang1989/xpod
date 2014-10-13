package com.ttpod.stat.hive.udf;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: andy
 * Date: 13-11-26
 * Time: 下午7:30
 * To change this template use File | Settings | File Templates.
 */
final class Metadata {
   private final BigInteger buildEpoch;
    final String databaseType;
    final JsonNode description;
    final int ipVersion;
    final int nodeCount;
    final int recordSize;
    final int nodeByteSize;
    final int searchTreeSize;

    public Metadata(JsonNode metadata){
        this.buildEpoch = metadata.get("build_epoch").bigIntegerValue();
        this.databaseType = metadata.get("database_type") .asText();
        this.description = metadata.get("description") ;
        this.ipVersion = metadata.get("ip_version") .asInt();
        this.nodeCount = metadata.get("node_count").asInt();
        this.recordSize = metadata.get("record_size").asInt();
        this.nodeByteSize = (this.recordSize / 8) * 2;
        this.searchTreeSize = this.nodeCount * this.nodeByteSize;
    }
}
