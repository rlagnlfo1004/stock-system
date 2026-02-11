# 재고 시스템 (Stock Management System)

## 📋 프로젝트 개요

재고 관리 시스템의 동시성 제어를 학습하기 위한 프로젝트입니다. 
여러 사용자가 동시에 재고를 감소시킬 때 발생할 수 있는 Race Condition을 해결하기 위해 
다양한 Lock 메커니즘을 구현하고 비교합니다.

## 🛠 기술 스택

- **Java**: 21
- **Spring Boot**: 4.0.2
- **Spring Data JPA**
- **MySQL**: 재고 데이터 저장
- **Redis**: 분산 락 구현
  - Lettuce (Spring Session Data Redis)
  - Redisson
- **Lombok**: 보일러플레이트 코드 감소
- **JUnit 5**: 테스트 프레임워크

## 🎯 주요 기능

### 1. 재고 감소 (Decrease Stock)
재고를 감소시키는 핵심 기능으로, 다음과 같은 동시성 제어 방식을 구현:

### 2. 동시성 제어 메커니즘

#### ✅ Optimistic Lock (낙관적 락)
- **구현**: JPA `@Version` 어노테이션 사용
- **특징**: 
  - 충돌이 드물게 발생하는 환경에 적합
  - 버전 정보를 통한 충돌 감지
  - 실패 시 재시도 로직 구현
- **클래스**: `OptimisticLockStockFacade`, `OptimisticLockStockService`

#### ✅ Pessimistic Lock (비관적 락)
- **구현**: JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)` 사용
- **특징**:
  - 데이터베이스 레벨의 배타적 락
  - 충돌이 빈번한 환경에 적합
  - 성능 오버헤드 존재
- **클래스**: `PessimisticLockStockService`

#### ✅ Named Lock (네임드 락)
- **구현**: MySQL의 `GET_LOCK()`, `RELEASE_LOCK()` 함수 활용
- **특징**:
  - 사용자 정의 락 이름으로 동시성 제어
  - 트랜잭션 종료와 무관하게 명시적으로 락 해제 필요
  - 세션 레벨의 락
- **클래스**: `NamedLockStockFacade`, `LockRepository`

#### ✅ Lettuce를 이용한 Redis 분산 락
- **구현**: Spring Data Redis + Spin Lock 패턴
- **특징**:
  - Redis의 SETNX 명령어 활용
  - 락 획득 실패 시 재시도 (Spin Lock)
  - 간단한 구현, 하지만 CPU 사용량 증가 가능
- **클래스**: `LettuceLockStockFacade`, `RedisLockRepository`

#### ✅ Redisson을 이용한 Redis 분산 락
- **구현**: Redisson 라이브러리의 RLock
- **특징**:
  - Pub/Sub 기반으로 효율적인 락 대기
  - 락 획득 대기 시간과 만료 시간 설정 가능
  - Lettuce 대비 CPU 부하 감소
  - 프로덕션 환경 권장
- **클래스**: `RedissonLockStockFacade`

## 📂 프로젝트 구조

```
src/main/java/com/example/stock/
├── StockApplication.java           # Spring Boot 애플리케이션 진입점
├── domain/
│   └── Stock.java                  # 재고 엔티티 (JPA Entity)
├── facade/
│   ├── OptimisticLockStockFacade.java   # Optimistic Lock 재시도 로직
│   ├── NamedLockStockFacade.java        # Named Lock 처리
│   ├── LettuceLockStockFacade.java      # Lettuce 기반 분산 락
│   └── RedissonLockStockFacade.java     # Redisson 기반 분산 락
├── repository/
│   ├── StockRepository.java        # 재고 Repository
│   ├── LockRepository.java         # Named Lock Repository
│   └── RedisLockRepository.java    # Redis Lock Repository
└── service/
    ├── StockService.java           # 기본 재고 서비스
    ├── OptimisticLockStockService.java  # Optimistic Lock 서비스
    └── PessimisticLockStockService.java # Pessimistic Lock 서비스
