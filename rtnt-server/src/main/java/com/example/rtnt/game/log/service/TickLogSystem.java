package com.example.rtnt.game.log.service;

import com.example.rtnt.game.clock.event.TickAdvancedEvent;
import com.example.rtnt.game.log.domain.GameLogEvent;
import com.example.rtnt.game.system.GameUnitOfWork;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TickLogSystem {
    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final GameUnitOfWork unitOfWork;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public TickLogSystem(GameUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    /***************************************************************************
     *                                                                         *
     * Event Handlers                                                          *
     *                                                                         *
     **************************************************************************/

    @EventListener
    public void onTickAdvanced(TickAdvancedEvent event) {
        this.unitOfWork.append(GameLogEvent.forTick(event.clock().tick()));
    }
}
