/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.examples.greeting;

import java.io.IOException;
import java.util.logging.LogManager;

import io.helidon.microprofile.server.Server;

/**
 * Service method simulating trigger of main method of the server.
 */
public final class Service {

    /**
     * Cannot be instantiated.
     */
    private Service() { }

    /**
     * Application main entry point.
     * @param args command line arguments
     * @throws IOException if there are problems reading logging properties
     */
    public static void main(final String[] args) throws IOException {
        setupLogging();

        Server server = startServer();
        System.out.println("**************************************************************************************************************");
        System.out.println("*                                                                                                            *");
        System.out.println("*                                                                                                            *");
        System.out.println("*                               Welcome to a Helidon Sample Service                                          *");
        System.out.println("*                                                                                                            *");
        System.out.println("*                                                                                                            *");
        System.out.println("**************************************************************************************************************");
        System.out.println("http://localhost:" + server.port() + "/meet - reveals the default meeting representation");
        System.out.println("http://localhost:" + server.port() + "/meet/{name} - meets and greets a person with the default representation");
        System.out.println("http://localhost:" + server.port() + "/meet-representation/{representation} - sets a new default meet and greet representation");
        System.out.println("http://localhost:" + server.port() + "/greetings - a list of greetings and their origin");
        System.out.println("http://localhost:" + server.port() + "/greetings/{representation} - a concrete representation and origin information");
        System.out.println("**************************************************************************************************************");
    }

    /**
     * Start the server.
     * @return the created {@link Server} instance
     */
    static Server startServer() {
        // Server will automatically pick up configuration from
        // microprofile-config.properties
        // and Application classes annotated as @ApplicationScoped
        return Server.create().start();
    }

    /**
     * Configure logging from logging.properties file.
     */
    private static void setupLogging() throws IOException {
        // load logging configuration
        LogManager.getLogManager().readConfiguration(
                Service.class.getResourceAsStream("/logging.properties"));
    }
}
