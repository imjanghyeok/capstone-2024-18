package capstone.facefriend.auth.controller.interceptor;


import capstone.facefriend.auth.controller.support.AuthenticationContext;
import capstone.facefriend.auth.controller.support.AuthenticationExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final LoginInterceptor loginInterceptor;
    private final AuthenticationContext authenticationContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 액세스 토큰이 없을 경우를 위한 인터셉터
        log.info("[ LoginCheckInterceptor ]");
        if (AuthenticationExtractor.extractAccessToken(request).isEmpty()) {
            authenticationContext.setAnonymous();
            return true;
        }
        return loginInterceptor.preHandle(request, response, handler);
    }
}
