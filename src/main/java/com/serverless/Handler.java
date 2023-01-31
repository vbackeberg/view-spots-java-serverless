package com.serverless;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;

@SuppressWarnings("UnstableApiUsage")
public class Handler implements RequestStreamHandler {

    private static final Logger LOG = LogManager.getLogger(Handler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        // long start = System.currentTimeMillis();

        ViewSpotsRequest viewSpotsRequest;

        try {
            viewSpotsRequest = objectMapper.readValue(inputStream, ViewSpotsRequest.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        ElementWithValue[] viewSpotCandidates = Streams
                .zip(
                        Arrays.stream(viewSpotsRequest.mesh.elements),
                        Arrays.stream(viewSpotsRequest.mesh.values),
                        (ElementWithValue::new)
                )
                .sorted(Comparator.comparingDouble(ElementWithValue::getValue).reversed())
                .toArray(ElementWithValue[]::new);

        var viewSpots = new ArrayList<ElementWithValue>(Collections.emptyList());
        var higherNodes = new HashSet<Integer>();

        var i = 0;
        while (viewSpots.size() < viewSpotsRequest.number) {
            var element = viewSpotCandidates[i];

            if (Sets.intersection(element.getNodes(), higherNodes).isEmpty()) {
                higherNodes.addAll(element.getNodes());
                viewSpots.add(element);
            }

            ++i;

            if (i == viewSpotCandidates.length) {
                LOG.error("More view spots requested than available.");
                break;
            }
        }

        try {
            objectMapper.writeValue(outputStream, viewSpots);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // long time = System.currentTimeMillis() - start;
        // System.out.println("took: " + time);
    }
}
