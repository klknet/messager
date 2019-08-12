package com.konglk.ims.ws;

import com.konglk.model.BinaryRequest;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.nio.ByteBuffer;

/**
 * Created by konglk on 2019/8/8.
 */
public class BinaryStreamDecoder implements Decoder.Binary<BinaryRequest> {
    @Override
    public BinaryRequest decode(ByteBuffer bytes) throws DecodeException {
        return null;
    }

    @Override
    public boolean willDecode(ByteBuffer bytes) {
        long aLong = bytes.getLong();
        return false;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
