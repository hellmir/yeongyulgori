package personal.yeongyulgori.post.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Aspect
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* personal.yeongyulgori.post.domain.post.service.*.*(..)) && args(longValue, ..)")
    public Object logAroundForPostService(ProceedingJoinPoint joinPoint, Long longValue) throws Throwable {

        String parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames()[0];

        log.info("Beginning to '{}.{}' task by {}: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, longValue);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();

        log.info("'{}.{}' task was executed successfully in '{} ms' by {}: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), stopWatch.getTotalTimeMillis(),
                parameterName, longValue);

        return process;

    }

    @Around("execution(* personal.yeongyulgori.post.domain.post.service.*.*(..)) && args(stringValue, ..)")
    public Object logAroundForPostService(ProceedingJoinPoint joinPoint, String stringValue) throws Throwable {

        String parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames()[0];

        log.info("Beginning to '{}.{}' task by {}: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, stringValue);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();

        log.info("'{}.{}' task was executed successfully in '{} ms' by {}: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), stopWatch.getTotalTimeMillis(),
                parameterName, stringValue);

        return process;

    }

    @Around("execution(* personal.yeongyulgori.post.domain.image.service.*.*(..)) && args(longValue, ..)")
    public Object logAroundForImageService(ProceedingJoinPoint joinPoint, Long longValue) throws Throwable {

        String parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames()[0];

        log.info("Beginning to '{}.{}' task by {}: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, longValue);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();

        log.info("'{}.{}' task was executed successfully in '{} ms' by {}: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), stopWatch.getTotalTimeMillis(),
                parameterName, longValue);

        return process;

    }

}
