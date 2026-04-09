package com.example.rtnt.adapter.in.web;

import com.example.rtnt.domain.inventory.GoodType;
import com.example.rtnt.domain.inventory.Inventory;
import com.example.rtnt.domain.player.PlayerRepository;
import com.example.rtnt.domain.ship.Journey;
import com.example.rtnt.domain.ship.Ship;
import com.example.rtnt.usecase.ship.CreatePlayerControlledShipUseCase;
import com.example.rtnt.usecase.ship.ListShipsUseCase;
import com.example.rtnt.usecase.ship.StartPlayerJourneyUseCase;
import com.example.rtnt.usecase.ship.TradeWithIslandUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/ships")
public class ShipController {

    private final ListShipsUseCase listShipsUseCase;
    private final CreatePlayerControlledShipUseCase createPlayerControlledShipUseCase;
    private final StartPlayerJourneyUseCase startPlayerJourneyUseCase;
    private final TradeWithIslandUseCase tradeWithIslandUseCase;
    private final PlayerRepository playerRepository;

    public ShipController(
            ListShipsUseCase listShipsUseCase,
            CreatePlayerControlledShipUseCase createPlayerControlledShipUseCase,
            StartPlayerJourneyUseCase startPlayerJourneyUseCase,
            TradeWithIslandUseCase tradeWithIslandUseCase,
            PlayerRepository playerRepository
    ) {
        this.listShipsUseCase = listShipsUseCase;
        this.createPlayerControlledShipUseCase = createPlayerControlledShipUseCase;
        this.startPlayerJourneyUseCase = startPlayerJourneyUseCase;
        this.tradeWithIslandUseCase = tradeWithIslandUseCase;
        this.playerRepository = playerRepository;
    }

    @GetMapping
    public List<ShipResponse> getAll(
            @RequestParam(required = false) String islandId,
            @RequestParam(required = false) String playerId
    ) {
        List<Ship> ships;
        boolean hasIslandFilter = islandId != null && !islandId.isBlank();
        boolean hasPlayerFilter = playerId != null && !playerId.isBlank();

        if (hasIslandFilter && hasPlayerFilter) {
            ships = this.listShipsUseCase.getShipsAtIsland(islandId).stream()
                    .filter(ship -> playerId.equals(ship.getPlayerId()))
                    .toList();
        } else if (hasIslandFilter) {
            ships = this.listShipsUseCase.getShipsAtIsland(islandId);
        } else if (hasPlayerFilter) {
            ships = this.listShipsUseCase.getShipsForPlayer(playerId);
        } else {
            ships = this.listShipsUseCase.getAllShips();
        }

        return ships.stream()
                .map(this::toShipResponse)
                .toList();
    }

