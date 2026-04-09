package com.example.rtnt.usecase.island;

/**
 * Port used by use cases to retrieve island names.
 * Implementations are provided by outbound adapters.
 */
public interface IslandNameProvider {

    /**
     * Get the next island name according to provider strategy.
     *
     * @return next island name
     */
    String getNextName();

    /**
     * Get a random island name according to provider strategy.
     *
     * @return random island name
     */
    String getRandomName();
}
