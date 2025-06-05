package goorm.humandelivery.customer.application.port.in;

import goorm.humandelivery.customer.application.dto.FavoriteDestinationDto;

import java.util.List;

public interface FavoriteDestinationUseCase {
    FavoriteDestinationDto.Response createFavoriteDestination(Long customerId, FavoriteDestinationDto.CreateRequest request);
    List<FavoriteDestinationDto.Response> getFavoriteDestinations(Long customerId);
    FavoriteDestinationDto.Response updateFavoriteDestination(Long customerId, Long destinationId, FavoriteDestinationDto.UpdateRequest request);
    void deleteFavoriteDestination(Long customerId, Long destinationId);
} 