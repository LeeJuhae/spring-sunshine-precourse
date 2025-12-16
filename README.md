### 1) API 개요

- **Endpoint**: `GET /api/weather/{city}`
- **입력**: `city` (Path Variable) — 예: `seoul`, `busan`, `tokyo`
- **출력**: `String` (텍스트)
    - 기본 모드: 템플릿 기반 “날씨 요약 + 옷차림 추천”
    - LLM 모드: LLM이 생성한 “날씨 요약 + 옷차림 추천” (구조화 출력 → 합쳐서 반환)


### 2) 구현 방식(흐름)

요청이 들어오면 아래 순서로 처리됩니다.

1. **Controller**  
   - `WeatherController`가 `/api/weather/{city}` 요청을 받고 `WeatherService`에 위임합니다.

2. **도시 → 좌표 변환(Geocoding)**  
   - `CityResolver`가 사용자가 입력한 도시명을 `City(name, latitude, longitude)`로 변환합니다.
   - 구현체 `LlmCityResolver`는 LLM을 이용해 좌표를 추론하고, 간단한 in-memory 캐시로 반복 요청을 줄입니다.

3. **Open-Meteo로 현재 날씨 조회**
   - `OpenMeteo`가 `https://api.open-meteo.com/v1/forecast`를 호출해 현재 날씨를 받아옵니다.
   - 현재 사용 필드:
     - `temperature_2m`, `apparent_temperature`, `weather_code`, `relative_humidity_2m`, `wind_speed_10m`

4. **응답 생성(LLM ON/OFF)**
   - `sunshine.llm.enabled` 설정에 따라 분기합니다.
     - `false`: 규칙/템플릿 기반 문장 생성
     - `true`: `LlmWeatherAdvisor`가 LLM으로 요약/옷차림을 생성  
       (단, **구조화 출력(BeanOutputConverter)** 으로 파싱 가능하게 만들고, 두 문장을 합쳐 반환)

---

### 3) 코드 개선

#### (1) Controller는 얇게, Service로 책임 이동
- Controller에서는 입력만 받고 비즈니스 로직(좌표 변환, 외부 API 호출, 문장 생성)은 모두 Service 레이어로 모았습니다.
- 결과적으로 테스트/확장이 쉬워지고, 웹 레이어가 단순해졌습니다.

#### (2) 외부 API 호출을 전용 컴포넌트로 분리 (`OpenMeteo`)
- `RestClient` + `UriComponentsBuilder`로 URL/쿼리를 안전하게 구성했습니다.
- 응답이 `null`이거나 `current`가 비어있는 경우 예외를 명확히 던지도록 처리했습니다.

#### (3) LLM 기능을 “옵션”으로 설계 (Feature Toggle)
- `sunshine.llm.enabled` 값으로 LLM 사용 여부를 쉽게 켜고 끌 수 있게 했습니다.
- LLM을 끄면 **완전한 규칙 기반**으로도 동작하도록 만들어 “LLM 장애/비용”에 대한 리스크를 낮췄습니다.

#### (4) LLM 출력은 구조화(파싱 가능한 형태)로 강제
- `LlmWeatherAdvisor` / `LlmCityResolver` 모두 **BeanOutputConverter 기반 포맷**을 사용해
    - “문장만 잔뜩 출력하는” 형태를 피하고
    - DTO로 안정적으로 변환되도록 했습니다.

#### (5) LLM 비용 추정/관측 가능성(Observability) 추가
- `LlmCostProperties` + `LlmCostEstimator`로 토큰 사용량을 비용(USD)로 추정합니다.
- `LlmWeatherAdvisor`에서 요청별로 모델/토큰/추정비용을 로그로 남겨,
    - “기능은 되는데 비용이 얼마인지 모르는 상태”를 피했습니다.

---

### 4) 학습한 내용

- **레이어링 감각**
    - Controller는 HTTP 입출력에 집중, Service는 유스케이스/조합 로직, 외부 연동은 전용 컴포넌트로 분리.
- **외부 API 연동 기본기**
    - `RestClient` 사용, URI 구성, 응답 null 처리, 예외 래핑 등.
- **LLM을 제품 기능으로 붙일 때의 패턴**
    - 토글로 켜고 끄기(안전장치)
    - 구조화 출력으로 “파싱 가능한 결과” 만들기
    - 관측(토큰/비용 로그)으로 운영 가능하게 만들기
- **간단 캐시의 가치**
    - 도시 좌표는 자주 반복되므로, 작은 캐시만으로도 LLM 호출을 줄여 응답속도/비용을 동시에 개선 가능.

---

### 5) 설정 값

`src/main/resources/application.yml`에서 제어합니다.

- LLM 사용 여부
    - `sunshine.llm.enabled: true|false`
- LLM 비용 추정 단가
    - `app.llm-cost.input-per-1k`
    - `app.llm-cost.output-per-1k`
- (예시) LLM API Key는 실제 값 대신 플레이스홀더로 관리
    - `spring.ai.google.genai.api-key: {YOUR_API_KEY}`

---

### 6) 다음 개선 아이디어

- API 응답을 `String` 대신 **JSON DTO**로 변경(클라이언트 사용성 향상)
- 예외를 `@ControllerAdvice`로 모아 HTTP 상태코드/에러 포맷 통일
- 캐시를 Spring Cache(Caffeine 등)로 교체(만료/크기 제한)
- 도시 좌표는 LLM 대신 실제 지오코딩 API로 대체(정확도 향상)# spring-sunshine-precourse