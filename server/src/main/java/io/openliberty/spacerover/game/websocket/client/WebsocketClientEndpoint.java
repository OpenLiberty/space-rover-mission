/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.spacerover.game.websocket.client;


import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.util.logging.Level;
import java.util.logging.Logger;


@ClientEndpoint
public class WebsocketClientEndpoint {

    private static final Logger LOGGER = Logger.getLogger(WebsocketClientEndpoint.class.getName());
    Session userSession = null;
    private io.openliberty.spacerover.game.websocket.client.MessageHandler messageHandler;

    public WebsocketClientEndpoint(URI endpointURI) throws IOException {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException e) {
            throw new IOException(e);
        }
    }
    
    public WebsocketClientEndpoint(URI endpointURI,io.openliberty.spacerover.game.websocket.client.MessageHandler handler) throws IOException
    {
        try {
        	this.messageHandler = handler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException e) {
            throw new IOException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
    	try {
			this.disconnect();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "failed to disconnect during onClose()");
		} 
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
        else
        {
        	LOGGER.log(Level.WARNING, "message handler is null");
        }
    }

   @OnMessage
   public void onMessage(ByteBuffer bytes) {
	   LOGGER.log(Level.WARNING, "got a byte buffer message");
    }


    /**
     * Send a message.
     *
     * @param message
     * @throws IOException 
     */
    public void sendMessage(String message) throws IOException {
        this.userSession.getAsyncRemote().sendText(message);
        LOGGER.info("Sent Message "+ message);
    }
    

    
    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(io.openliberty.spacerover.game.websocket.client.MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void disconnect() throws IOException
    {
    	if(this.userSession.isOpen())
    	{
    		this.userSession.close();
    	}
    }
}
