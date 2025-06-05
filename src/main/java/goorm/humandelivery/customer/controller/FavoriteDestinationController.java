package goorm.humandelivery.customer.controller;

import goorm.humandelivery.customer.application.dto.FavoriteDestinationDto;
import goorm.humandelivery.customer.application.port.in.FavoriteDestinationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerId}/favorite-destinations")
public class FavoriteDestinationController {

    private final FavoriteDestinationUseCase favoriteDestinationUseCase;

    @PostMapping
    public ResponseEntity<FavoriteDestinationDto.Response> createFavoriteDestination(
            @PathVariable Long customerId,
            @RequestBody FavoriteDestinationDto.CreateRequest request) {
        return ResponseEntity.ok(favoriteDestinationUseCase.createFavoriteDestination(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDestinationDto.Response>> getFavoriteDestinations(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(favoriteDestinationUseCase.getFavoriteDestinations(customerId));
    }

    @PutMapping("/{destinationId}")
    public ResponseEntity<FavoriteDestinationDto.Response> updateFavoriteDestination(
            @PathVariable Long customerId,
            @PathVariable Long destinationId,
            @RequestBody FavoriteDestinationDto.UpdateRequest request) {
        return ResponseEntity.ok(favoriteDestinationUseCase.updateFavoriteDestination(customerId, destinationId, request));
    }

    @DeleteMapping("/{destinationId}")
    public ResponseEntity<Void> deleteFavoriteDestination(
            @PathVariable Long customerId,
            @PathVariable Long destinationId) {
        favoriteDestinationUseCase.deleteFavoriteDestination(customerId, destinationId);
        return ResponseEntity.noContent().build();
    }
} 