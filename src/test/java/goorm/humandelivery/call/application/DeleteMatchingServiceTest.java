package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.application.port.out.SaveMatchingPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.call.infrastructure.persistence.JpaMatchingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeleteMatchingServiceTest {

    @Autowired
    DeleteMatchingService deleteMatchingService;

    @Autowired
    SaveMatchingPort saveMatchingPort;

    @Autowired
    SaveCallInfoPort saveCallInfoPort;

    @Autowired
    JpaMatchingRepository jpaMatchingRepository;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @AfterEach
    void tearDown() {
        jpaMatchingRepository.deleteAllInBatch();
        jpaCallInfoRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("콜 ID를 통해 배차를 삭제할 수 있다.")
    void deleteByCallId() throws Exception {
        // Given
        CallInfo savedCall = saveCallInfoPort.save(new CallInfo(null, null, null, null, null));
        Matching matching = Matching.builder()
                .id(null)
                .callInfo(savedCall)
                .taxiDriver(null)
                .build();
        Matching savedMatching = saveMatchingPort.save(matching);
        Long target = savedMatching.getCallInfo().getId();

        // When
        deleteMatchingService.deleteByCallId(target);

        // Then
        List<Matching> result = jpaMatchingRepository.findAll();
        assertThat(result).isEmpty();
    }

}