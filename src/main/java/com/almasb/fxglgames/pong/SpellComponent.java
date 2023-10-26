package com.almasb.fxglgames.pong;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class SpellComponent  extends Component {

    private static final double SPELL_SPEED = 5;
    private static final float RECYCLETIME = 5;
    public boolean Active=false;
    private float TimeLeft;
    private Point2D Velocity;

    private int ShotBY=-1;

    public static Enemy_Component[] Enemys;

    public int getShotBY() {
        return ShotBY;
    }

    private boolean SpellType=false;

    public boolean isFireSpellType() {
        return SpellType;
    }

    private boolean ShouldUpdate=false;
    public boolean HasServerUpdate(){
        return ShouldUpdate;
    }

    public String GetServerUpdate(int Index){
        ShouldUpdate=false;
        if(Active){
            return"SPELL_START,"+Index+","+SpellType+","+getEntity().getPosition().getX()+","+getEntity().getPosition().getY()+","+Velocity.getX()+","+Velocity.getY()+",|";
        }else {
            return"SPELL_FINISH,"+Index+",|";
        }
    }

    public void Shoot(int ShotBYID,Point2D StartPos, Point2D Dir,boolean IsFireSpell){
        ShotBY=ShotBYID;
        TimeLeft=RECYCLETIME;
        SpellType=IsFireSpell;
        Active=true;
        entity.setPosition(StartPos);
        entity.translate(Dir);
        Velocity=new Point2D(Dir.getX()*SPELL_SPEED,Dir.getY()*SPELL_SPEED);
        ShouldUpdate=true;
    }

    @Override
    public void onUpdate(double tpf) {
        if(Active){
            if(TimeLeft>0){
                TimeLeft-=tpf;
            }
            entity.translate(Velocity.getX(),Velocity.getY());
            if(TimeLeft<0){
                ShouldUpdate=true;
                Active=false;
                return;
            }



            for (Enemy_Component Enemy : Enemys) {
                var dist = entity.getPosition().distance(Enemy.getEntity().getPosition());
                if (dist < 20 ) {
                    Enemy.DealDamage(this);
                    ShouldUpdate=true;
                    Active=false;
                    return;
                }
            }
        }
    }

}
