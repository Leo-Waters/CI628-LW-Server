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
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.net.*;
import com.almasb.fxgl.ui.UI;
import javafx.scene.paint.Color;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

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

    private PlayerControllerComponent[] players= new PlayerControllerComponent[4];
    public Enemy_Component[] enemys= new Enemy_Component[40];

    //if true will force game stat update, used for when new clients join
    public boolean UpdateOveride=false;

    public SpellComponent[] Spells=new SpellComponent[20];

    public  LevelManager LevelManager= new LevelManager();
    private Server<String> server;


    @Override
    protected void initGameVars(Map<String, Object> vars) {
    }

    @Override
    protected void initGame() {
        Writers.INSTANCE.addTCPWriter(String.class, outputStream -> new MessageWriterS(outputStream));
        Readers.INSTANCE.addTCPReader(String.class, in -> new MessageReaderS(in));

        server = getNetService().newTCPServer(55555, new ServerConfig<>(String.class));

        ScoreSystem.LoadHighScore();

        server.setOnConnected(connection -> {//called when client connects
            System.out.println("Player Connected!");

            String IDMess="";
            boolean AssignedAPlayerID=false;//player hasn't been assigned an ID
            for (int i = 0; i < 4; i++) {

                if(!players[i].Connected){//if Connection ID == false
                    AssignedAPlayerID=players[i].Connected=true;// has assign Player ID
                    connection.getLocalSessionData().setValue("ID",i);//set Connection ID
                    IDMess="ID,"+i+",|";//send ID to Client
                    break;
                }
            }
            if(!AssignedAPlayerID){//hasn't Received ID, set to -1 as spectator
                connection.getLocalSessionData().setValue("ID",-1);
            }

            //send current level file----------------------------------------------------------------------
            String HighScoreMess="HIGHSCORE,"+ScoreSystem.HighLevel+","+ScoreSystem.HighKills+",|";
            connection.send(IDMess+GetLevelDataAsString()+HighScoreMess);
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

    private String GetLevelDataAsString() {
        Level level=LevelManager.GetCurrent();
        StringBuilder levelUpdate= new StringBuilder();
        levelUpdate.append("LEVELUPDATE,").append(LevelManager.CurrentLevel + 1).append(",").append(level.Width).append(",").append(level.Height).append(",|");
        for (int y=0; y<level.Height; y++){
            levelUpdate.append("LEVELDATA,").append(y);

            for (int x=0; x<level.Width; x++){
                levelUpdate.append(",").append(level.LevelData[y][x]);
            }
            levelUpdate.append(",|");


        }
        levelUpdate.append("LEVELUPDATECOMPLETE|");

        return  levelUpdate.toString();
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
            boolean isPossed= players[i].Connected;
            ((MainUIController)ui.getController()).ShowPlayerPossessionState(i,isPossed);
        }
    }

    boolean ShouldUpdate=false;
    private static final double UpdateDelay = 0.2;
    double TimeTillUpdate=0;


    @Override
    protected void onUpdate(double tpf) {

        if(!ShouldUpdate){
            TimeTillUpdate-=tpf;
            if(TimeTillUpdate<=0){
                ShouldUpdate=true;
                TimeTillUpdate=UpdateDelay;
            }
        }
        BroadCastSpellUpdates();

        boolean PlayersAllDead=true;
        for (int i = 0; i < players.length; i++) {
            if(!players[i].IsDead()){
                PlayersAllDead=false;
                break;
            }
        }
        if(PlayersAllDead){//restart game

            int combinedkills=0;

            for (int i = 0; i < players.length; i++) {
                combinedkills+=players[i].getKills();
            }

            ScoreSystem.SaveNewHighScore(LevelManager.CurrentLevel+1,combinedkills);
            server.broadcast("HIGHSCORE,"+ScoreSystem.HighLevel+","+ScoreSystem.HighKills+",|");
            LevelManager.Reset(players,enemys);



            UpdateOveride=true;//force game state update
        }else if(LevelManager.HatchCheck(players)){//player at hatch load next level
            LevelManager.NextLevel(players,enemys);
            UpdateOveride=true;//force game state update
        }


        ((MainUIController)ui.getController()).ShowServerPerformance(tpf,MessageReaderS.TotalBytesRead,MessageReaderS.TotalBytesAfterDecompression,MessageWriterS.TotalBytesSent,MessageWriterS.TotalBytesAfterCompression);
        if (!server.getConnections().isEmpty()&&(ShouldUpdate||LevelManager.ShouldUpdate)) {
            ShouldUpdate=false;

            if(LevelManager.ShouldUpdate){
                server.broadcast(GetLevelDataAsString());
                LevelManager.ShouldUpdate=false;
            }
            UpdateUI();
            BroadCastPlayerUpdates();
            BroadCastEnemyUpdates();

            if(UpdateOveride){//update override was performed
                System.out.println("Update Overide Preformed----------------");
                UpdateOveride=false;
            }
        }else{
            //no players connected, level file will be sent individualy as they join to save repeating the message
            if(LevelManager.ShouldUpdate) {
                LevelManager.ShouldUpdate=false;
            }
        }
    }

    void BroadCastPlayerUpdates(){
        StringBuilder message= new StringBuilder();
        for(int i=0;i<players.length;i++)//send updates for each players data
        {
            if(players[i].ShouldUpdate()||UpdateOveride){
                message.append(players[i].GetUpdateString());

            }

        }
        //send data to server
        if(!message.toString().isEmpty()){
            server.broadcast(message.toString());
        }

    }

    void BroadCastEnemyUpdates(){
        StringBuilder message= new StringBuilder();
        for(int i=0;i<enemys.length;i++)//send updates for each players data
        {

            if(enemys[i].ShouldUpdate||UpdateOveride){
                message.append("ENEMY_DATA,").append(i).append(",").append(enemys[i].getEntity().getX()).append(",").append(enemys[i].getEntity().getY()).append(",").append(enemys[i].GetHealth()).append(",").append(enemys[i].Type_FireDemon).append(",|");

                enemys[i].ShouldUpdate=false;
            }

        }
        //send data to server
        if(!message.toString().isEmpty()){
            server.broadcast(message.toString());
        }

    }

    void BroadCastSpellUpdates(){
        StringBuilder message= new StringBuilder();
        for (int s = 0; s < Spells.length; s++) {
            if(Spells[s].HasServerUpdate()){//does the spell have an update
                message.append(Spells[s].GetServerUpdate(s));//send the update to clients
            }
        }
        //send data to server
        if(!message.toString().isEmpty()){
            server.broadcast(message.toString());

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

        for (int i = 0; i < Spells.length; i++) {
            Spells[i]=spawn("Spell", new SpawnData(-1000+(i*40), -1000)).getComponent(SpellComponent.class);
        }
        for (int i = 0; i < players.length; i++) {
            players[i]=spawn("Player", new SpawnData(-1000+(i*40), -1000)).getComponent(PlayerControllerComponent.class);
            players[i].spellsPool=Spells;
            players[i].PlayerID=i;
        }

        for (int i = 0; i < enemys.length; i++) {
            enemys[i]=spawn("Enemy", new SpawnData(-1000+(i*40), -1000)).getComponent(Enemy_Component.class);
            enemys[i].Setup(players);
        }

        SpellComponent.Enemys=enemys;

        LevelManager.NextLevel(players,enemys);
    }



    @Override
    public void onReceive(Connection<String> connection, String message) {



        int PlayerID=connection.getLocalSessionData().getInt("ID");


        if(PlayerID==-1){//assign and ID to a spectator
            for (int i = 0; i < 4; i++) {
                if(!players[i].Connected){
                    players[i].Connected=true;
                    connection.getLocalSessionData().setValue("ID",i);
                    connection.send("ID,"+i+",|");
                    break;
                }
            }

        }

        if(PlayerID!=-1&&players[PlayerID].IsDead()){//assign a new ID to a dead player
            int NewID=-1;
            for (int i = 0; i < 4; i++) {
                if(!players[i].Connected&& !players[i].IsDead()){
                    NewID=i;
                    players[PlayerID].Connected=false;
                    players[i].Connected=true;
                    connection.getLocalSessionData().setValue("ID",i);
                    connection.send("ID,"+i+",|");
                    break;
                }
            }
            if(NewID!=-1){
                PlayerID=NewID;
            }

        }
        String[] Commands=message.split("\\|");
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

            System.out.println("Player:"+PlayerID+" sent "+"command: "+command+ArgDebugString+" INDATA:"+Data);

            if(command.equals("PlayerDisconnected")){
                if(PlayerID!=-1){
                    players[PlayerID].Connected=false;
                }

            }
            else if(command.equals("GAMESTATE")){//player is requesting an update of the game state to sync
                UpdateOveride=true;
            }
            else if(PlayerID!=-1) {
                if (command.equals("KEY")) {
                    players[PlayerID].getEntity().getComponent(PlayerControllerComponent.class).UpdateKey(CommandArgs.get(0));
                }else if (command.equals("ANGLE")) {
                    players[PlayerID].getEntity().getComponent(PlayerControllerComponent.class).setAngle(Float.parseFloat(CommandArgs.get(0)));
                }
            }
        }
    }

    static class MessageWriterS implements TCPMessageWriter<String> {


        public static Integer TotalBytesSent=0;
        public static Integer TotalBytesAfterCompression=0;
        private OutputStream os;
        private PrintWriter out;

        MessageWriterS(OutputStream os) {
            this.os = os;
            out = new PrintWriter(os, true);
        }

        @Override
        public void write(String s) throws Exception {
            TotalBytesSent+=s.length();//used to debug sent data
            System.out.println("Sending :"+s);

            var compressed=ServerMessageHelpers.CompressString(s);
            TotalBytesAfterCompression+=compressed.length();

            //System.out.println("Non Comp :"+(s.getBytes()).length);
            //System.out.println(s);
            //System.out.println("Comp :"+compressed.getBytes().length);
            //System.out.println(compressed);
            //String decomp=ServerMessageHelpers.DecompressString(compressed);
            //System.out.println("De comp :"+decomp.getBytes().length);
            //System.out.println(decomp);
            out.write(compressed+"%");
            out.flush();
        }
    }

    static class MessageReaderS implements TCPMessageReader<String> {

        private BlockingQueue<String> messages = new ArrayBlockingQueue<>(50);

        public  static Integer TotalBytesRead=0;
        public static Integer TotalBytesAfterDecompression=0;
        private InputStreamReader in;

        MessageReaderS(InputStream is) {
            in =  new InputStreamReader(is);
            AtomicReference<StringBuilder> CurrentMessage= new AtomicReference<>(new StringBuilder());

            var t = new Thread(() -> {
                try {

                    char[] buf = new char[36];

                    int len;

                    while ((len = in.read(buf)) > 0) {
                        //READ MESSAGE UNTIL REACH %, THEN DECOMPRESS AND ADD TO MESSAGE QUE
                        TotalBytesRead+=len;
                        for (int i = 0; i < len; i++) {
                            if(buf[i]=='%') {//end of message
                                var decompressed=ServerMessageHelpers.DecompressString(CurrentMessage.toString());
                                TotalBytesAfterDecompression+=decompressed.length();
                                System.out.println("Recv message: " + decompressed);
                                messages.put(decompressed);
                                CurrentMessage.set(new StringBuilder());
                            }else {//add char to message
                                CurrentMessage.get().append(buf[i]);
                            }

                        }

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
