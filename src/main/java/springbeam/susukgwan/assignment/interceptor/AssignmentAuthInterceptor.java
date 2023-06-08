package springbeam.susukgwan.assignment.interceptor;

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
import springbeam.susukgwan.assignment.Assignment;
import springbeam.susukgwan.assignment.AssignmentRepository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AssignmentAuthInterceptor implements HandlerInterceptor {
    @Autowired
    private final AssignmentRepository assignmentRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long assignmentId = Long.parseLong((String)pathVariables.get("assignmentId"));

        // (1) assignment 404 not found
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isEmpty()) {
            String result = objectMapper.writeValueAsString(new ResponseMsg(ResponseMsgList.NOT_EXIST_ASSIGNMENT.getMsg()));
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.setStatus(404); // Not Found
            response.getWriter().write(result);

            log.info("status code={}, body={}", response.getStatus(), ResponseMsgList.NOT_EXIST_ASSIGNMENT.getMsg());

            return false;
        }

        // (2) assignment 403 not authorized (tutor)
        Long tutorIdOfAssignment = assignmentRepository.GetTutorIdOfAssignment(assignmentId);
        if (tutorIdOfAssignment != null && userId != tutorIdOfAssignment) {
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
