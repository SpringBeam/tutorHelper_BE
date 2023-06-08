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
import springbeam.susukgwan.assignment.Submit;
import springbeam.susukgwan.assignment.SubmitRepository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class SubmitTuteeInterceptor implements HandlerInterceptor {
    @Autowired
    private final AssignmentRepository assignmentRepository;
    @Autowired
    private final SubmitRepository submitRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String assignmentIdStr = (String)pathVariables.get("assignmentId");
        String submitIdStr = (String)pathVariables.get("submitId");

        if (assignmentIdStr != null) {
            Long assignmentId = Long.parseLong(assignmentIdStr);

            // (1-1) assignment 404 not found
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

            // (1-2) assignment 403 not authorized (tutee)
            Long tuteeIdOfAssignment = assignmentRepository.GetTuteeIdOfAssignment(assignmentId);
            if ((tuteeIdOfAssignment != null && userId != tuteeIdOfAssignment) || tuteeIdOfAssignment == null) {
                String result = objectMapper.writeValueAsString(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.setStatus(403); // Forbidden
                response.getWriter().write(result);

                log.info("status code={}, body={}", response.getStatus(), ResponseMsgList.NOT_AUTHORIZED.getMsg());

                return false;
            }
        } else if (submitIdStr != null) {
            Long submitId = Long.parseLong(submitIdStr);

            // (2-1) submit 404 not found
            Optional<Submit> submit = submitRepository.findById(submitId);
            if (submit.isEmpty()) {
                String result = objectMapper.writeValueAsString(new ResponseMsg(ResponseMsgList.NOT_EXIST_SUBMIT.getMsg()));
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.setStatus(404); // Not Found
                response.getWriter().write(result);

                log.info("status code={}, body={}", response.getStatus(), ResponseMsgList.NOT_EXIST_SUBMIT.getMsg());

                return false;
            }

            // (2-2) submit 403 not authorized (tutee)
            Long tuteeIdOfSubmit = submitRepository.GetTuteeIdOfSubmit(submitId);
            if ((tuteeIdOfSubmit != null && userId != tuteeIdOfSubmit) || tuteeIdOfSubmit == null) {
                String result = objectMapper.writeValueAsString(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.setStatus(403); // Forbidden
                response.getWriter().write(result);

                log.info("status code={}, body={}", response.getStatus(), ResponseMsgList.NOT_AUTHORIZED.getMsg());

                return false;
            }
        }

        return true;
    }
}
