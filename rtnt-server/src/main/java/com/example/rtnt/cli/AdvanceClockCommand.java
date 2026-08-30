package com.example.rtnt.cli;

import com.example.rtnt.game.loop.ClockStatus;
import com.example.rtnt.game.loop.GameLoop;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(0)
public class AdvanceClockCommand implements ApplicationRunner {
    public static final String ARG = "advance-clock";

    private final GameLoop gameLoop;
    private final ConfigurableApplicationContext context;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public AdvanceClockCommand(GameLoop gameLoop, ConfigurableApplicationContext context) {
        this.gameLoop = gameLoop;
        this.context = context;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (!this.isCommandCalled(args)) {
            return;
        }
        int ticks = this.parseTicks(args);
        ClockStatus status = this.gameLoop.advance(ticks);
        System.out.println("Clock advanced to tick " + status.tick());
        System.exit(SpringApplication.exit(this.context, () -> 0));
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                         *
     *                                                                         *
     **************************************************************************/

    private boolean isCommandCalled(ApplicationArguments args) {
        return args.getNonOptionArgs().contains(ARG);
    }

    private int parseTicks(ApplicationArguments args) {
        List<String> nonOption = args.getNonOptionArgs();
        int index = nonOption.indexOf(ARG);
        if (index + 1 >= nonOption.size()) {
            return 10;
        }
        return Integer.parseInt(nonOption.get(index + 1));
    }
}
