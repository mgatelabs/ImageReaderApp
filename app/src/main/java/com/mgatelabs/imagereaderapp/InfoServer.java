package com.mgatelabs.imagereaderapp;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgatelabs.imagereaderapp.shared.Closer;
import com.mgatelabs.imagereaderapp.shared.InfoTransfer;
import com.mgatelabs.imagereaderapp.shared.MapTransfer;
import com.mgatelabs.imagereaderapp.shared.PointTransfer;
import com.mgatelabs.imagereaderapp.shared.Sampler;
import com.mgatelabs.imagereaderapp.shared.StateTransfer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by mmgat on 9/6/2017.
 */

public class InfoServer extends NanoHTTPD {

    final ObjectMapper objectMapper;
    private final ContentResolver contentResolver;
    private final Context context;

    MapTransfer mapTransfer;
    Map<String, StateTransfer> states;

    public InfoServer(int port, ContentResolver contentResolver, Context context) throws IOException {
        super(port);
        start(25000, true);
        objectMapper = new ObjectMapper();
        states = null;
        mapTransfer = null;
        this.contentResolver = contentResolver;
        this.context = context;
    }

    private static final int SAMPLE_SIZE = 3;

    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();

        if (uri.equals("/setup")) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = session.getInputStream();

            int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));

            byte[] buffer = new byte[1024];
            int len;
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
                mapTransfer = transfer.getMap();
            } catch (IOException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + ex.getLocalizedMessage() + "\"}");
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"OK\",\"states\":\"" + states.values().size() + "\"}");

        } else if (uri.startsWith("/check/")) {
            if (states == null) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + "please run /setup first" + "\"}");
            }

            int debugIndex = -1;
            String debugName = getParameter("screen-id", "", session.getParameters());

            String stateId = session.getUri().substring(7);
            StateTransfer stateTransfer = states.get(stateId);

            if (stateTransfer == null) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + "Unknown StateId" + "\"}");
            }

            FileInputStream fileInputStream = null;
            boolean[] success = new boolean[stateTransfer.getScreenIds().size()];
            for (int i = 0; i < success.length; i++) {
                if (debugName.length() > 0 && stateTransfer.getScreenIds().get(i).equalsIgnoreCase(debugName)) {
                    debugIndex = i;
                }
                success[i] = true;
            }

            try {
                fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/framebuffer.raw");

                byte[] temp = new byte[3];
                int len;
                int remainingStates = success.length;
                boolean sampleRead = false;
                int extraRead = 0;

                for (PointTransfer pointTransfer : stateTransfer.getPoints()) {

                    if (pointTransfer.getOffset() > 0) {
                        fileInputStream.skip(pointTransfer.getOffset() + extraRead);
                        sampleRead = false;
                        extraRead = SAMPLE_SIZE;

                    }

                    if (!success[pointTransfer.getIndex()]) continue;

                    if (!sampleRead) {
                        len = fileInputStream.read(temp);
                        if (len != SAMPLE_SIZE) {
                            throw new RuntimeException("Invalid byte read");
                        }
                        sampleRead = true;
                        extraRead = 0;
                    }

                    success[pointTransfer.getIndex()] &= (within((0xff) & temp[0], (0xff) & pointTransfer.getA(), 6) && within((0xff) & temp[1], (0xff) & pointTransfer.getB(), 6) && within((0xff) & temp[2], (0xff) & pointTransfer.getC(), 6));

                    if (!success[pointTransfer.getIndex()]) {
                        remainingStates--;
                    }

                    if (remainingStates <= 0) {
                        break;
                    }

                    if (pointTransfer.getIndex() == debugIndex) {

                        final boolean w1 = within((0xff) & temp[0], (0xff) & pointTransfer.getA(), 6);
                        final boolean w2 = within((0xff) & temp[1], (0xff) & pointTransfer.getB(), 6);
                        final boolean w3 = within((0xff) & temp[2], (0xff) & pointTransfer.getC(), 6);

                        Log.d("IS", "S = " + success[pointTransfer.getIndex()] + " (" + w1 + "[" + (0xff & pointTransfer.getA()) + "]" + "," + w2 + "[" + (0xff & pointTransfer.getB()) + "]" + "," + w3 + "[" + (0xff & pointTransfer.getC()) + "]" + ")");
                    }
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

                byte[] temp = new byte[3];
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

        } else if (uri.startsWith("/download")) {
            FileInputStream fileInputStream = null;
            try {
                File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/framebuffer.raw");
                fileInputStream = new FileInputStream(path);
                return newFixedLengthResponse(Response.Status.OK, "application/octet-stream", fileInputStream, path.length());
            } catch (Exception ex) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + ex.getLocalizedMessage() + "\"}");
            }
        }else if (uri.startsWith("/head")) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/framebuffer.raw");

                byte[] temp = new byte[128];

                fileInputStream.read(temp);

                String temoBase = Base64.encodeToString(temp, 0);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"OK\",\"bytes\":'" + temoBase + "'}");

            } catch (Exception ex) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + ex.getLocalizedMessage() + "\"}");
            }
        } else if (uri.startsWith("/map")) {

            Sampler[][] grid = new Sampler[mapTransfer.getRows()][mapTransfer.getColumns()];

            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid[y].length; x++) {
                    grid[y][x] = new Sampler();
                }
            }

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/framebuffer.raw");

                byte[] temp = new byte[3];

                fileInputStream.skip(mapTransfer.getStartingOffset());

                final int rows = mapTransfer.getRows();
                final int columns = mapTransfer.getColumns();
                final int blockSize = mapTransfer.getBlockSize();
                final int blockStartSkip = mapTransfer.getPreSkip();
                final int blockEndSkip = mapTransfer.getPostSkip();

                int sampleCount = 0;

                final int middleStart = (columns / 2) - 1;
                final int middleEnd = middleStart + 2;

                for (int y = 0; y < rows; y++) {
                    for (int z = 0; z < blockSize; z += mapTransfer.getRowSkip()) {
                        for (int x = 0; x < columns; x++) {
                            for (int p = 0; p < blockSize; p++) {
                                if (p % 4 != 0) {
                                    fileInputStream.skip(3 + blockStartSkip + blockEndSkip);
                                    continue;
                                } else if (x >= middleStart && x <= middleEnd && y >= middleStart && y <= middleEnd) {
                                    fileInputStream.skip(3 + blockStartSkip + blockEndSkip);
                                    continue;
                                }
                                if (blockStartSkip > 0) fileInputStream.skip(blockStartSkip);
                                fileInputStream.read(temp);
                                grid[y][x].add((0xff & temp[0]), (0xff & temp[1]), (0xff & temp[2]));
                                if (blockEndSkip > 0) fileInputStream.skip(blockEndSkip);
                                if (x == 0 && y == 0) {
                                    sampleCount++;
                                }
                            }
                        }
                        fileInputStream.skip(mapTransfer.getNextRowOffset());
                        fileInputStream.skip((mapTransfer.getWidth() * mapTransfer.getBpp()) * (mapTransfer.getRowSkip() - 1)); // Skip every other line
                    }
                }

                final float samples = sampleCount;

                final StringBuilder sb = new StringBuilder((columns + 1) * rows);

                for (int y = 0; y < grid.length; y++) {
                    for (int x = 0; x < grid[y].length; x++) {
                        if (x >= middleStart && x <= middleEnd && y >= middleStart && y <= middleEnd) {
                            sb.append("?");
                            continue;
                        }
                        if ((grid[y][x].getR() / (samples)) > 128) {
                            sb.append("R");
                        } else if ((grid[y][x].getG() / (samples)) > 128) {
                            sb.append("G");
                        } else if ((grid[y][x].getB() / (samples)) > 128) {
                            sb.append("B");
                        } else {
                            sb.append("#");
                        }
                    }
                    sb.append("-");
                }

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"OK\",\"states\":\"" + sb.toString() + "\"}");

            } catch (Exception ex) {
                return newFixedLengthResponse(Response.Status.EXPECTATION_FAILED, "application/json", "{\"status\":\"FAIL\",\"msg\":\"" + ex.getLocalizedMessage() + "\"}");
            } finally {
                Closer.close(fileInputStream);
            }
        }
        return super.serve(session);
    }

    public String getParameter(String name, String defaultValue, Map<String, List<String>> parameters) {
        if (parameters == null || parameters.isEmpty()) return defaultValue;
        List<String> values = parameters.get(name);
        if (values == null || values.isEmpty()) return defaultValue;
        return values.get(0).trim();
    }

    public boolean within(int source, int test, int range) {
        int a = source - test;
        if (a < 0) a *= -1;
        return a <= range;
    }
}
