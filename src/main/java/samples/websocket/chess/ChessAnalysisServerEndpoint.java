package samples.websocket.chess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChessAnalysisServerEndpoint extends TextWebSocketHandler {
    private static final String SESSION_ID = "sessionId";
    private static Map<Integer, ChessSession> sessions = Collections.synchronizedMap(new HashMap<Integer, ChessSession>());

    @Autowired
    public ChessAnalysisServerEndpoint() {
        if (!sessions.containsKey(1)) {
            sessions.put(1, new ChessSession());
        }
    }

    private String buildJsonData(String message) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObject jsonObject = factory.createObjectBuilder()
                .add("fen", message).build();
//        JsonObject jsonObject = Json.createObjectBuilder().add("fen", message).build();
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.write(jsonObject);
        }
        return stringWriter.toString();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession userSession) throws Exception {
        Integer sessionId = (Integer) userSession.getAttributes().get(SESSION_ID);
        if (sessionId == null) {
            sessionId = 1; // all users goes to one analysis board for now
            userSession.getAttributes().put(SESSION_ID, sessionId);
        }
        final ChessSession chessSession = sessions.get(1);
        chessSession.addUser(userSession);
        final String currentFen = chessSession.getCurrentFen();
        if (currentFen != null) {
            userSession.sendMessage(
                    new TextMessage(buildJsonData(currentFen)));
        }
    }

    @Override
    public void handleMessage(WebSocketSession userSession, WebSocketMessage<?> message) throws Exception {
        Integer sessionId = (Integer) userSession.getAttributes().get(SESSION_ID);
        final ChessSession chessSession = sessions.get(sessionId);
        synchronized (chessSession) {
            chessSession.setCurrentFen((String) message.getPayload());
            Iterator<WebSocketSession> iterator = chessSession.getUsersIterator();
            while (iterator.hasNext()) iterator.next().sendMessage(
                    new TextMessage(buildJsonData((String) message.getPayload())));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession userSession, CloseStatus closeStatus) throws Exception {
        Integer sessionId = (Integer) userSession.getAttributes().get(SESSION_ID);
        sessions.get(sessionId).removeUser(userSession);
    }
}
