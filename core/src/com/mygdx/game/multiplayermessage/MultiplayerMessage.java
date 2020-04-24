package com.mygdx.game.multiplayermessage;

import com.mygdx.game.InversePacman;
import com.mygdx.game.managers.NetworkManager;
import com.mygdx.game.screens.play.LobbyScreen;
import com.mygdx.game.shared.Constants;

import org.json.JSONArray;
import org.json.JSONObject;


public class MultiplayerMessage {

    private static final MultiplayerMessage instance = new MultiplayerMessage();

    private NetworkManager networkManager = InversePacman.NETWORKMANAGER;
    public static String METYPE;
    public static JSONArray DIRECTIONS = new JSONArray();
    JSONArray tempResponse = new JSONArray();
    JSONArray response = new JSONArray();

    private MultiplayerMessage() {}

    public static MultiplayerMessage getInstance(){
        return instance;
    }

    public void sendInput(JSONArray directions) {
        if (LobbyScreen.LOBBY_JOINED != null) {
            networkManager.sendInput(LobbyScreen.LOBBY_JOINED, directions);
        }
    }

    //android:networkSecurityConfig="@xml/network_security_config"

    public JSONArray getInput() {
        tempResponse = networkManager.getUpdate(LobbyScreen.LOBBY_JOINED);
        if (tempResponse != null) {
            METYPE = tempResponse.getJSONObject(0).getString("me");
            response = new JSONArray();
            for (int i = 1; i < tempResponse.length(); i++) {
                response.put(tempResponse.get(i));
            }
            return response;
        }
        return tempResponse;
    }

    public void createLobby(String nickname, String playerType) {
        networkManager.createLobby(nickname, playerType);
    }

    public String getLobby() {
        return networkManager.getLobby();
    }

    public void joinLobby(String lobbyName, String nickname, String type) {
        networkManager.joinLobby(lobbyName, nickname, type);
    }

    public JSONArray getLobbies() {
        return networkManager.getLobbies();
    }

}
