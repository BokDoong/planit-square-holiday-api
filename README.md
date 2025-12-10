## 개요

### 프로젝스 설명

이 서비스는 Nager Public Holidays API를 활용해 국가/연도별 공휴일 데이터를 적재·조회·삭제·재동기화하는 Holiday API입니다.

### 기술 스택

	•	Spring Boot 3.x
	•	Java 21
	•	Spring Data JPA
	•	H2
	•	Swagger UI
	•	JUnit 5

<br></br>

## 빌드 & 실행 방법

### 사전 준비

- Java 21
- Git

### 프로젝트 클론

```bash
git clone https://github.com/BokDoong/planit-square-holiday-api.git
cd planit-square-holiday-api
```

### 빌드

프로젝트에는 Gradle Wrapper가 포함되어 있으므로 별도 설치 없이 아래 명령으로 빌드할 수 있습니다.

```bash
# macOS / Linux
./gradlew clean build

# Windows
gradlew clean build
```

성공 시, 실행 가능한 Jar 파일이 build/libs 아래에 생성됩니다.

```bash
build/libs/
  └── holiday-service-0.0.1-SNAPSHOT.jar
```

### 애플리케이션 실행

서버 기본 포트는 8080입니다.

```bash
# 실행
java -jar build/libs/holiday-service-0.0.1-SNAPSHOT.jar
```
<br></br>

## 테스트 실행

아래 명령으로 전체 테스트를 실행합니다.

```bash
./gradlew clean test
```

총 53개의 테스트 작성, 과제 요구사항인 ./gradlew clean test 성공 스크린샷은 아래와 같습니다.

![image.png](attachment:34d6ab3a-0cf4-4b16-a6a7-4fe23130d1a4:image.png)

<br></br>

## API 명세

### Swagger UI

http://localhost:8080/swagger-ui/index.html

### 요약

설계 및 구현한 API 목록입니다. API별 요청, 응답, 구현 의도 등 상세 설명은 아래 상세 설명에 기재하였습니다.

| **URL** | **메서드** | **기능** |
| --- | --- | --- |
| /api/v1/holidays/sync | POST | 연도·국가별 공휴일 데이터 적재 |
| /api/v1/holidays/refresh | POST | 특정 연도·국가의 공휴일 덮어쓰기 |
| /api/v1/holidays | GET | 연도별·국가별 필터 기반 공휴일 조회 |
| /api/v1/holidays | DELETE | 특정 연도·국가의 공휴일 삭제 |
| /api/v1/countries | GET | 전체 국가 조회 |
| - | - | 매년 1월 2일 기준 전년도·금년도 나라별 공휴일 동기화 |

### 응답 구조 ( 공통 )

API 요청에 대해 응답 코드와 응답 필드로 구성된 응답을 제공합니다. 응답 필드는 각 API의 호스트 및 요청 성공 여부에 따라 구성이 다릅니다.

- **요청 성공 시**: HTTP 상태 코드 및 API별 성공 응답 반환
- **요청 실패 시**: HTTP 상태 코드 및 `JSON` 형식의 에러 응답 필드 반환, 에러 응답 필드에 커스텀 에러 코드 및 메시지 포함

아래는 에러 응답 필드 구성 및 예시입니다.

- Error Response 공통 스키마
    
    
    | **이름** | **타입** | **설명** |
    | --- | --- | --- |
    | code | String | 에러 코드 |
    | message | String | 에러 메세지 |
    | fieldErrorInfos | List<FieldErrorInfo> | 입력값 검증 실패 시 필드별 오류 정보 목록 |
- FieldErrorInfo ( 필수 X )
    
    
    | **이름** | **타입** | **설명** |
    | --- | --- | --- |
    | field | String | 에러 입력 필드, 복합인 경우 서버에서 지정 |
    | reason | String | 에러 원인 |

예: 공휴일 조회 시, 국가 코드가 비어있는 경우 + 시작 날짜가 끝 날짜 보다 뒷 날짜인 경우

```json
HTTP/1.1 400 Bad Request

{
    "code": "H-001",
    "message": "잘못된 요청 데이터입니다.",
    "fieldErrorInfos": [
        {
            "field": "countryCode",
            "reason": "공백일 수 없습니다"
        },
        {
            "field": "validRange",
            "reason": "from 은 to 보다 이후일 수 없습니다."
        }
    ]
}
```

### 에러 코드

| code | 설명 | HTTP 상태코드 |
| --- | --- | --- |
| `H-001` | 잘못된 요청 데이터입니다. | 400 |
| `H-002`  | 요청한 리소스를 찾을 수 없습니다. | 404 |
| `H-003` | 서버 내부 오류가 발생했습니다. | 500 |
| `H-004` | 허용되지 않은 연도 범위입니다. 2021~2025 내로 입력해주세요. | 400 |

