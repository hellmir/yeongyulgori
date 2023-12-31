package personal.yeongyulgori.user.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import personal.yeongyulgori.user.model.form.SignInForm;
import personal.yeongyulgori.user.model.form.SignUpForm;
import personal.yeongyulgori.user.utility.MemoryUtil;

@Component
@Aspect
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* personal.yeongyulgori.user.service.*.*(..)) && args(signUpForm)")
    public Object logAroundForSignUpForm(ProceedingJoinPoint joinPoint, SignUpForm signUpForm) throws Throwable {

        log.info("Beginning to '{}.{}' task by email: '{}', username: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), signUpForm.getEmail(), signUpForm.getUsername());

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info("'{}.{}' task was executed successfully by 'email: {}', 'username: {}', estimated time: {} ms, used memory: {} bytes",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), signUpForm.getEmail(), signUpForm.getUsername(),
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;

    }

    @Around("execution(* personal.yeongyulgori.user.service.*.*(..)) && args(signInForm)")
    public Object logAroundForSignInForm(ProceedingJoinPoint joinPoint, SignInForm signInForm) throws Throwable {

        log.info("Beginning to '{}.{}' task by email or username: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), signInForm.getEmailOrUsername());

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info("'{}.{}' task was executed successfully by 'email or username: {}', estimated time: {} ms, used memory: {} bytes",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), signInForm.getEmailOrUsername(),
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;

    }

    @Around("execution(* personal.yeongyulgori.user.service.*.*(..)) && args(stringValue, ..)")
    public Object logAroundForStringValue(ProceedingJoinPoint joinPoint, String stringValue) throws Throwable {

        String parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames()[0];

        log.info("Beginning to '{}.{}' task by {}: '{}'",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, stringValue);

        long beforeMemory = MemoryUtil.usedMemory();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object process = joinPoint.proceed();

        stopWatch.stop();
        long memoryUsage = MemoryUtil.usedMemory() - beforeMemory;

        log.info("'{}.{}' task was executed successfully by '{}: {}', estimated time: {} ms, used memory: {} bytes",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), parameterName, stringValue,
                stopWatch.getTotalTimeMillis(), memoryUsage);

        return process;

    }

}
