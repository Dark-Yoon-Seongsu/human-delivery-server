package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.application.dto.FavoriteDestinationDto;
import goorm.humandelivery.customer.application.port.in.FavoriteDestinationUseCase;
import goorm.humandelivery.customer.application.port.out.FavoriteDestinationPort;
import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.domain.FavoriteDestination;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteDestinationService implements FavoriteDestinationUseCase {

    private final FavoriteDestinationPort favoriteDestinationPort;
    private final LoadCustomerPort loadCustomerPort;

    @Override
    @Transactional
    public FavoriteDestinationDto.Response createFavoriteDestination(Long customerId, FavoriteDestinationDto.CreateRequest request) {
        Customer customer = loadCustomerPort.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        FavoriteDestination favoriteDestination = FavoriteDestination.create(
                customer,
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );

        FavoriteDestination saved = favoriteDestinationPort.save(favoriteDestination);
        return convertToResponse(saved);
    }

    @Override
    public List<FavoriteDestinationDto.Response> getFavoriteDestinations(Long customerId) {
        return favoriteDestinationPort.findByCustomerId(customerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FavoriteDestinationDto.Response updateFavoriteDestination(Long customerId, Long destinationId, FavoriteDestinationDto.UpdateRequest request) {
        FavoriteDestination favoriteDestination = favoriteDestinationPort.findById(destinationId);

        if (!favoriteDestination.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Not authorized to update this destination");
        }

        favoriteDestination.update(
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );

        return convertToResponse(favoriteDestination);
    }

    @Override
    @Transactional
    public void deleteFavoriteDestination(Long customerId, Long destinationId) {
        favoriteDestinationPort.deleteByIdAndCustomerId(destinationId, customerId);
    }

    private FavoriteDestinationDto.Response convertToResponse(FavoriteDestination favoriteDestination) {
        return new FavoriteDestinationDto.Response(
                favoriteDestination.getId(),
                favoriteDestination.getName(),
                favoriteDestination.getAddress(),
                favoriteDestination.getLatitude(),
                favoriteDestination.getLongitude()
        );
    }
} 