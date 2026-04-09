package com.example.rtnt.adapter.out.reference;

import com.example.rtnt.usecase.ship.ShipNameProvider;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter for ship name provisioning.
 */
@Component
public class CaribbeanShipNameProvider implements ShipNameProvider {
    private final CaribbeanShipNames caribbeanShipNames;

    public CaribbeanShipNameProvider(CaribbeanShipNames caribbeanShipNames) {
        this.caribbeanShipNames = caribbeanShipNames;
    }

    @Override
    public String getNextName() {
        return this.caribbeanShipNames.getNext();
    }

    @Override
    public String getRandomName() {
        return this.caribbeanShipNames.getRandom();
    }
}
