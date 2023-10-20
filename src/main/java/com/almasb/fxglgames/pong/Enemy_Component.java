package com.almasb.fxglgames.pong;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.core.math.FXGLMath;
public class Enemy_Component  extends Component {

    private static final double ENEMY_SPEED = 50;

    private static final double DETECTION_RADIUS = 250;
    private static final double ATTACK_RADIUS = 50;
    private static final float ATTACK_DAMAGE = 1;

    private static final float ENEMY_MAX_HEALTH = 100;

    public float Health=ENEMY_MAX_HEALTH;

    protected PhysicsComponent physics;

    public PlayerControllerComponent[] Players;

    public void  Setup(PlayerControllerComponent[] _Players){
        Players=_Players;
        Health=ENEMY_MAX_HEALTH;
    }


    double clamp(double value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void onUpdate(double tpf) {
        if(Health<=0){
            entity.setPosition(-1000,-1000);
            return;
        }

        //Search if players are within attack radius and make the closet player the target
        PlayerControllerComponent Target = null;
        var ClosestPlayer = DETECTION_RADIUS * 10;


        boolean InAttackRadius=false;
        for (PlayerControllerComponent player : Players) {
            var dist = entity.getPosition().distance(player.getEntity().getPosition());
            if (dist < DETECTION_RADIUS && dist < ClosestPlayer) {
                ClosestPlayer = dist;
                Target = player;
                if(dist<ATTACK_RADIUS){
                    InAttackRadius=true;
                }
            }
        }

        //did we find a target
        if (Target != null) {//move towards Target
            double X = clamp((Target.getEntity().getX() - entity.getX()), -1, 1);
            double Y = clamp((Target.getEntity().getY() - entity.getY()), -1, 1);

            if(InAttackRadius){
                Target.Health-=ATTACK_DAMAGE;
            }

            physics.setVelocityY(Y * ENEMY_SPEED);
            physics.setVelocityX(X * ENEMY_SPEED);
        }


    }
}