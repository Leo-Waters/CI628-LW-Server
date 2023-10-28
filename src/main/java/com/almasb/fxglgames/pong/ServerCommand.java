package com.almasb.fxglgames.pong;

import java.util.List;

public class ServerCommand {
    public ServerCommand(String cmd, List<String> _args) {
        Command = cmd;
        Args = _args;
    }

    public String Command;
    public List<String> Args;
}
