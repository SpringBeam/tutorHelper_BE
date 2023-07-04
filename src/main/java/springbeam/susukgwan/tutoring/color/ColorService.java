package springbeam.susukgwan.tutoring.color;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.util.Optional;

@Service
public class ColorService {
    @Autowired
    private ColorRepository colorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TutoringRepository tutoringRepository;

    public ResponseEntity<?> setColor(ColorSetDTO colorSetDTO) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        User requestUser = userOptional.get();
        Tutoring tutoring;
        if (requestUser.getRole() == Role.TUTOR) {
            Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(colorSetDTO.getTutoringId(), userId);
            if (tutoringOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
            }
            else tutoring = tutoringOptional.get();
        }
        else if (requestUser.getRole() == Role.TUTEE) {
            Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTuteeId(colorSetDTO.getTutoringId(), userId);
            if (tutoringOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
            }
            else tutoring = tutoringOptional.get();
        }
        else return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (colorSetDTO.getColorId() < 1 || colorSetDTO.getColorId() > 10) {
            return ResponseEntity.badRequest().build();
        }
        ColorList colorPicked = ColorList.values()[colorSetDTO.getColorId().intValue()];
        Optional<Color> colorOptional = colorRepository.findByTutoring(tutoring);
        if (colorOptional.isEmpty()) {
            Color newColor = Color.builder().tutoring(tutoring).build();
            // save new Color object
            if (requestUser.getRole() == Role.TUTOR) newColor.setTutorColor(colorPicked);
            else newColor.setTuteeColor(colorPicked);
            colorRepository.save(newColor);
        }
        else {
            Color oldColor = colorOptional.get();
            if (requestUser.getRole() == Role.TUTOR) oldColor.setTutorColor(colorPicked);
            else oldColor.setTuteeColor(colorPicked);
            colorRepository.save(oldColor);
        }
        return ResponseEntity.ok().build();
    }

}
