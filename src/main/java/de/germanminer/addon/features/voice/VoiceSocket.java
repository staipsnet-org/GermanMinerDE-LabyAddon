package de.germanminer.addon.features.voice;

import de.germanminer.addon.features.NotificationManager;
import net.labymod.main.LabyMod;
import org.glassfish.tyrus.client.ClientManager;
import org.json.JSONObject;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

@ClientEndpoint
public class VoiceSocket {
    private static Session session;
    private static boolean expectClose;
    private static Timer reconnectTimer;

    protected static void connect() {
        if (session != null && session.isOpen())
            throw new IllegalStateException("Session is already opened");

        try {
            session = ClientManager.createClient().connectToServer(VoiceSocket.class, new URI("ws://finnlukasbck.de:9977/player/" + LabyMod.getInstance().getPlayerUUID()));
            System.out.println("Verbindung zum Server hergestellt. Warte auf Authentifizierungs-Anfrage...");
        } catch (DeploymentException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        // -- Bei Neuverbindung den Timer canceln --
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }

        NotificationManager.sendNotification("Voice-Chat", "Die Verbindung wurde erfolgreich hergestellt, du kannst jetzt sprechen.", null, null);

        System.out.println("Verbindung zum Server erfolgreich hergestellt.");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
        } catch (Exception e) {
            return;
        }

        String command = jsonObject.getString("command");
        if (command.equals("authenticate")) {
            System.out.println("Authentifizierungs-Anfrage vom Voice-Server empfangen. Sende Authentifizierungs-Schlüssel zum Server...");
            JSONObject authenticateAnswer = new JSONObject();
            authenticateAnswer.put("command", "authenticate");
            authenticateAnswer.put("key", VoiceClient.getPlayerKey());
            session.getAsyncRemote().sendText(authenticateAnswer.toString());
            System.out.println("Authentifizierungs-Schlüssel gesendet. Warte auf Antwort vom Voice-Server...");
        } else if (command.equals("authenticated")) {
            System.out.println("Authentifizierung erfolgreich!");
        }
    }

    @OnMessage
    public void onMessage(Session session, ByteBuffer buffer) {
        VoiceClient.handleAudioBuffer(buffer.array());
    }

    @OnClose
    public void onClose(Session session) {
        // WebSocket connection closes

        if (expectClose) {
            expectClose = false;
            VoiceClient.disconnected();
            VoiceSocket.session = null;
            return;
        }

        NotificationManager.sendNotification("Voice-Chat", "Die Verbindung wurde unterbrochen, versuche erneut zu verbinden...", null, null);
        reconnectTimer = new Timer();
        reconnectTimer.schedule(new TimerTask() {
            int tries;

            @Override
            public void run() {
                if (tries == 0) {
                    reconnectTimer.cancel();
                    reconnectTimer = null;

                    NotificationManager.sendNotification("Voice-Chat", "Die Verbindung wurde dauerhaft getrennt.", null, null);
                    return;
                }

                 connect();
            }
        }, 30 * 1000, 30 * 1000);
    }

    protected static boolean isConnected() {
        return session != null && session.isOpen();
    }

    public static void disconnect() {
        if(!isConnected())
            return;

        try {
            if(reconnectTimer != null) {
                reconnectTimer.cancel();
                reconnectTimer = null;
            }

            expectClose = true;
            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Disconnected"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void sendBytes(byte[] bytes) {
        session.getAsyncRemote().sendBinary(ByteBuffer.wrap(bytes));
    }

}