각 API에서 발생할 수 있는 커스텀 에러 코드입니다. 
요청 성공 시 2xx HTTP 상태 코드와 함께 요청에 대한 응답 본문이 반환되고, 요청이 실패한 경우 상단의 기재한 실패 응답 예시에서 code 필드에 커스텀 에러 코드가 담겨 응답됩니다.

<br></br>


## API 상세

### `POST /api/v1/holidays/sync`

**설명**

- 최근 5 년(2021 ~ 2025)의 공휴일을 Nager API에서 수집하여 저장합니다.
- 기존의 데이터가 있다면 덮어씌우며, 성공했다면 적재한 나라와 공휴일의 총 개수를 응답합니다.

**구현 의도**

- 동기화 순서 및 트랜잭션을 “국가 → 국가마다 5년치 공휴일 반복”순으로 분리하였습니다. 동기화 과정을 추적하기 위해 각 순서와 트랜잭션마다 INFO 레벨로 로그를 남겼습니다.
- 외부 API 호출은 트랜잭션 외부에서 수행하여, 네트워크 이슈가 트랜잭션에 영향을 주지 않도록 했습니다.
- (country, date, localName) 기준의 중복 데이터 무결성을 보장하기 위해, 전체 기간을 delete 후 재삽입하는 전략을 택했습니다. 그 과정에서 JPA bulk delete는 즉시 flush 되지 않아 유니크 제약 오류가 발생할 수 있어, 명시적으로 flush 하도록 조정했습니다. upsert도 고려했으나 과제 범위에서는 단순성과 일관성을 위해 전체 replace 전략을 선택했습니다.

**요청**

- 조건 없음

**응답**

- 본문
    
    
    | **이름** | Type | **설명** |
    | --- | --- | --- |
    | countriesCount | Integer | 동기화된 나라 수 |
    | holidaysCount | Integer | 동기화된 공휴일 수 |
- 예시
    
    ```json
    {
        "countriesCount": 119,
        "holidaysCount": 8277
    }
    ```
    

**실패**

- Nager API와 통신 장애 or 서버 내부 오류
    
    ```json
    HTTP/1.1 500 Internal Server Error
    {
        "code": "H-003",
        "message": "서버 내부 오류가 발생했습니다.",
        "fieldErrorInfos": []
    }
    ```
    

### `POST /api/v1/holidays/refresh`

**설명**

- 특정 연도·국가 데이터를 재호출하여 Upsert(덮어쓰기)를 진행합니다.
- 기존의 데이터가 있다면 덮어씌우며, 성공했다면 적재한 공휴일의 총 개수를 응답합니다.

**구현 의도**

- 적재하는 방식과 동일하게 delete 후 재삽입하는 방식으로 구현했습니다.

**요청**

- 본문
    
    
    | **이름** | Type | **설명** | 필수 유무 |
    | --- | --- | --- | --- |
    | countryCode | String | 국가 코드 | O |
    | year | Integer | 연도 | O |

**응답**

- 본문
    
    
    | **이름** | Type | **설명** |
    | --- | --- | --- |
    | holidaysCount | Integer | 동기화된 공휴일 수 |
- 예시
    
    ```json
    {
        "holidaysCount": 11
    }
    ```
    

**실패**

- 국가 코드나 연도가 비어있는 경우
    
    ```json
    HTTP/1.1 400 Bad Request
    {
        "code": "H-001",
        "message": "잘못된 요청 데이터입니다.",
        "fieldErrorInfos": [
            {
                "field": "countryCode",
                "reason": "공백일 수 없습니다"
            },
            {
                "field": "year",
                "reason": "널이어서는 안됩니다"
            }
        ]
    }
    ```
    
- 입력한 국가 코드에 대한 국가가 DB에 없는 경우
    
    ```json
    HTTP/1.1 404 Not Found
    {
        "code": "H-002",
        "message": "요청한 리소스를 찾을 수 없습니다.",
        "fieldErrorInfos": []
    }
    ```
    
- 입력한 연도가 최근 5년(2021 ~ 2025)년의 연도가 아닌 경우
    
    ```json
    HTTP/1.1 400 Bad Request
    {
        "code": "H-004",
        "message": "허용되지 않은 연도 범위입니다. 2021~2025 내로 입력해주세요.",
        "fieldErrorInfos": []
    }
    ```
    

### `GET /api/v1/holidays`

**설명**

