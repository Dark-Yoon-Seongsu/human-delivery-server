package goorm.humandelivery.domain.repository;

import goorm.humandelivery.call.domain.CallInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallInfoRepository extends JpaRepository<CallInfo, Long> {


}
