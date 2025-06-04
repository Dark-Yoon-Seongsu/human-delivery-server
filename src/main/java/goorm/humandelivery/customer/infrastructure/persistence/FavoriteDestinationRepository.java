package goorm.humandelivery.customer.infrastructure.persistence;

import goorm.humandelivery.customer.domain.FavoriteDestination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteDestinationRepository extends JpaRepository<FavoriteDestination, Long> {
    List<FavoriteDestination> findByCustomerId(Long customerId);
    void deleteByIdAndCustomerId(Long id, Long customerId);
} 