- 국가 코드와 기간(연도 또는 from~to) 및 공휴일 타입을 기준으로 공휴일 목록을 페이징 조회합니다.
- JPA 페이징을 기반으로 구현했으며, 공휴일 목록과 함께 페이지네이션 메타데이터가 함께 반환됩니다.

**구현 의도**

- Holiday 조회는 country_id 선별 후 날짜 범위를 필터링하는 형태로 수행됩니다. 이에 (country_id, date) 복합 인덱스로 설계해 날짜 범위 검색에 대해, 인덱스 Range Scan으로 처리할 수 있도록 했습니다.
- 날짜 필터는 연도(year)와 구간(from/to)을 함께 지원하며, 둘 다 없을 경우 “최근 5년”을 기본 검색 범위로 사용하는 전략을 택했습니다.

**요청**

- 쿼리 파라미터
    
    
    | **이름** | Type | **설명** | 필수 유무 |
    | --- | --- | --- | --- |
    | countryCode | String | 조회할 국가 코드 (예: KR) | O |
    | year | Integer | 조회 연도. from, to가 없을 때만 사용되며, 해당 연도 전체(1/1~12/31)를 조회 | X |
    | from | LocalDate | 조회 시작일 (yyyy-MM-dd). 
    to와 함께 사용되며, 하나만 주어지면 나머지는 기본값 사용 | X |
    | to | LocalDate | 조회 종료일 (yyyy-MM-dd).
    from와 함께 사용되며, 하나만 주어지면 나머지는 기본값 사용 | X |
    | type | HolidayType | 공휴일 타입 필터 | X |
    | page | Integer | 페이지 번호 (0부터 시작), 기본값: 0 | X |
    | size | Integer | 페이지 크기, 기본값: 20 | X |
- 날짜 필터링 동작 규칙
    - from/to가 둘 다 null이 아니면
        - from ~ to 그대로 사용
        - 단, from > to 인 경우 @AssertTrue 검증 실패로 400 반환
    - from/to 중 하나만 주어진 경우
        - from == null → 최근 5년의 시작 연도 1월 1일을 기본값으로 사용
        - to == null   → 최근 5년의 종료 연도 12월 31일을 기본값으로 사용
    - from/to 모두 null이고, year가 존재하는 경우
        - 해당 연도의 1월 1일 ~ 12월 31일로 범위 설정
    - year, from, to 모두 null인 경우
        - 최근 5년 전체 기간을 기본 조회 범위로 사용
- 타입 규칙
    - 외부 API Docs에 있는 공휴일 타입을 Enum으로 변환, 해당 타입이 포함되어 있는 공휴일 조회
    - 종류
        - PUBLIC
        - BANK
        - SCHOOL
        - AUTHORITIES
        - OPTIONAL
        - OBSERVANCE

**응답**

- 본문 ( 페이징 정보 제외 )
    
    
    | **이름** | **Type** | **설명** |
    | --- | --- | --- |
    | id | Long | 공휴일 ID |
    | date | LocalDate | 공휴일 날짜 |
    | localName | String | 현지어 공휴일명 |
    | name | String | 영어 공휴일명 |
    | global | boolean | 글로벌 공휴일 여부 |
    | fixed | boolean | 고정일 여부(매년 같은 날짜) |
    | launchYear | Integer | 해당 공휴일이 시작된 연도 (없을 수 있음) |
    | types | List<String> | 공휴일 타입 목록 |
    | counties | List<String> | 특정 연방주/지역에 한정된 경우 지역 목록 |
- 예시
    
    ```json
    HTTP/1.1 200 OK
    {
        "content": [
            {
                "id": 4596,
                "date": "2025-10-06",
                "localName": "추석",
                "name": "Chuseok",
                "global": true,
                "fixed": false,
                "launchYear": null,
                "types": [
                    "PUBLIC"
                ],
                "counties": []
            },
            {
                "id": 4597,
                "date": "2025-10-07",
                "localName": "추석",
                "name": "Chuseok",
                "global": true,
                "fixed": false,
                "launchYear": null,
                "types": [
                    "PUBLIC"
                ],
                "counties": []
            },
            ...
        ],
        "pageable": {
            "pageNumber": 1,
            "pageSize": 10,
            "sort": {
                "empty": true,
                "sorted": false,
                "unsorted": true
            },
            "offset": 10,
            "paged": true,
            "unpaged": false
        },
        "last": true,
        "totalElements": 15,
        "totalPages": 2,
        "first": false,
        "size": 10,
        "number": 1,
        "sort": {
            "empty": true,
            "sorted": false,
            "unsorted": true
        },
        "numberOfElements": 5,
        "empty": false
    }
    ```
    

**실패**

