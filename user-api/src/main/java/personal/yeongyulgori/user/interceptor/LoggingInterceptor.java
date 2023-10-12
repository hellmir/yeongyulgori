package personal.yeongyulgori.user.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoggingInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static final String STOP_WATCH = "stopWatch";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        log.info("Received request for '{}'", request.getRequestURI());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        request.setAttribute(STOP_WATCH, stopWatch);

        return true;

    }

    @Override
    public void afterCompletion
            (HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {

        StopWatch stopWatch = (StopWatch) request.getAttribute(STOP_WATCH);
        stopWatch.stop();

        log.info("Completed request for '{}', status code: {}, estimated time: {}ms",
                request.getRequestURI(), response.getStatus(), stopWatch.getTotalTimeMillis());

    }

}
