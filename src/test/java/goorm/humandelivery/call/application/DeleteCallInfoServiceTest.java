package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeleteCallInfoServiceTest {

    @Autowired
    DeleteCallInfoService deleteCallInfoService;

    @Autowired
    SaveCallInfoPort saveCallInfoPort;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @AfterEach
    void tearDown() {
        jpaCallInfoRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("콜 ID를 통해 콜을 삭제할 수 있다.")
    void deleteCallById() throws Exception {
        // Given
        CallInfo savedCall = saveCallInfoPort.save(new CallInfo(null, null, null, null, null));
        Long target = savedCall.getId();

        // When
        deleteCallInfoService.deleteCallById(target);

        // Then
        List<CallInfo> result = jpaCallInfoRepository.findAll();
        assertThat(result).isEmpty();
    }

}