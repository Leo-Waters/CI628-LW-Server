package com.almasb.fxglgames.pong;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class SpellComponent  extends Component {


    private static final double SPELL_SPEED = 1;
    private static final float RECYCLETIME = 5;
    public boolean Active=false;
    float TimeLeft;
    Point2D Velocity;
    public void Shoot(Point2D StartPos, Point2D Dir){
        TimeLeft=RECYCLETIME;
        Active=true;
        entity.setPosition(StartPos);
        entity.translate(Dir);
        Velocity=Dir;
    }

    @Override
    public void onUpdate(double tpf) {
        if(Active){
            if(TimeLeft>0){
                TimeLeft-=tpf;
            }
            entity.translate(Velocity.getX()*SPELL_SPEED,Velocity.getY()*SPELL_SPEED);
            if(TimeLeft<0){
                Active=false;
            }

        }
    }

}
