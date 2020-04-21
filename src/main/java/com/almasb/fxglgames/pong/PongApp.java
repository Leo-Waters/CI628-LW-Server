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

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.ui.UI;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * A simple clone of Pong.
 * Sounds from https://freesound.org/people/NoiseCollector/sounds/4391/ under CC BY 3.0.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class PongApp extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Pong");
        settings.setVersion("1.0");
        settings.setFontUI("pong.ttf");
    }

    private BatComponent playerBat;

    private Server server;

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                playerBat.up();
            }

            @Override
            protected void onActionEnd() {
                playerBat.stop();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                playerBat.down();
            }

            @Override
            protected void onActionEnd() {
                playerBat.stop();
            }
        }, KeyCode.S);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("player1score", 0);
        vars.put("player2score", 0);
    }

    @Override
    protected void initGame() {
        server = new Server();
        server.start();

//        getGameState().<Integer>addListener("player1score", (old, newScore) -> {
//            if (newScore == 11) {
//                showGameOver("Player 1");
//            }
//        });
//
//        getGameState().<Integer>addListener("player2score", (old, newScore) -> {
//            if (newScore == 11) {
//                showGameOver("Player 2");
//            }
//        });

        getGameWorld().addEntityFactory(new PongFactory());

        getGameScene().setBackgroundColor(Color.rgb(0, 0, 5));

        initScreenBounds();
        initGameObjects();
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 0);

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BALL, EntityType.WALL) {
            @Override
            protected void onHitBoxTrigger(Entity a, Entity b, HitBox boxA, HitBox boxB) {
                if (boxB.getName().equals("LEFT")) {
                    inc("player2score", +1);
                } else if (boxB.getName().equals("RIGHT")) {
                    inc("player1score", +1);
                }

                play("hit_wall.wav");
                getGameScene().getViewport().shakeTranslational(5);
            }
        });

        CollisionHandler ballBatHandler = new CollisionHandler(EntityType.BALL, EntityType.PLAYER_BAT) {
            @Override
            protected void onCollisionBegin(Entity a, Entity bat) {
                play("hit_bat.wav");
                playHitAnimation(bat);
            }
        };

        getPhysicsWorld().addCollisionHandler(ballBatHandler);
        getPhysicsWorld().addCollisionHandler(ballBatHandler.copyFor(EntityType.BALL, EntityType.ENEMY_BAT));
    }

    @Override
    protected void initUI() {
        MainUIController controller = new MainUIController();
        UI ui = getAssetLoader().loadUI("main.fxml", controller);

        controller.getLabelScorePlayer().textProperty().bind(getip("player1score").asString());
        controller.getLabelScoreEnemy().textProperty().bind(getip("player2score").asString());

        getGameScene().addUI(ui);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!server.isConnected)
            return;

        try {
            server.data.put(playerBat.getEntity().getPosition());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initScreenBounds() {
        Entity walls = entityBuilder()
                .type(EntityType.WALL)
                .collidable()
                .buildScreenBounds(150);

        getGameWorld().addEntity(walls);
    }

    private void initGameObjects() {
        Entity ball = spawn("ball", getAppWidth() / 2 - 5, getAppHeight() / 2 - 5);
        Entity bat1 = spawn("bat", new SpawnData(getAppWidth() / 4, getAppHeight() / 2 - 30).put("isPlayer", true));
        Entity bat2 = spawn("bat", new SpawnData(3 * getAppWidth() / 4 - 20, getAppHeight() / 2 - 30).put("isPlayer", false));

        playerBat = bat1.getComponent(BatComponent.class);
    }

    private void playHitAnimation(Entity bat) {
        animationBuilder()
                .autoReverse(true)
                .duration(Duration.seconds(0.5))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .rotate(bat)
                .from(FXGLMath.random(-25, 25))
                .to(0)
                .buildAndPlay();
    }

    private void showGameOver(String winner) {
        getDisplay().showMessageBox(winner + " won! Demo over\nThanks for playing", getGameController()::exit);
    }

    private class Server {
        private boolean isConnected = false;
        private BlockingQueue<Point2D> data = new ArrayBlockingQueue<>(20);

        private void run() {
            int portNumber = 55555;

            System.out.println("Listening on " + portNumber);

            try (ServerSocket serverSocket = new ServerSocket(portNumber);
                 Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 var in = new InputStreamReader(clientSocket.getInputStream());
            ) {

                isConnected = true;
                clientSocket.setTcpNoDelay(true);



                char[] buf = new char[36];

                int len;

//                while ((len = in.read(buf)) > 0) {
//                    System.out.println(new String(Arrays.copyOf(buf, len)));
//                }


                // TODO: clientSocket.isClosed()
                while (true) {
                    var p = data.take();

                    int y = (int) p.getY();

                    var s = String.valueOf(y);

                    char[] buffer;

                    if (s.length() == 3) {
                        buffer = s.toCharArray();
                    } else if (s.length() == 2) {
                        buffer = new char[3];
                        buffer[1] = s.charAt(0);
                        buffer[2] = s.charAt(1);
                    } else { // length == 1
                        buffer = new char[3];
                        buffer[2] = s.charAt(0);
                    }

                    out.println(buffer);
                }

//            while ((inputLine = in.) != null) {
//                System.out.println("Input: " + inputLine);
//
//                //outputLine = "Bye.";
//                //out.println(outputLine);
//                if (outputLine.equals("Bye."))
//                    break;
//            }
            } catch (Exception e) {
                System.out.println("Exception caught when trying to listen on port "
                        + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
            }

            isConnected = false;
            System.out.println("Connection closed");
        }

        public void start() {
            Thread t = new Thread(this::run);
            t.setDaemon(true);
            t.start();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}