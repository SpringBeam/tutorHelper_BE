package springbeam.susukgwan.tag;

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
public class TagAuthInterceptor implements HandlerInterceptor {
    @Autowired
    private final TagRepository tagRepository;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /* Tag Update & Delete Authorization */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);

        Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long tagId = Long.parseLong((String)pathVariables.get("tagId"));

        Long tutorIdOfTag = tagRepository.GetTutorIdOfTag(tagId);

        if (tutorIdOfTag != null && tutorId != tutorIdOfTag) {
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
