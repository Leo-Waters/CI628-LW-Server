package com.almasb.fxglgames.pong;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class PlayerControllerComponent extends Component {

    private static final double PLAYER_SPEED = 100;

    private static final float PLAYER_MAX_HEALTH = 100;

    protected PhysicsComponent physics;

    public SpellComponent[] spellsPool;

    SpellComponent GetSpell(){

        for (int i = 0; i < spellsPool.length; i++) {
            if(!spellsPool[i].Active){
                return spellsPool[i];
            }
        }
        return  null;
    }

    private boolean up=false,down=false,left=false,right=false;

    public float Health=PLAYER_MAX_HEALTH;


    public void CastSpell(boolean FireType){
        var Spell=GetSpell();

        if(Spell!=null){
            Spell.Shoot(entity.getPosition(), new Point2D(10,0));
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
    }
}