package com.mgatelabs.imagereaderapp;

import android.os.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgatelabs.imagereaderapp.shared.Closer;
import com.mgatelabs.imagereaderapp.shared.InfoTransfer;
import com.mgatelabs.imagereaderapp.shared.PointTransfer;
import com.mgatelabs.imagereaderapp.shared.StateTransfer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by mmgat on 9/6/2017.
 */

public class InfoServer extends NanoHTTPD {

    final ObjectMapper objectMapper;

    Map<String, StateTransfer> states;

    public InfoServer (int port) throws IOException {
        super(port);
        start(5000, true);
        objectMapper = new ObjectMapper();
        states = null;
    }

    @Override
    public Response serve (IHTTPSession session) {

        String uri = session.getUri();

        if (uri.equals("/setup")) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream           inputStream           = session.getInputStream();

            int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));

            byte[] buffer = new byte[1024];
            int    len;
            try {
                while (byteArrayOutputStream.size() < contentLength) {
                    len = inputStream.read(buffer);
                    if (len <= 0) {
                        continue;
                    }
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                InfoTransfer transfer = objectMapper.readValue(byteArrayOutputStream.toByteArray(), InfoTransfer.class);
                states = transfer.getStates();
            } catch (IOException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + ex.getLocalizedMessage() + "\"}");
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"OK\",\"states\":\"" + states.values().size() + "\"}");

        } else if (uri.startsWith("/check/")) {
            if (states == null) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" +"please run /setup first" + "\"}");
            }

            String stateId = session.getUri().substring(7);
            StateTransfer stateTransfer = states.get(stateId);

            if (stateTransfer == null) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + "Unknown StateId" + "\"}");
            }

            FileInputStream fileInputStream = null;
            boolean [] success = new boolean[stateTransfer.getScreenIds().size()];
            for (int i = 0; i < success.length; i++) {
                success[i] = true;
            }

            try {
                fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/framebuffer.raw");

                byte [] temp = new byte [3];
                int len;
                for (PointTransfer pointTransfer: stateTransfer.getPoints()) {

                    if (pointTransfer.getOffset() > 0) {
                        fileInputStream.skip(pointTransfer.getOffset());
                        len = fileInputStream.read(temp);
                        if (len != 3) {
                            throw new RuntimeException("Invalid byte read");
                        }
                    }
                    success[pointTransfer.getIndex()] &= (within(temp[0], pointTransfer.getA(), 6) && within(temp[1], pointTransfer.getB(), 6) && within(temp[2], pointTransfer.getC(), 6));
                }
            } catch (Exception ex) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + ex.getLocalizedMessage() + "\"}");
            } finally {
                Closer.close(fileInputStream);
            }

            StringBuilder successList = new StringBuilder();
            int i = 0;
            for (int j = 0; j < success.length; j++) {
                if (success[j]) {
                    if (i > 0) successList.append(",");
                    successList.append("\"").append(stateTransfer.getScreenIds().get(j)).append("\"");
                    i++;
                }
            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"OK\",\"screens\":[" + successList + "]}");
        } else if (uri.startsWith("/pixel/")) {
            String pixelValue = session.getUri().substring(7);
            int offset = Integer.parseInt(pixelValue);

            FileInputStream fileInputStream = null;

            try {
                fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/framebuffer.raw");

                byte [] temp = new byte [3];
                int len;

                fileInputStream.skip(offset);
                len = fileInputStream.read(temp);
                if (len != 3) {
                    throw new RuntimeException("Invalid byte read");
                }

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"OK\",\"pixels\":[" + (0xff & temp[0]) + "," + (0xff & temp[1]) + "," + (0xff & temp[2]) + "]}");

            } catch (Exception ex) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + ex.getLocalizedMessage() + "\"}");
            } finally {
                Closer.close(fileInputStream);
            }

        }
        return super.serve(session);
    }

    public boolean within(int source, int test, int range) {
        int a = source - test;
        if (a < 0) a *= -1;
        return  a <= range;
    }
}
