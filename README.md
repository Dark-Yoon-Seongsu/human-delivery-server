# 실시간 택시 배차 시스템 
<p align="center">
  <img src="imageV2.png" alt="인간배달 로고" width="400"/>
</p>

## 발표자료
[발표 PPT](https://www.canva.com/design/DAGohi0fHnk/ibI4ZPlvw_sP7NI_7s_9Cg/edit)

## API 문서
[인간배달 V2(HTTP).postman_collection.json](../../../Downloads/%EC%9D%B8%EA%B0%84%EB%B0%B0%EB%8B%AC%20V2%28HTTP%29.postman_collection.json)

## 팀원 소개

| 이름  | 역할             | GitHub                         |
|-----|----------------|--------------------------------|
| 박성근 | 백엔드 개발         | https://github.com/p-seonggeun |
| 이한결 | 백엔드 개발         | https://github.com/hangyeoli    |
| 김윤영 | 백엔드 개발         | https://github.com/yunrry      |
| 이 수 | 택시&승객 클라이언트 개발 | https://github.com/SooLee99       |


##  프로젝트 개요   
이 프로젝트는 기존 [인간배달](https://github.com/humandelivery/backend) 프로젝트를 기반으로,
실시간 택시 배차 시스템을 분석하고 리팩토링하여 유지보수성과 성능을 개선하는 것을 목표로 합니다.
기존 시스템은 WebSocket을 통한 실시간 통신과 Redis를 활용한 위치 정보 처리를 포함하고 있으며,
메시지 큐를 통해 콜 요청을 분산 처리합니다.
저희팀은  기존 기능에서 콜취소, 요금계산, 즐겨찾기 등의 기능을 추가하였으며,
헥사고날 아키텍처를 적용하여 시스템을 모듈화하고, 테스트 코드를 작성하여 코드 품질을 높이는 데 중점을 두었습니다.
또한 승객&기사 android 어플리케이션을 개발하여 실제 사용 가능한 시스템을 구현합니다.

---

##  주요 목표 및 기술 스택
- **메시지 큐 성능 비교**: Kafka, RabbitMQ, BlockingQueue 등
- **부하 테스트**: 자체 자바 프로세스 + k6
- **모니터링 시스템**: Spring Actuator + Prometheus + Grafana
- **WebSocket + STOMP + JWT 인증 처리**
- **Redis 기반 캐시 및 위치 정보 처리**

사용 기술: `Spring Boot`, `Kafka`, `Redis`, `MySQL`, `k6`, `Prometheus`, `Grafana`, `RabbitMQ`, `Docker`, `JUnit`, `EC2', `ECS`, `Android`, 'WebSocket`, `STOMP`, `JWT`

---

##  시스템 아키텍처
- **WebSocket 기반 실시간 양방향 통신**
- **STOMP 프로토콜을 통한 주제 기반 메시지 전송**
- **Redis GEO 자료구조로 반경 내 택시 탐색**
- **Kafka 기반 콜 요청 분산 처리**
- **JWT 기반 사용자 인증 및 Principal 설정**
- **Docker를 활용한 컨테이너화 및 배포**
- **GitHub Actions를 통한 CI/CD 파이프라인 구축**

---

##  전체 흐름 요약
1. **승객**은 출발지/도착지를 입력해 **콜 요청**을 전송
2. 서버는 요청을 **Kafka에 전송**, 메시지 소비 후 Redis에서 인접 택시 탐색
3. **택시기사**는 요청을 수신하고 수락 여부 결정 → 서버로 응답
4. 서버는 Redis의 `SET NX` 명령어로 선착순 배차 처리
5. 배차 완료 후 위치 정보는 WebSocket을 통해 주기적으로 주고받음
6. 모니터링 스케줄러가 비정상 상태를 감지하고 자동 취소 처리 가능

---

##  핵심 기술 설명




###  Redis + SET NX
- 콜 요청은 고유 키(`call:{callId}`)로 Redis에 저장
- 택시기사가 콜을 수락하면 `SET NX`로 해당 키에 자신의 ID 저장
- 먼저 저장한 기사만 배차 확정 → **동시성 제어 및 분산 락 구현**
- **Lua 스크립트**로 Redis 조회 + SET을 원자적으로 처리

###  WebSocket + STOMP + JWT
- WebSocket 연결 시 JWT를 헤더에 포함
- Spring Security `ChannelInterceptor`에서 토큰 검증 후 `Principal` 설정
- `@MessageMapping` 메서드에서 자동으로 사용자 정보 접근 가능
- `@SendToUser`로 특정 사용자에게 메시지 응답 가능

###  Kafka
- 다수의 서버 인스턴스에서 안정적으로 메시지 처리 가능
- Partition을 활용한 메시지 분산 처리 구조
- 처리량, 확장성, 장애 복구에 유리


###  테스트 & 배포자동화
- JUnit을 활용한 단위 테스트 및 통합 테스트 작성
- WebSocket 테스트를 위한 MockMvc 사용
- k6를 활용한 부하 테스트 스크립트 작성
- GitHub Actions로 CI/CD 파이프라인 구축
- 테스트 커버리지 80% 이상 달성
- 헥사고날 아키텍처로 모듈화된 테스트 구조
- Docker Compose로 로컬 개발 환경 구성
- ECS를 활용한 클라우드 배포
- EC2 인스턴스에서 Redis, Kafka, MySQL 등 서비스 운영