    @PostMapping("/{shipId}/trade/buy")
    public ShipResponse buyFromIsland(@PathVariable String shipId, @RequestBody TradeRequest request) {
        try {
            Ship updatedShip = this.tradeWithIslandUseCase.buyFromIsland(shipId, request.goodType(), request.amount());
            return this.toShipResponse(updatedShip);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{shipId}/trade/sell")
    public ShipResponse sellToIsland(@PathVariable String shipId, @RequestBody TradeRequest request) {
        try {
            Ship updatedShip = this.tradeWithIslandUseCase.sellToIsland(shipId, request.goodType(), request.amount());
            return this.toShipResponse(updatedShip);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{shipId}/journeys")
    public ShipResponse startJourney(@PathVariable String shipId, @RequestBody StartJourneyRequest request) {
        try {
            Ship updatedShip = this.startPlayerJourneyUseCase.startJourney(shipId, request.playerId(), request.targetIslandId());
            return this.toShipResponse(updatedShip);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping("/player-controlled")
    public CreatePlayerControlledShipResponse createPlayerControlledShip(@RequestBody CreatePlayerControlledShipRequest request) {
        try {
            CreatePlayerControlledShipUseCase.CreationResult created =
                    this.createPlayerControlledShipUseCase.create(request.shipName(), request.playerName());
            return CreatePlayerControlledShipResponse.fromResult(created);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping("/player-controlled/existing")
    public CreatePlayerControlledShipResponse createPlayerControlledShipForExistingPlayer(
            @RequestBody CreateForExistingPlayerRequest request
    ) {
        try {
            CreatePlayerControlledShipUseCase.CreationResult created =
                    this.createPlayerControlledShipUseCase.createForExistingPlayer(request.shipName(), request.playerId());
            return CreatePlayerControlledShipResponse.fromResult(created);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    public record CreatePlayerControlledShipRequest(String shipName, String playerName) {
    }

    public record CreateForExistingPlayerRequest(String shipName, String playerId) {
    }

    public record CreatePlayerControlledShipResponse(
            ShipResponse ship,
            CreatedPlayerResponse player
    ) {
        static CreatePlayerControlledShipResponse fromResult(CreatePlayerControlledShipUseCase.CreationResult result) {
            return new CreatePlayerControlledShipResponse(
                    ShipResponse.fromDomain(result.ship(), result.player().getHexColor()),
                    new CreatedPlayerResponse(
                            result.player().getId(),
                            result.player().getName(),
                            result.player().getHexColor()
                    )
            );
        }
    }

    public record CreatedPlayerResponse(String id, String name, String hexColor) {
    }

    public record TradeRequest(GoodType goodType, int amount) {
    }

    public record StartJourneyRequest(String playerId, String targetIslandId) {
    }

    public record ShipResponse(
            String id,
            String name,
            String islandId,
            JourneyResponse journey,
            String playerId,
            InventoryResponse inventory,
            String playerHexColor
    ) {
        static ShipResponse fromDomain(Ship ship, String playerHexColor) {
            return new ShipResponse(
                    ship.getId(),
                    ship.getName(),
                    ship.getIslandId(),
                    JourneyResponse.fromDomain(ship.getJourney()),
                    ship.getPlayerId(),
                    InventoryResponse.fromDomain(ship.getInventory()),
                    playerHexColor
            );
        }
    }

    public record JourneyResponse(
            String id,
            String shipId,
            String startIslandId,
            String targetIslandId,
            String departed,
            String arrived,
            String estimatedArrival,
            boolean active
    ) {
        static JourneyResponse fromDomain(Journey journey) {
            if (journey == null) {
                return null;
            }
            return new JourneyResponse(
                    journey.id(),
                    journey.shipId(),
                    journey.startIslandId(),
                    journey.targetIslandId(),
                    asIsoString(journey.departed()),
                    asIsoString(journey.arrived()),
                    asIsoString(journey.estimatedArrival()),
                    journey.active()
            );
        }

        private static String asIsoString(java.time.Instant value) {
            return value == null ? null : value.toString();
        }
    }

    private ShipResponse toShipResponse(Ship ship) {
        String playerHexColor = null;
        if (ship.getPlayerId() != null && !ship.getPlayerId().isBlank()) {
            Optional<com.example.rtnt.domain.player.Player> player = this.playerRepository.findById(ship.getPlayerId());
            if (player != null) {
                playerHexColor = player.map(com.example.rtnt.domain.player.Player::getHexColor).orElse(null);
            }
        }
        return ShipResponse.fromDomain(ship, playerHexColor);
    }

    public record InventoryResponse(int gold, int rum, int sugar, int spices, int tobacco) {
        static InventoryResponse fromDomain(Inventory inventory) {
            Inventory safeInventory = inventory == null ? Inventory.empty() : inventory;
            return new InventoryResponse(
                    safeInventory.getAmount(GoodType.GOLD),
                    safeInventory.getAmount(GoodType.RUM),
                    safeInventory.getAmount(GoodType.SUGAR),
                    safeInventory.getAmount(GoodType.SPICES),
                    safeInventory.getAmount(GoodType.TOBACCO)
            );
        }
    }
}
