package goorm.humandelivery.customer.application.port.out;

import goorm.humandelivery.customer.domain.FavoriteDestination;

import java.util.List;

public interface FavoriteDestinationPort {
    FavoriteDestination save(FavoriteDestination favoriteDestination);
    List<FavoriteDestination> findByCustomerId(Long customerId);
    FavoriteDestination findById(Long id);
    void deleteByIdAndCustomerId(Long id, Long customerId);
} 