package com.serverless;

import java.util.*;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

@SuppressWarnings("UnstableApiUsage")
public class Handler implements RequestHandler<ViewSpotsRequest, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(Handler.class);

    @Override
    public ApiGatewayResponse handleRequest(ViewSpotsRequest viewSpotsRequest, Context context) {
//		LOG.info("received: {}", viewSpotsRequest);

        // long start = System.currentTimeMillis();

        var objectMapper = new ObjectMapper();
        Mesh mesh;

        try {
            mesh = objectMapper.readValue(viewSpotsRequest.meshAsJsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ElementWithValue[] viewSpotCandidates = Streams
                .zip(
                        Arrays.stream(mesh.elements),
                        Arrays.stream(mesh.values),
                        (ElementWithValue::new)
                )
                .sorted(Comparator.comparingDouble(ElementWithValue::getValue).reversed())
                .toArray(ElementWithValue[]::new);

        var viewSpots = new ArrayList<ElementWithValue>(Collections.emptyList());
        var higherNodes = new HashSet<Integer>();

        var i = 0;
        while (viewSpots.size() < viewSpotsRequest.requestedNumberOfViewSpots) {
            var element = viewSpotCandidates[i];

            if (Sets.intersection(element.getNodes(), higherNodes).isEmpty()) {
                higherNodes.addAll(element.getNodes());
                viewSpots.add(element);
            }

            ++i;

            if (i == viewSpotCandidates.length) {
                LOG.warn("More view spots requested than available.");
                break;
            }
        }

        // long time = System.currentTimeMillis() - start;
        // System.out.println("took: " + time);

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(Arrays.toString(viewSpots.stream().flatMap(e ->
                        Stream.of("\n{element_id: " + e.getId() + ", value: " + e.getValue() + "}")).toArray()))
                .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .build();
    }
}
