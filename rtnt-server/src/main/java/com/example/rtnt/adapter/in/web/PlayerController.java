package com.example.rtnt.adapter.in.web;

import com.example.rtnt.domain.player.Player;
import com.example.rtnt.domain.player.PlayerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @GetMapping
    public List<PlayerResponse> getAll() {
        return this.playerRepository.findAll().stream()
                .map(PlayerResponse::fromDomain)
                .toList();
    }

    public record PlayerResponse(String id, String name, String hexColor) {
        static PlayerResponse fromDomain(Player player) {
            return new PlayerResponse(player.getId(), player.getName(), player.getHexColor());
        }
    }
}
