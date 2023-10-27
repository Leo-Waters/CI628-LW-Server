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

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.UIController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * @author Leo waters
 */
public class MainUIController implements UIController {

    @FXML
    private Label Player1;
    @FXML
    private Label Player2;
    @FXML
    private Label Player3;
    @FXML
    private Label Player4;
    @FXML
    private Label TimeElapsed;
    @FXML
    private Label Recived;
    @FXML
    private Label Sent;

    public void ShowPlayerPossessionState(int player, boolean Possessed){
        switch (player){
            case 0:
                Player1.setText(Possessed?"Player Has Client":"Player Is Idle");
                break;
            case 1:
                Player2.setText(Possessed?"Player Has Client":"Player Is Idle");
                break;
            case 2:
                Player3.setText(Possessed?"Player Has Client":"Player Is Idle");
                break;
            case 3:
                Player4.setText(Possessed?"Player Has Client":"Player Is Idle");
                break;
        }
    }
    double Time=0;
    public void ShowServerPerformance(double tpf,int BytesRead,int BytesSent){
        Time+=tpf;
        TimeElapsed.setText("Time Elapsed: "+String.format("%.2f",Time)+"s");
        Sent.setText("Bytes Sent: "+BytesSent+"\nAverage "+String.format("%.2f",(BytesSent)/Time)+"/s");
        Recived.setText("Bytes Read: "+BytesRead+"\nAverage "+String.format("%.2f", BytesRead/Time)+"/s");
    }
    @Override
    public void init() {
        Player1.setFont(FXGL.getUIFactory().newFont(18));
        Player2.setFont(FXGL.getUIFactory().newFont(18));
        Player3.setFont(FXGL.getUIFactory().newFont(18));
        Player4.setFont(FXGL.getUIFactory().newFont(18));

        TimeElapsed.setFont(FXGL.getUIFactory().newFont(18));
        Sent.setFont(FXGL.getUIFactory().newFont(18));
        Recived.setFont(FXGL.getUIFactory().newFont(18));

    }

}