- 국가 코드가 비어있는 경우
    
    ```json
    HTTP/1.1 400 Bad Request
    {
        "code": "H-001",
        "message": "잘못된 요청 데이터입니다.",
        "fieldErrorInfos": [
            {
                "field": "countryCode",
                "reason": "공백일 수 없습니다"
            }
        ]
    }
    ```
    
- to, from이 둘 다 있을 때, 시작 날짜가 끝 날짜 보다 뒷 날짜인 경우
    
    ```json
    HTTP/1.1 400 Bad Request
    
    {
        "code": "H-001",
        "message": "잘못된 요청 데이터입니다.",
        "fieldErrorInfos": [
            {
                "field": "validRange",
                "reason": "from 은 to 보다 이후일 수 없습니다."
            }
        ]
    }
    ```
    
- 입력한 연도가 최근 5년(2021 ~ 2025)년의 연도가 아닌 경우
    
    ```json
    HTTP/1.1 400 Bad Request
    {
        "code": "H-004",
        "message": "허용되지 않은 연도 범위입니다. 2021~2025 내로 입력해주세요.",
        "fieldErrorInfos": []
    }
    ```
    

### `DELETE /api/v1/holidays`

**설명**

- 특정 연도·국가 데이터를 삭제합니다.

**요청**

- 본문
    
    
    | **이름** | Type | **설명** | 필수 유무 |
    | --- | --- | --- | --- |
    | countryCode | String | 국가 코드 | O |
    | year | Integer | 연도 | O |

**응답**

- 본문
    
    
    | **이름** | Type | **설명** |
    | --- | --- | --- |
    | holidaysCount | Integer | 삭제된 공휴일 수 |
- 예시
    
    ```json
    {
        "holidaysCount": 11
    }
    ```
    

**실패**

- 국가 코드나 연도가 비어있는 경우
    
    ```json
    HTTP/1.1 400 Bad Request
    {
        "code": "H-001",
        "message": "잘못된 요청 데이터입니다.",
        "fieldErrorInfos": [
            {
                "field": "countryCode",
                "reason": "공백일 수 없습니다"
            },
            {
                "field": "year",
                "reason": "널이어서는 안됩니다"
            }
        ]
    }
    ```
    
- 입력한 국가 코드에 대한 국가가 DB에 없는 경우
    
    ```json
    HTTP/1.1 404 Not Found
    {
        "code": "H-002",
        "message": "요청한 리소스를 찾을 수 없습니다.",
        "fieldErrorInfos": []
    }
    ```
    
- 입력한 연도가 최근 5년(2021 ~ 2025)년의 연도가 아닌 경우
    
    ```json
    HTTP/1.1 400 Bad Request
    {
        "code": "H-004",
        "message": "허용되지 않은 연도 범위입니다. 2021~2025 내로 입력해주세요.",
        "fieldErrorInfos": []
    }
    ```
    

### `GET /api/v1/countries`

**설명**

- DB에 있는 전체 국가 코드를 조회합니다.

**구현 의도**

- 재동기화(refresh)를 진행하거나 조회를 위해서는 전체 시스템을 보았을 때, 현재 DB에 어떠한 국가가 있는지 알 수 있는 API가 있어야 한다고 생각하여 추가적으로 구현했습니다.

**요청**

- 조건 없음

**응답**

- 본문
    
    아래의 응답이 담긴 객체가 리스트로 응답합니다.
    
    | **이름** | Type | **설명** |
    | --- | --- | --- |
    | code | String | 국가 코드 |
    | name | String | 국가명 |
- 예시
    
    ```json
    [
        {
            "code": "AD",
            "name": "Andorra"
        },
        {
            "code": "AL",
            "name": "Albania"
        },
        {
            "code": "AM",
            "name": "Armenia"
        },
        ...
    ]
    ```
    

### `1월 2일 기준 배치 작업`

**설명**

- @Scheduled를 활용하여 1월 2일에 금년/작년의 국가와 공휴일 데이터를 재동기화하도록 구현했습니다.

**구현 의도**

- 동기화 과정에서 외부 API 호출은 트랜잭션 외부에서 수행하여, 네트워크 장애가 내부 트랜잭션에 영향을 주지 않도록 했습니다.
- 과제 규모 및 단일 서버 구동을 고려해 단순한 @Scheduled 전략을 사용했으며, 다중 서버 환경에서는 ShedLock·Redis 기반 분산락·cron 작업 등을 통해 단일 실행 보장을 적용하는 것이 적절합니다.
- 작업이 실패할 가능성을 고려하여 로그 기반의 실패 추적이 가능하며, 실무라면 배치 이력 테이블을 두어 재시도 전략과 모니터링을 개선할 수 있다고 생각합니다.
