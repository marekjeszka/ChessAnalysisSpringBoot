package samples.websocket.chess;

import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChessSession {
    private Set<WebSocketSession> analysisUsers = new HashSet<>();
    private String currentFen;

    public void addUser(WebSocketSession session) {
        analysisUsers.add(session);
    }

    public Iterator<WebSocketSession> getUsersIterator() {
        return analysisUsers.iterator();
    }

    public boolean removeUser(WebSocketSession session) {
        return analysisUsers.remove(session);
    }

    public String getCurrentFen() {
        return currentFen;
    }

    public void setCurrentFen(String currentFen) {
        this.currentFen = currentFen;
    }
}
