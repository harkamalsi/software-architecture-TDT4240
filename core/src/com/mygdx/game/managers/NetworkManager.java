package com.mygdx.game.managers;

import com.mygdx.game.shared.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;



/**
 * This manager is used to communicate with server.
 * Communication involves sending and receiving data.
 */

public class NetworkManager {


    private Socket socket = null;
    private String socketID;
    public static Boolean CONNECTED = false;
    private boolean fetch = false;

    private JSONArray lobbies = new JSONArray();
    private JSONObject player = new JSONObject();
    private JSONArray serverInput;
    private String lobby = null;
    private JSONArray players = new JSONArray();
    private JSONObject response;
    public Boolean connected = false;

    public NetworkManager() {
        setSocket();
    }

    public void setSocket() {
        if (socket==null){
            try {
                socket = IO.socket(Constants.HOST);
                initializeSocket();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    public Socket getSocket() {
        return socket;
    }
    private void initializeSocket(){
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketID = socket.connect().id();
                fetch = true;
                CONNECTED = true;
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args != null) {

                }
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });

        socket.connect();

        //socket.on("ping", sendPong);

        /*Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(0);
                        socket.on("ping", sendPong);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();*/


        /*socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("joinSocket", socketID);
            }
        });*/
    }

    // Single lobby
    // Fetching a single lobby
    private void setLobby(String lobby) {
        //System.out.println(lobbies);
        this.lobby = lobby;
    }
    private void fetchLobby() {
        //System.out.println("Fetch lobby is called!");

        if (fetch) {
            getSocket().emit(Constants.GET_LOBBY, socketID);

            getSocket().on(Constants.GET_LOBBY, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject) args[0];
                    setLobby(response.getString("lobby"));
                }
            });

            fetch = false;
        }
    }
    public String getLobby() {
        this.fetchLobby();
        return this.lobby;
    }

    // Creating, joining and leaving a lobby
    public void createLobby(Object ...args) {
        // args: nickname, type
        //System.out.println("Create Lobby is called!");

        final JSONObject inputs = new JSONObject();
        inputs.put("nickname", args[0]);
        inputs.put("type", args[1]);

        getSocket().emit(Constants.CREATE_LOBBY, socketID, inputs);

    }

    public void joinLobby(Object ...args) {
        // args: lobbyName, getNickname(), getPlayerType()
        if (fetch) {
            //System.out.println("Join Lobby is called!");

            final JSONObject inputs = new JSONObject();
            inputs.put("lobbyName", args[0]);
            inputs.put("nickname", args[1]);
            inputs.put("type", args[2]);

            getSocket().emit(Constants.JOIN_LOBBY, socketID, inputs);

            fetch = false;
        }

    }

    public void readyUp(String lobbyName, Boolean isReadyUp) {
        getSocket().emit(Constants.READY_UP, socketID, lobbyName, isReadyUp);
    }

    public void leaveLobby(String lobbyName) {
        // args: lobbyName
        //System.out.println("Leave Lobby is called!");
        getSocket().emit(Constants.LEAVE_LOBBY, socketID, lobbyName);
    }

    // Input sending a receiving
    public void sendInput(Object ...args) {
        /*
         args: lobbyName, direction
         float direction = Math.random() * 2 * Math.PI;
        */

        //System.out.println("Send Input is called!");

        final JSONObject inputs = new JSONObject();
        inputs.put("lobbyName", args[0]);
        inputs.put("x", args[1]);
        inputs.put("y", args[2]);

        getSocket().emit(Constants.INPUT, socketID, inputs);
    }

    private void setUpdate(JSONObject me, JSONArray others) {
        JSONArray input = new JSONArray();

        JSONObject myObject = new JSONObject();
        myObject.put("me", me.getString("type"));
        input.put(myObject);

        for(int i = 0; i < others.length(); i++) {
            input.put(others.get(i));
        }

        this.serverInput = input;
    }
    private void fetchUpdate(String lobbyName) {

        //System.out.println("Fetch update is called!");

        getSocket().on(Constants.GAME_UPDATE + "_" + lobbyName, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject)args[0];
                JSONObject me = response.getJSONObject("me");
                JSONArray others = response.getJSONArray("others");

                setUpdate(me, others);
            }
        });
    }
    public JSONArray getUpdate(String lobbyName) {
        fetchUpdate(lobbyName);
        return serverInput;
    }

    // Multiple lobbies

    // Fetching lobbies
    private void setLobbies(JSONArray lobbies) {
        //System.out.println(lobbies);
        this.lobbies = lobbies;
    }
    public JSONArray getLobbies() {
        if (fetch) {
            //System.out.println("Get lobbies is called!");

            getSocket().emit(Constants.GET_GAME_LOBBIES, socketID);

            getSocket().on(Constants.GET_GAME_LOBBIES, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray response = (JSONArray) args[0];
                    setLobbies(response);
                    //System.out.println(response);
                }
            });

            //fetch = false;
        }
        return lobbies;
    }

    // Player sockets
    public void addPlayer(final String nickname) {
        if (fetch) {
            //System.out.println("Add player is called!");

            getSocket().emit(Constants.ADD_PLAYER, socketID, nickname);

            getSocket().on(Constants.DATABASE_UPDATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject)args[0];
                    System.out.println(response);
                }
            });

            fetch = false;
        }
    }

    private void setPlayer(JSONObject player) {
        this.player = player;
    }
    private void fetchPlayerWithNickname(final String nickname) {
        if (fetch) {
            //System.out.println("Get player with nickname is called!");

            getSocket().emit(Constants.GET_PLAYER_WITH_NICKNAME, socketID, nickname);

            getSocket().on(Constants.DATABASE_UPDATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject)args[0];
                    if (response == null) {
                        JSONObject errorResponse = new JSONObject();
                        errorResponse.put("error", "No user found!");
                        System.out.println(errorResponse);
                    }
                    setPlayer(response);
                }
            });

            fetch = false;
        }

    }
    public JSONObject getPlayerWithNickname(String nickname) {
        if (fetch) {
            this.fetchPlayerWithNickname(nickname);
            return this.player;
        }
        return null;
    }

    private void fetchPlayerWithID(final String id) {
        if (fetch) {
            //System.out.println("Get player with ID is called!");

            getSocket().emit(Constants.GET_PLAYER_WITH_ID, socketID, id);

            getSocket().on(Constants.DATABASE_UPDATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject)args[0];
                    System.out.println(response);
                }
            });

            fetch = false;
        }
    }
    public JSONObject getPlayerWithID(String id) {
        if (fetch) {
            this.fetchPlayerWithID(id);
            return this.player;
        }
        return null;
    }

    public void changeNickname(final String id, final String nickname) {
        if (fetch) {
            //System.out.println("Change nickname is called!");

            getSocket().emit(Constants.CHANGE_NICKNAME, socketID, id, nickname);

            fetch = false;
        }
    }

    public void changeSkinType(final String id, final int skinType) {
       if (fetch) {
           //System.out.println("Change skin type is called!");

           getSocket().emit(Constants.CHANGE_SKINTYPE, socketID, id, skinType);

           fetch = false;
       }
    }

    // Score sockets
    public void updateSingleplayerScore(Object ...args) {
        // args: id, spScore
        if (fetch) {
            //System.out.println("Update single player score is called!");

            final JSONObject inputs = new JSONObject();
            inputs.put("id", args[0]);
            inputs.put("spScore", args[1]);
            inputs.put("mpScore", -1);

            getSocket().emit(Constants.UPDATE_HIGHSCORE, socketID, inputs);

            getSocket().on(Constants.DATABASE_UPDATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject)args[0];
                    System.out.println(response);
                }
            });

            fetch = false;
        }
    }

    public void updateMultiplayerScore(Object ...args) {
        // args: id, mpScore
        if (fetch) {
            //System.out.println("Update single player score is called!");

            final JSONObject inputs = new JSONObject();
            inputs.put("id", args[0]);
            inputs.put("spScore", -1);
            inputs.put("mpScore", args[1]);

            getSocket().emit(Constants.UPDATE_HIGHSCORE, socketID, inputs);

            getSocket().on(Constants.DATABASE_UPDATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject)args[0];
                    System.out.println(response);
                }
            });

            fetch = false;
        }
    }

    private Emitter.Listener sendPong = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            String ping;
            try {
                ping = data.getString("ping");
            } catch (JSONException e) {
                return;
            }
            if(ping.equals("1")){
                getSocket().emit("pong", "pong");
            }
            //System.out.println("SOCKETPING" + " RECEIVED PING! ");
        }
    };

    public void setPlayers(JSONArray players) {
        this.players = players;
    }

    public JSONArray getPlayers() {
        return players;
    }

    public void fetchAllPlayers() {
        if (fetch) {
            getSocket().emit(Constants.GET_ALL_PLAYERS, socketID);

            getSocket().on(Constants.GET_ALL_PLAYERS, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray response = (JSONArray) args[0];
                    System.out.println("Players response: " + response);
                    setPlayers(response);
                }
            });

            //fetch = false;
        }
    }

}
