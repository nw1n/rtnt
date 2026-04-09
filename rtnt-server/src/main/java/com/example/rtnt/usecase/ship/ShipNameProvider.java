package com.example.rtnt.usecase.ship;

/**
 * Port used by use cases to retrieve ship names.
 * Implementations are provided by outbound adapters.
 */
public interface ShipNameProvider {

    /**
     * Get the next ship name according to provider strategy.
     *
     * @return next ship name
     */
    String getNextName();

    /**
     * Get a random ship name according to provider strategy.
     *
     * @return random ship name
     */
    String getRandomName();
}
