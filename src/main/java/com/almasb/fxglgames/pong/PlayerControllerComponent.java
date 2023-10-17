package com.almasb.fxglgames.pong;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
public class PlayerControllerComponent extends Component {

    private static final double PLAYER_SPEED = 100;

    protected PhysicsComponent physics;

    private boolean up=false,down=false,left=false,right=false;

    public void UpdateKey(String Info){
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