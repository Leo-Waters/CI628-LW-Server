package com.almasb.fxglgames.pong;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;


import static com.almasb.fxgl.dsl.FXGL.spawn;

public class LevelManager {

    public static final int TileSize  = 80;

    Level[] Levels={new Level("Level1")};
    int CurrentLevel=-1;

    public  boolean ShouldUpdate=false;

    Entity[] LevelEntities;

    public Level GetCurrent(){
        return Levels[CurrentLevel];
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

    public void NextLevel(PlayerControllerComponent[] players, Enemy_Component[] enemy){
        DestroyLevel();
        CurrentLevel++;


        LevelEntities= new Entity[Levels[CurrentLevel].Width*Levels[CurrentLevel].Height];

        int PlayersLeft = players.length;
        int EnemysLeft = enemy.length;
        for (int x = 0; x < Levels[CurrentLevel].Width; x++) {
            for (int y = 0; y < Levels[CurrentLevel].Height; y++) {
                if (Levels[CurrentLevel].LevelData[y][x] == 1) {
                    LevelEntities[(y*Levels[CurrentLevel].Width)+x]=spawn("Wall", new SpawnData(x * TileSize, y * TileSize));
                }
                if (Levels[CurrentLevel].LevelData[y][x] == 4 && PlayersLeft != 0) {
                    PlayersLeft--;
                    players[PlayersLeft].getEntity().getComponent(PhysicsComponent.class).overwritePosition(new Point2D(x*TileSize,y*TileSize));
                }
                if (Levels[CurrentLevel].LevelData[y][x] == 5 && EnemysLeft != 0) {
                    EnemysLeft--;
                    enemy[EnemysLeft].getEntity().getComponent(PhysicsComponent.class).overwritePosition(new Point2D(x*TileSize,y*TileSize));
                    enemy[EnemysLeft].Init();
                }
            }
        }
        ShouldUpdate=true;
    }

}
