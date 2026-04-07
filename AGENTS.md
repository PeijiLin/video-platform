# AGENTS.md - Development Guidelines

## Project Overview
Spring Cloud microservices video platform with: user-service, video-service, interaction-service, recommendation-service, data-processing-service, email-service, framework-service, api-gateway, common.

## Tech Stack
Java 17, Spring Boot 3.3.4, Spring Cloud 2023.0.3, Spring Cloud Alibaba 2023.0.3.2, Maven, MyBatis Plus 3.5.5, Nacos, OpenFeign, Sentinel, Redis/Redisson, Elasticsearch, ClickHouse, JWT, Lombok, Hutool.

---

## Build, Test, Lint Commands

```bash
# Build
mvn clean install                          # Full build
mvn clean install -pl user-service -am     # Build module + deps
mvn clean install -DskipTests              # Skip tests

# Run
cd user-service && mvn spring-boot:run
java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar

# Test
mvn test                                   # All tests
mvn test -Dtest=Test                       # Specific class
mvn test -Dtest=Test#test01                # Specific method
mvn test -pl video-service                 # Module tests

# Code quality
mvn compile                                # Compile only
mvn dependency:tree                        # Analyze deps
```

---

## Code Style Guidelines

### Naming Conventions
| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `UserController`, `VideoServiceImpl` |
| Methods | camelCase | `userRegister`, `batchSelect` |
| Variables | camelCase | `userService`, `videoIds` |
| Constants | UPPER_SNAKE_CASE | `VIDEO_CATEGORY`, `USER_LIKED` |
| DTOs | Suffix Request/VO/DTO | `UserLoginRequest`, `UserVO` |

### Import Order
1. Java/Jakarta (`java.util`, `jakarta.annotation`)
2. Spring (`org.springframework`)
3. Third-party (Lombok, MyBatis Plus, Hutool)
4. Internal (`com.lpjpro`)

```java
package com.lpjpro.controller;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import cn.hutool.core.bean.BeanUtil;
import com.lpjpro.service.UserService;
```

### Class Structure
Order: annotations → fields → @Resource → public methods → private methods

### Annotations
```java
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(...) { }
}

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> 
    implements VideoService {
    @Resource
    private CategoryService categoryService;
}
```

### DTO/VO Patterns
- `*Request` - Input parameters
- `*VO` - API response objects
- Always wrap returns in `BaseResponse<T>`

### Error Handling
```java
// Use ThrowsUtils for validation
ThrowsUtils.throwIf(condition, ErrorCode.PARAMS_ERROR);
ThrowsUtils.throwIf(condition, ErrorCode.PARAMS_ERROR, "Custom message");
throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Not found");
```
Error codes: `PARAMS_ERROR`, `NOT_FOUND_ERROR`, `SYSTEM_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`

### Database (MyBatis Plus)
```java
LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Category::getName, categoryName);
Category category = categoryService.getOne(wrapper);
this.save(video);
this.updateById(video);
this.removeById(id);
```

### Logging
```java
@Slf4j
public class UserController {
    log.info("User login attempt: {}", username);
    log.error("Failed", exception);
}
```

### Transactions
```java
@Transactional
public Long uploadVideo(VideoUploadRequest request) { }
```

### Inter-Service Calls (OpenFeign)
```java
@FeignClient(name = "user-service", fallback = XxxFallback.class)
public interface UserServiceFeignClient {
    @GetMapping("/user/getList")
    BaseResponse<List<UserVO>> getUserByIds(@RequestParam("ids") List<Long> ids);
}
```

---

## Testing

```java
import org.junit.jupiter.api.Test;

@SpringBootTest
public class VideoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class, args);
    }
    
    @Test
    void test01() { }
}
```

---

## Common Patterns
1. Validate inputs with `ThrowsUtils.throwIf()`
2. Use DTOs, not entities directly in API
3. Use Service interfaces with implementations
4. Log important operations
5. Use transactions for multi-step DB ops
6. Handle exceptions via `BusinessException`
7. Cache frequently accessed data in Redis
8. Use constants for magic strings/numbers