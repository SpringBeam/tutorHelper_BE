package springbeam.susukgwan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springbeam.susukgwan.tag.TagAuthInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    TagAuthInterceptor tagAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tagAuthInterceptor)
                .excludePathPatterns("/api/tag", "/api/tag/list") // 인터셉터가 가로채지 않는 url
                .addPathPatterns("/api/tag/**"); // 인터셉터가 가로채는 url (authorization 확인이 필요한 api)
    }

}