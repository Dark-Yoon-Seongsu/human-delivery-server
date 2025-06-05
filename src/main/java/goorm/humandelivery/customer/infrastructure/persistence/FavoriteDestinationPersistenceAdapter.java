package goorm.humandelivery.customer.infrastructure.persistence;

import goorm.humandelivery.customer.application.port.out.FavoriteDestinationPort;
import goorm.humandelivery.customer.domain.FavoriteDestination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FavoriteDestinationPersistenceAdapter implements FavoriteDestinationPort {

    private final FavoriteDestinationRepository favoriteDestinationRepository;

    @Override
    public FavoriteDestination save(FavoriteDestination favoriteDestination) {
        return favoriteDestinationRepository.save(favoriteDestination);
    }

    @Override
    public List<FavoriteDestination> findByCustomerId(Long customerId) {
        return favoriteDestinationRepository.findByCustomerId(customerId);
    }

    @Override
    public Optional<FavoriteDestination> findById(Long id) {
        return favoriteDestinationRepository.findById(id);
    }

    @Override
    public void deleteByIdAndCustomerId(Long id, Long customerId) {
        favoriteDestinationRepository.deleteByIdAndCustomerId(id, customerId);
    }
} 