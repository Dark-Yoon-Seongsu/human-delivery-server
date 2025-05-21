package goorm.humandelivery.shared.messaging;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,         // 클래스 이름으로 타입 구분
        include = JsonTypeInfo.As.PROPERTY,  // 속성으로 타입 저장
        property = "@class"                  // 저장될 필드명
)
public abstract class QueueMessage { }
