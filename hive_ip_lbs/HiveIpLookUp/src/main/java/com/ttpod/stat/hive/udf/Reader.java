package com.ttpod.stat.hive.udf;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: andy
 * Date: 13-11-26
 * Time: 下午8:01
 * To change this template use File | Settings | File Templates.
 */
public final class Reader implements  Cloneable{
    private static final int DATA_SECTION_SEP_SIZE = 16;
    private static final byte[] METADATA_START_MARKER = {(byte) 0xAB,(byte) 0xCD,(byte)0xEF,'T','T','P','O','D','.','C','O','M'};
    private final Decoder decoder;
    private final Metadata metadata;
    private final ThreadBuffer threadBuffer;

    public enum  FileMode{
        M_MAP,
        MEM
    }


    public Reader(File db) throws IOException{
        this(db, FileMode.M_MAP);
    }
    public Reader(File db,FileMode fileMode) throws  IOException{
        this(new ThreadBuffer(db,fileMode),db.getName());
    }
    public Reader(ThreadBuffer buffer,String name) throws  IOException{
        this.threadBuffer = buffer;
        int start = this.findMetadataStart(name);
        Decoder metadataDecoder = null;
        try{
            metadataDecoder = new Decoder(this.threadBuffer,start);
        }
        catch (Exception e){
        }
        this.metadata = new Metadata(metadataDecoder.decode(start).getNode());
        this.decoder = new Decoder(this.threadBuffer,this.metadata.searchTreeSize + DATA_SECTION_SEP_SIZE);
    }
    private int findMetadataStart(String dbName) throws InvalidDatabaseException {
        ByteBuffer buffer = this.threadBuffer.get();
        int fileSize = buffer.capacity();
        FILE: for (int i = 0; i < fileSize - METADATA_START_MARKER.length + 1; i++) {
            for (int j = 0; j < METADATA_START_MARKER.length; j++) {
                byte b = buffer.get(fileSize - i - j - 1);
                if (b != METADATA_START_MARKER[METADATA_START_MARKER.length - j
                        - 1]) {
                    continue FILE;
                }
            }
            return fileSize - i;
        }
        throw new InvalidDatabaseException(
                "Could not find DB metadata marker in this file ("
                        + dbName + "). Is this a valid DB file?");
    }

    Metadata getMetadata() {
        return this.metadata;
    }
    public JsonNode get(InetAddress ipAddress) throws IOException {
        int pointer = this.findAddressInTree(ipAddress);
        if (pointer == 0) {
            return null;
        }
        return this.resolveDataPointer(pointer);
    }

    public Location get(String ipStr) throws  IOException{
        InetAddress addr;
        JsonNode jsonLocation;
        Location location = new Location();
        try{
             addr = InetAddress.getByName(ipStr);
        } catch (UnknownHostException e){
            return null;
        }
        jsonLocation = get(addr);

        /*{"country":"中国",
        "area":"华东",
        "province:":"上海",
        "city":"上海",
        "county":"闵行",
        "district":"",
        "street":"吴中路",
        "street_num":"3691",
        "other":"大世界网吧",
        "isp":"电信"
        },*/
        if(jsonLocation == null)
            return null;
        jsonLocation = jsonLocation.get("address") ;
        if(jsonLocation.has("country")){
            location.country = jsonLocation.get("country").asText();
        }
        if(jsonLocation.has("area")){
            location.area = jsonLocation.get("area").asText();
        }
        if(jsonLocation.has("province")){
            location.province = jsonLocation.get("province").asText();
        }
        if(jsonLocation.has("city")){
            location.city = jsonLocation.get("city").asText();
        }
        if(jsonLocation.has("county")){
            location.county = jsonLocation.get("county").asText();
        }
        if(jsonLocation.has("district")){
            location.district = jsonLocation.get("district").asText();
        }
        if(jsonLocation.has("street")){
            location.street = jsonLocation.get("street").asText();
        }
        if(jsonLocation.has("street_num")){
            location.street_num = jsonLocation.get("street_num").asText();
        }
        if(jsonLocation.has("other")){
            location.other = jsonLocation.get("other").asText();
        }
        if(jsonLocation.has("isp")){
            location.isp = jsonLocation.get("isp").asText();
        }
        return location;
    }

    private int findAddressInTree(InetAddress address)
            throws InvalidDatabaseException {
        byte[] rawAddress = address.getAddress();

        int bitLength = rawAddress.length * 8;
        int record = 0; // this.startNode(bitLength);

        for (int i = 0; i < bitLength; i++) {
            if (record >= this.metadata.nodeCount) {
                break;
            }
            int b = 0xFF & rawAddress[i / 8];
            int bit = 1 & (b >> 7 - (i % 8));
            record = this.readNode(record, bit);
        }
        if (record == this.metadata.nodeCount) {
            // record is empty
            return 0;
        } else if (record > this.metadata.nodeCount) {
            // record is a data pointer
            return record;
        }
        throw new InvalidDatabaseException("Something bad happened");
    }
    private int readNode(int nodeNumber, int index)
            throws InvalidDatabaseException {
        ByteBuffer buffer = this.threadBuffer.get();
        int baseOffset = nodeNumber * this.metadata.nodeByteSize;

        switch (this.metadata.recordSize) {
            case 24:
                buffer.position(baseOffset + index * 3);
                return Decoder.decodeInteger(buffer, 0, 3);
            case 28:
                int middle = buffer.get(baseOffset + 3);

                if (index == 0) {
                    middle = (0xF0 & middle) >>> 4;
                } else {
                    middle = 0x0F & middle;
                }
                buffer.position(baseOffset + index * 4);
                return Decoder.decodeInteger(buffer, middle, 3);
            case 32:
                buffer.position(baseOffset + index * 4);
                return Decoder.decodeInteger(buffer, 0, 4);
            default:
                throw new InvalidDatabaseException("Unknown record size: "
                        + this.metadata.recordSize);
        }
    }
    private JsonNode resolveDataPointer(int pointer) throws IOException {
        int resolved = (pointer - this.metadata.nodeCount)
                + this.metadata.searchTreeSize;

        if (resolved >= this.threadBuffer.get().capacity()) {
            throw new InvalidDatabaseException(
                    "The MaxMind DB file's search tree is corrupt: "
                            + "contains pointer larger than the database.");
        }

        // We only want the data from the decoder, not the offset where it was
        // found.
        return this.decoder.decode(resolved).getNode();
    }

  //  @Override
    public void close() throws IOException {
        this.threadBuffer.close();
    }
}
