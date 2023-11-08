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

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.beans.binding.Bindings;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Leo Waters
 */
public class DungenFactory implements EntityFactory {
    @Spawns("Player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        return entityBuilder(data)
                .viewWithBBox(new Rectangle(40, 40, Color.BLUE))
                .with(new CollidableComponent(true))
                .with(physics)
                .with(new PlayerControllerComponent())
                .build();
    }

    @Spawns("Spell")
    public Entity newSpell(SpawnData data) {
        return entityBuilder(data)
                .viewWithBBox(new Rectangle(20, 20, Color.RED))
                .with(new SpellComponent())
                .build();
    }

    @Spawns("Wall")
    public Entity newWall(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        return entityBuilder(data)
                .viewWithBBox(new Rectangle(LevelManager.TileSize, LevelManager.TileSize, Color.DIMGRAY))
                .with(new CollidableComponent(true))
                .with(physics)
                .build();
    }

    @Spawns("Enemy")
    public Entity newNPC(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        Random random = new Random();
        boolean FireDemon = random.nextInt(2) == 0;
        var enmeycomp=new Enemy_Component();
        enmeycomp.Type_FireDemon=FireDemon;

        if(FireDemon){
            return entityBuilder(data)
                    .viewWithBBox(new Rectangle(40, 40, Color.RED))
                    .with(new CollidableComponent(true))
                    .with(physics)
                    .with(enmeycomp)
                    .build();
        }else {
            return entityBuilder(data)
                    .viewWithBBox(new Rectangle(40, 40, Color.BLUE))
                    .with(new CollidableComponent(true))
                    .with(physics)
                    .with(enmeycomp)
                    .build();
        }

    }

}
