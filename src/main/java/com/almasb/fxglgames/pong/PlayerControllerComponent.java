package com.almasb.fxglgames.pong;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class PlayerControllerComponent extends Component {

    public boolean Connected=false;
    public int PlayerID=-1;
    private static final double PLAYER_SPEED = 100;

    private static final float PLAYER_MAX_HEALTH = 100;

    protected PhysicsComponent physics;


    private int Kills=0;

    public SpellComponent[] spellsPool;

    //health, kills ect
    private boolean StatsChanged=false;

    //position Changed x y angle
    private boolean PosChanged=false;
    public boolean ShouldUpdate(){
        return StatsChanged||PosChanged;
    }

    public String GetUpdateString(){
        if(!ShouldUpdate()||(StatsChanged&&PosChanged)){//overide or all
            StatsChanged=false;
            PosChanged=false;
            return "PLAYER_DATA,"+PlayerID+","+getEntity().getX()+","+getEntity().getY()+","+(Connected ? "ACTIVE" : "IDLE")+","+GetHealth()+","+getAngle()+","+getKills()+",|";
        }else {
            if(StatsChanged){
                StatsChanged=false;
                return "PLAYER_STAT,"+PlayerID+","+(Connected ? "ACTIVE" : "IDLE")+","+GetHealth()+","+getKills()+",|";
            }else if(PosChanged){//pos changed
                PosChanged=false;
                return "PLAYER_POS,"+PlayerID+","+getEntity().getX()+","+getEntity().getY()+","+getAngle()+",|";
            }
        }
        return "UpdateErrorOnPlayerData";
    }

    SpellComponent GetSpell(){

        for (int i = 0; i < spellsPool.length; i++) {
            if(!spellsPool[i].Active){
                return spellsPool[i];
            }
        }
        return  null;
    }



    private boolean up=false,down=false,left=false,right=false;

    private float Health=PLAYER_MAX_HEALTH;

    private float Angle=0;

    private double LastPosCheck=0;

    public float getAngle() {
        return Angle;
    }

    public void setAngle(float angle) {
        if(Angle!=angle){
            Angle = angle;
            PosChanged=true;
        }
    }

    public boolean IsDead(){
        return Health<=0;
    }
    public void DealDamage(float Amount){
        Health-=Amount;

        if(Health<=0){
            up=false;
            down=false;
            left=false;
            right=false;
            physics.overwritePosition(new Point2D(-1000,-1000));
        }
        StatsChanged=true;
    }

    public void AddKillStat(){
        Kills++;
    }

    public int getKills() {
        return Kills;
    }

    public float GetHealth(){
        return Health;
    }


    PlayerControllerComponent(){
        Reset();
    }

    public void  Reset(){
        Health=PLAYER_MAX_HEALTH;
        Kills=0;
        StatsChanged=true;
        PosChanged=true;
    }

    public void CastSpell(boolean FireType){
        var Spell=GetSpell();

        if(Spell!=null){
            var radians=(getAngle()-90)*( 3.14159265/180);
            var x = Math.cos(radians);
            var y = Math.sin(radians);

            Spell.Shoot(PlayerID,entity.getPosition(), new Point2D(x,y),FireType);
        }
    }

    public void UpdateKey(String Info){
        if(Info.contains("CAST")){
            CastSpell(Info.contains("FIRE"));
            return;
        }

        var key=Info.charAt(0);
        System.out.println(key);
        System.out.println(Info);
        boolean KeyActive=!Info.contains("UP");
        System.out.println(KeyActive);
        switch (key){
            case 'W':
                up=KeyActive;
                break;
            case 'A':
                left=KeyActive;
                break;
            case 'S':
                down=KeyActive;
                break;
            case 'D':
                right=KeyActive;
                break;
        }
    }
    @Override
    public void onUpdate(double tpf) {
        double X=0;
        double Y=0;
        if(up){
            Y=-PLAYER_SPEED;
        } else if (down) {
            Y=+PLAYER_SPEED;
        }

        if(left){
            X=-PLAYER_SPEED;
        } else if (right) {
            X=PLAYER_SPEED;
        }

        physics.setVelocityY(Y);
        physics.setVelocityX(X);

        double newPos= (entity.getPosition().getX()+entity.getY());

        if(newPos!=LastPosCheck){
            LastPosCheck=newPos;
            PosChanged=true;
        }
    }
}