package springbeam.susukgwan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springbeam.susukgwan.assignment.interceptor.AssignmentAuthInterceptor;
import springbeam.susukgwan.assignment.interceptor.AssignmentRoleInterceptor;
import springbeam.susukgwan.assignment.interceptor.SubmitTuteeInterceptor;
import springbeam.susukgwan.review.ReviewAuthInterceptor;
import springbeam.susukgwan.tag.TagAuthInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    TagAuthInterceptor tagAuthInterceptor;
    @Autowired
    ReviewAuthInterceptor reviewAuthInterceptor;
    @Autowired
    AssignmentAuthInterceptor assignmentAuthInterceptor;
    @Autowired
    SubmitTuteeInterceptor submitTuteeInterceptor;
    @Autowired
    AssignmentRoleInterceptor assignmentRoleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tagAuthInterceptor)
                .excludePathPatterns("/api/tag", "/api/tag/list") // 인터셉터가 가로채지 않는 url
                .addPathPatterns("/api/tag/**"); // 인터셉터가 가로채는 url (authorization 확인이 필요한 api)
        registry.addInterceptor(reviewAuthInterceptor)
                .excludePathPatterns("/api/review", "/api/review/list")
                .addPathPatterns("/api/review/**");
        registry.addInterceptor(assignmentAuthInterceptor)
                .excludePathPatterns("/api/assignment")
                .addPathPatterns("/api/assignment/*", "/api/assignment/*/check");
        registry.addInterceptor(submitTuteeInterceptor)
                .addPathPatterns("/api/assignment/*/submit", "/api/assignment/submit/*");
        registry.addInterceptor(assignmentRoleInterceptor)
                .addPathPatterns("/api/assignment/*/submit/list", "/api/assignment/submit/*/evaluate");
    }

}
