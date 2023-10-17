/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2017 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.almasb.fxglgames.pong;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.collection.Array;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.net.*;
import com.almasb.fxgl.ui.UI;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.almasb.fxgl.dsl.FXGL.*;


/**
 * Server for Dungen Crawler
 *
 *
 * @author Leo Waters
 */
public class DungenServer extends GameApplication implements MessageHandler<String> {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Dungen Crawler Server");
        settings.setVersion("1.0");
        settings.setFontUI("pong.ttf");
        settings.setApplicationMode(ApplicationMode.DEBUG);
    }

    private Entity[] players= new Entity[4];
    private boolean[] ConnectionIDs={false,false,false,false};
    private Server<String> server;


    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("player1Health", 100);
        vars.put("player2Health", 100);
        vars.put("player3Health", 100);
        vars.put("player4Health", 100);
    }

    @Override
    protected void initGame() {
        Writers.INSTANCE.addTCPWriter(String.class, outputStream -> new MessageWriterS(outputStream));
        Readers.INSTANCE.addTCPReader(String.class, in -> new MessageReaderS(in));

        server = getNetService().newTCPServer(55555, new ServerConfig<>(String.class));

        server.setOnConnected(connection -> {
            System.out.println("Player Connected!");

            boolean AssignedAPlayerID=false;
            for (int i = 0; i < 4; i++) {
                if(!ConnectionIDs[i]){
                    AssignedAPlayerID=ConnectionIDs[i]=true;
                    connection.getLocalSessionData().setValue("ID",i);
                    break;
                }
            }
            if(!AssignedAPlayerID){
                connection.getLocalSessionData().setValue("ID",-1);
            }

            connection.addMessageHandlerFX(this);
        });


        getGameWorld().addEntityFactory(new DungenFactory());
        getGameScene().setBackgroundColor(Color.rgb(0, 0, 5));

        initScreenBounds();
        initGameObjects();

        var t = new Thread(server.startTask()::run);
        t.setDaemon(true);
        t.start();
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 0);


    }
    UI ui;
    @Override
    protected void initUI() {
        MainUIController controller = new MainUIController();
        ui= getAssetLoader().loadUI("main.fxml", controller);
        getGameScene().addUI(ui);
        UpdateUI();
    }

    void UpdateUI(){

        for(int i=0;i<players.length;i++)
        {
            //add possesion detection coe
            //players[i].getComponent()
            boolean isPossed= ConnectionIDs[i];
            ((MainUIController)ui.getController()).ShowPlayerPossessionState(i,isPossed);
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!server.getConnections().isEmpty()) {

            UpdateUI();
            BroadCastPlayerUpdates();
        }
    }

    void BroadCastPlayerUpdates(){

        for(int i=0;i<players.length;i++)//send updates for each players data
        {

            var message = "PLAYER_DATA,"+i+"," + players[i].getX() + "," + players[i].getY()+",IDLE,|";
            server.broadcast(message);
        }

    }


    private void initScreenBounds() {
        //Entity walls = entityBuilder()
        //        .type(EntityType.WALL)
        //        .collidable()
        //        .buildScreenBounds(150);

        //getGameWorld().addEntity(walls);
    }

    private void initGameObjects() {

        for(int i=0;i<players.length;i++)
        {
         players[i]=spawn("Player", new SpawnData(100*i, 100));
        }

    }


    @Override
    public void onReceive(Connection<String> connection, String message) {

        String[] Commands=message.split("\\|");

        int PlayerID=connection.getLocalSessionData().getInt("ID");


        if(PlayerID==-1){
            for (int i = 0; i < 4; i++) {
                if(!ConnectionIDs[i]){
                    connection.getLocalSessionData().setValue("ID",i);
                    break;
                }
            }

        }

        for (int i = 0; i < Commands.length; i++) {
            var Data=Commands[i];

            StringBuilder ArgDebugString= new StringBuilder(" Args: ");

            var command="";
            List<String> CommandArgs = new ArrayList<String>();
            if(Data.contains((","))){

                var args=Data.split(",");
                command=args[0];
                for (int a = 1; a < args.length; a++){//skip first as is command name
                    ArgDebugString.append(args[a]).append(", ");
                    CommandArgs.add(args[a]);
                }
            }else {
                command=Data;
                ArgDebugString.delete(0,ArgDebugString.length());
                ArgDebugString.append(" No Args");
            }

            System.out.println("Player:"+PlayerID+" sent "+"command: "+command+ArgDebugString);

            if(command.equals("PlayerDisconnected")){
                if(PlayerID!=-1){
                    ConnectionIDs[PlayerID]=false;
                }

            }
            if(PlayerID!=-1) {
                if (command.equals("KEY")) {
                    players[PlayerID].getComponent(PlayerControllerComponent.class).UpdateKey(CommandArgs.get(0));
                }
            }
        }
    }

    static class MessageWriterS implements TCPMessageWriter<String> {

        private OutputStream os;
        private PrintWriter out;

        MessageWriterS(OutputStream os) {
            this.os = os;
            out = new PrintWriter(os, true);
        }

        @Override
        public void write(String s) throws Exception {
            out.print(s.toCharArray());
            out.flush();
        }
    }

    static class MessageReaderS implements TCPMessageReader<String> {

        private BlockingQueue<String> messages = new ArrayBlockingQueue<>(50);

        private InputStreamReader in;

        MessageReaderS(InputStream is) {
            in =  new InputStreamReader(is);

            var t = new Thread(() -> {
                try {

                    char[] buf = new char[36];

                    int len;

                    while ((len = in.read(buf)) > 0) {
                        var message = new String(Arrays.copyOf(buf, len));

                        System.out.println("Recv message: " + message);

                        messages.put(message);
                    }

                } catch (Exception e) {

                    var message = new String("PlayerDisconnected|");
                    try {
                        messages.put(message);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    e.printStackTrace();
                }
            });

            t.setDaemon(true);
            t.start();
        }

        @Override
        public String read() throws Exception {
            return messages.take();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