```

## 🚀 실행 방법

### 사전 요구사항

1. **Java 21** 설치
2. **MySQL** 설치 및 실행
   ```bash
   # MySQL 데이터베이스 생성
   mysql -u root -p
   CREATE DATABASE stock_example;
   ```

3. **Redis** 설치 및 실행
   ```bash
   # macOS (Homebrew)
   brew install redis
   brew services start redis
   
   # 또는 Docker 사용
   docker run -d -p 6379:6379 redis
   ```

### 애플리케이션 설정

`src/main/resources/application.yaml` 파일에서 데이터베이스 설정을 확인하고 필요시 수정:

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/stock_example
    username: root
    password: your_password  # 본인의 MySQL 비밀번호로 변경
```

### 빌드 및 실행

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

## 🧪 테스트

각 Lock 메커니즘에 대한 동시성 테스트가 구현되어 있습니다.

### 전체 테스트 실행
```bash
./gradlew test
```

### 개별 테스트 실행
```bash
# Optimistic Lock 테스트
./gradlew test --tests OptimisticLockStockFacadeTest

# Pessimistic Lock 테스트  
./gradlew test --tests StockServiceTest

# Named Lock 테스트
./gradlew test --tests NamedLockStockFacadeTest

# Lettuce Lock 테스트
./gradlew test --tests LettuceLockStockFacadeTest

# Redisson Lock 테스트
./gradlew test --tests RedissonLockStockFacadeTest
```

### 테스트 시나리오

모든 테스트는 동일한 시나리오를 기반으로 합니다:
- 초기 재고: 100개
- 동시 요청 수: 100개
- 각 요청당 감소량: 1개
- **예상 결과**: 최종 재고 0개

## 📊 Lock 메커니즘 비교

| 방식 | 장점 | 단점 | 적합한 상황 |
|------|------|------|------------|
| **Optimistic Lock** | - DB 락 미사용으로 성능 우수<br>- 충돌 적을 때 효율적 | - 충돌 시 재시도 필요<br>- 충돌 빈번 시 성능 저하 | 읽기가 많고 쓰기가 적은 경우 |
| **Pessimistic Lock** | - 확실한 동시성 보장<br>- 구현 간단 | - DB 락으로 인한 성능 저하<br>- 데드락 가능성 | 충돌이 빈번한 경우 |
| **Named Lock** | - 트랜잭션 독립적<br>- 유연한 락 관리 | - 락 해제 누락 위험<br>- 커넥션 풀 고려 필요 | 분산 환경에서 복잡한 로직 |
| **Redis (Lettuce)** | - 분산 환경 지원<br>- 간단한 구현 | - Spin Lock으로 CPU 부하<br>- 네트워크 지연 | 간단한 분산 락 구현 |
| **Redis (Redisson)** | - 효율적인 락 대기<br>- 프로덕션 준비됨<br>- 다양한 기능 제공 | - 라이브러리 의존성<br>- 상대적으로 무거움 | 프로덕션 분산 환경 권장 |

## 🔍 주요 코드 설명

### Stock 엔티티
```java
@Entity
public class Stock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long productId;
    private Long quantity;
    
    @Version  // Optimistic Lock을 위한 버전 관리
    private Long version;
    
    public void decrease(Long quantity) {
        if (this.quantity - quantity < 0) {
            throw new RuntimeException("재고는 0개 미만이 될 수 없습니다.");
        }
        this.quantity -= quantity;
    }
}
```

### Redisson Lock 예제
```java
public void decrease(Long id, Long quantity) {
    RLock lock = redissonClient.getLock(id.toString());
    
    try {
        boolean available = lock.tryLock(10, 1, TimeUnit.MILLISECONDS);
        
        if(!available) {
            System.out.println("Lock 획득 실패");
            return;
        }
        
        stockService.decrease(id, quantity);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        lock.unlock();
    }
}
```

