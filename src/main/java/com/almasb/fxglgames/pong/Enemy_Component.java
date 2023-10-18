package com.almasb.fxglgames.pong;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.core.math.FXGLMath;
public class Enemy_Component  extends Component {

    private static final double ENEMY_SPEED = 50;

    private static final double ATTACK_RADIUS = 250;

    protected PhysicsComponent physics;

    public Entity[] Players;

    public void  Setup(Entity[] _Players){
        Players=_Players;
    }


    double clamp(double value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void onUpdate(double tpf) {


        //Search if players are within attack radius and make the closet player the target
        Entity Target=null;
        var ClosestPlayer=ATTACK_RADIUS*10;

        for (Entity player : Players) {
            var dist=entity.getPosition().distance(player.getPosition());
            if(dist<ATTACK_RADIUS&&dist<ClosestPlayer){
                ClosestPlayer=dist;
                Target=player;
            }
        }

        //did we find a target
        if(Target!=null){//move towards Target
            double X=clamp((Target.getX()-entity.getX()), -1, 1);
            double Y=clamp((Target.getY()-entity.getY()), -1, 1);


            physics.setVelocityY(Y*ENEMY_SPEED);
            physics.setVelocityX(X*ENEMY_SPEED);
        }


    }
}