package com.example.rtnt.cli;

import com.example.rtnt.service.IslandService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class RecreateIslandsCommand implements ApplicationRunner {
    public static final String ARG = "recreate-islands";

    private final IslandService islandService;
    private final ConfigurableApplicationContext context;

    public RecreateIslandsCommand(IslandService islandService, ConfigurableApplicationContext context) {
        this.islandService = islandService;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.getNonOptionArgs().contains(ARG)) {
            return;
        }
        this.islandService.recreateAll();
        System.exit(SpringApplication.exit(this.context, () -> 0));
    }
}
