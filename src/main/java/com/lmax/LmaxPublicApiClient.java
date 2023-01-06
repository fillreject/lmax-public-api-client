package com.lmax;

import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketConnectOptions;

import java.util.concurrent.atomic.AtomicLong;

public class LmaxPublicApiClient
{
    private static final Vertx vertx = Vertx.vertx();
    private static final WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions()
            .setSsl(true)
            .setHost("public-data-api.london-digital.lmax.com")
            .setPort(443)
            .setURI("/v1/web-socket");
    private static final String SUBSCRIBE = """
            {
              "type": "SUBSCRIBE",
              "channels": [
                {
                  "name": "ORDER_BOOK",
                  "instruments": [
                    "bch-eur", "btc-eur"
                  ]
                },
                {
                  "name": "TICKER",
                  "instruments": [
                    "bch-eur"
                  ]
                }
              ]
            }""";
    private static final String UNSUBSCRIBE = """
            {
              "type": "UNSUBSCRIBE",
              "channels": [
                {
                  "name": "ORDER_BOOK",
                  "instruments": [
                    "bch-eur", "btc-eur"
                  ]
                },
                {
                  "name": "TICKER",
                  "instruments": [
                    "bch-eur"
                  ]
                }
              ]
            }""";

    public static void main(String[] args)
    {
        final AtomicLong counter = new AtomicLong();
        vertx.createHttpClient().webSocket(webSocketConnectOptions)
                .onFailure(LmaxPublicApiClient::logError)
                .onSuccess(webSocket -> {
                    webSocket.textMessageHandler(message -> {
                        System.out.printf("%5d | Received: %s\n", counter.incrementAndGet(), message);
                        if (counter.get() == 10)
                        {
                            System.out.println(" -----| Unsubscribing...");
                            webSocket.writeTextMessage(UNSUBSCRIBE);
                        }
                    });
                    System.out.println(" -----| Subscribing...");
                    webSocket.writeTextMessage(SUBSCRIBE);
                });
    }

    private static void logError(Throwable error)
    {
        System.out.printf("%s %s\n", error.getMessage(), error.getCause());
    }
}