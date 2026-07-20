# Notification 프로젝트 디렉터리 구조

현재 프로젝트는 별도의 `notification` 하위 디렉터리로 나뉘어 있지 않고, 다음과 같은 패키지 구조로 구성되어 있습니다.

- `consumer`: 알림 수신부
- `event`, `service`, `delivery`: 알림 처리 관련 기능
- `push`: 푸시 알림 관련 기능
- `config`, `dto`, `domain`, `repository` 등: 공통 및 기능별 구성

현재 `producer`라는 이름의 디렉터리는 확인되지 않습니다.
