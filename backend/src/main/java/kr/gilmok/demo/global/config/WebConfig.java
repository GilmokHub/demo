package kr.gilmok.demo.global.config;

import kr.gilmok.demo.global.interceptor.AdmissionTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdmissionTokenInterceptor admissionTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(admissionTokenInterceptor)
                .addPathPatterns("/reservations", "/reservations/*/confirm");
    }
}
