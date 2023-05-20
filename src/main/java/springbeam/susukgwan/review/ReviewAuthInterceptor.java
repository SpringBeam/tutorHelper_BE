package springbeam.susukgwan.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReviewAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private final ReviewRepository reviewRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /* Review Update & Delete & Check Authorization */
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long reviewId = Long.parseLong((String)pathVariables.get("reviewId"));

        Long tutorIdOfReview = reviewRepository.GetTutorIdOfReview(reviewId);

        if (tutorIdOfReview != null && userId != tutorIdOfReview) {
            String result = objectMapper.writeValueAsString(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.setStatus(403); // Forbidden
            response.getWriter().write(result);

            log.info("status code={}, body={}", response.getStatus(), ResponseMsgList.NOT_AUTHORIZED.getMsg());

            return false;
        }

        return true;
    }
}
