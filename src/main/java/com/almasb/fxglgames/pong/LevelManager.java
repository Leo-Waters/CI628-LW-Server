package com.almasb.fxglgames.pong;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;


import java.awt.*;

import static com.almasb.fxgl.dsl.FXGL.spawn;

public class LevelManager {

    public static final int TileSize  = 80;

    Level[] Levels={new Level("Level1"),new Level("Level2")};
    int CurrentLevel=-1;

    public  boolean ShouldUpdate=false;

    Entity[] LevelEntities;

    public Level GetCurrent(){
        return Levels[CurrentLevel];
    }
    Point2D HatchPos;
    public boolean HatchCheck(PlayerControllerComponent[] players){
        for (PlayerControllerComponent player : players) {
            var dist = player.getEntity().getPosition().distance(HatchPos);
            if (dist < 20 ) {//player is at hatch
                return true;
            }
        }
       return false;
    }
    private void DestroyLevel(){
        if(LevelEntities!=null){
            for (var ent: LevelEntities){
                if(ent!=null) {
                    ent.removeFromWorld();
                }
            }
            LevelEntities=null;
        }
    }
    public void Reset(PlayerControllerComponent[] players, Enemy_Component[] enemy){
        for (int p = 0; p < players.length; p++) {
            players[p].Reset();
        }
        for (int e = 0; e < enemy.length; e++) {
            enemy[e].physics.overwritePosition(new Point2D(-1000,-1500));
        }
        CurrentLevel=-1;
        NextLevel(players,enemy);

    }

    public void NextLevel(PlayerControllerComponent[] players, Enemy_Component[] enemy){
        DestroyLevel();
        CurrentLevel++;
        if(CurrentLevel==Levels.length){//ensure a level is always loaded, loop player back to begining
            CurrentLevel=0;
        }

        LevelEntities= new Entity[Levels[CurrentLevel].Width*Levels[CurrentLevel].Height];

        int PlayersLeft = players.length;
        int EnemysLeft = enemy.length;
        for (int x = 0; x < Levels[CurrentLevel].Width; x++) {
            for (int y = 0; y < Levels[CurrentLevel].Height; y++) {
                if (Levels[CurrentLevel].LevelData[y][x] == 1) {
                    boolean WallNeeded=false;
                    //calculate if the wall is needed
                    if (x==0||x==Levels[CurrentLevel].Width-1||y==0||y==Levels[CurrentLevel].Height-1){//borders always needed
                        WallNeeded=true;
                    }else {
                        //check neighbours
                        if(Levels[CurrentLevel].LevelData[y][x-1]!=1||Levels[CurrentLevel].LevelData[y][x+1]!=1||
                                Levels[CurrentLevel].LevelData[y-1][x]!=1||Levels[CurrentLevel].LevelData[y+1][x]!=1){
                            WallNeeded=true;
                        }
                    }


                    if(WallNeeded) {
                        LevelEntities[(y * Levels[CurrentLevel].Width) + x] = spawn("Wall", new SpawnData(x * TileSize, y * TileSize));
                    }
                }//end of level hatch------------------
                else if (Levels[CurrentLevel].LevelData[y][x] == 6) {
                    HatchPos= new Point2D(x * TileSize, y * TileSize);
                }
                else {//entity checks-------------------------------------------------------------------
                    var spawnpos=new Point2D((x*TileSize)+(TileSize/4),(y*TileSize)+(TileSize/4));
                    if (Levels[CurrentLevel].LevelData[y][x] == 4 && PlayersLeft != 0) {
                        PlayersLeft--;
                        players[PlayersLeft].getEntity().setPosition(spawnpos);
                        players[PlayersLeft].getEntity().getComponent(PhysicsComponent.class).overwritePosition(spawnpos);
                    }
                    if (Levels[CurrentLevel].LevelData[y][x] == 5 && EnemysLeft != 0) {
                        EnemysLeft--;
                        enemy[EnemysLeft].getEntity().setPosition(spawnpos);
                        enemy[EnemysLeft].getEntity().getComponent(PhysicsComponent.class).overwritePosition(spawnpos);
                        enemy[EnemysLeft].Init();
                    }
                }

            }
        }
        ShouldUpdate=true;
    }

}
