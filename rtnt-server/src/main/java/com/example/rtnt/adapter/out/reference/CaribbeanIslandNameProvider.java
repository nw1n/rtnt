package com.example.rtnt.adapter.out.reference;

import com.example.rtnt.usecase.island.IslandNameProvider;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter for island name provisioning.
 */
@Component
public class CaribbeanIslandNameProvider implements IslandNameProvider {
    private final CaribbeanIslandNames caribbeanIslandNames;

    public CaribbeanIslandNameProvider(CaribbeanIslandNames caribbeanIslandNames) {
        this.caribbeanIslandNames = caribbeanIslandNames;
    }

    @Override
    public String getNextName() {
        return this.caribbeanIslandNames.getNext();
    }

    @Override
    public String getRandomName() {
        return this.caribbeanIslandNames.getRandom();
    }
}
