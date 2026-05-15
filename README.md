# Dev Activity Hub

개인 개발 활동을 수집, 정리, 검색, 리포팅하기 위한 Spring 백엔드 프로젝트다.

## 왜 만드는가
- 6년차 스프링 백엔드 개발자로서 실제 운영 가능한 개인 시스템을 만들기 위해
- GitHub 활동과 수기 작업 로그를 한 곳에서 관리하기 위해
- 주간 회고와 포트폴리오 공개용 데이터를 같은 원천 데이터에서 만들기 위해

## 프로젝트 사양
- Runtime: `Java 21`
- Framework: `Spring Boot 3.3.x`
- Database: `Supabase PostgreSQL`
- App schema: `dev_activity_hub`
- Git remote: `https://github.com/chonghg22/dev_activity_hub.git`
- Source root: `/Users/jack/project/dev-activity-hub-docs/devActivityHub`

## 핵심 기능
- 프로젝트 메타데이터 관리
- 수기 작업 로그 CRUD
- GitHub 활동 수집
- 통합 타임라인 조회
- 검색
- 주간 리포트
- 공개 대시보드용 읽기 전용 API

## 어떻게 사용하는가
1. 프로젝트 메타데이터와 공개 여부를 등록한다.
2. 수기 작업 로그를 기록한다.
3. GitHub 활동 동기화를 실행한다.
4. 타임라인과 검색 API로 활동을 조회한다.
5. 주간 리포트와 공개용 통계를 생성하거나 조회한다.

## 데이터 원칙
- 애플리케이션 테이블은 모두 `dev_activity_hub` 스키마 하위에 생성한다.
- 문서 저장소는 실행 데이터가 아니라 작업 컨텍스트를 관리한다.
- 공개 데이터는 프로젝트 공개 여부와 공개용 정책을 통과한 데이터만 노출한다.

## 현재 상태
- 문서 설계 진행 중
- DB 스키마 초안 작성 완료
- 패키지 구조 초안 작성 완료
- 실제 애플리케이션 코드는 다음 단계에서 생성 예정

## 다음 구현 우선순위
- Spring Boot 프로젝트 초기화
- Supabase 연결 설정
- `dev_activity_hub` 스키마 기반 migration 추가
- Project/ManualLog 기본 API 구현

## 문서 위치
- 설계 문서: `/Users/jack/project/dev-activity-hub-docs`
- 주요 문서: `MVP.md`, `DOMAIN.md`, `API.md`, `DB_SCHEMA.md`, `PACKAGE_STRUCTURE.md`
