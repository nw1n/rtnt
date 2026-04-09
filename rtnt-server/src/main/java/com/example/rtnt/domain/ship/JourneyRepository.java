package com.example.rtnt.domain.ship;

import java.util.List;

public interface JourneyRepository {
    Journey save(Journey journey);

    List<Journey> findLatest100();

    List<Journey> findByShipId(String shipId);

    void deleteAll();

    /**
     * If there are more than {@code maxSize} journeys, deletes the oldest (by {@code departed})
     * until at most {@code maxSize} remain.
     *
     * @return number of journeys removed
     */
    int trimToMaxSize(int maxSize);
}
