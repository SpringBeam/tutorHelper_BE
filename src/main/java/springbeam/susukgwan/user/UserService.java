package springbeam.susukgwan.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.user.dto.SignUpDTO;
import springbeam.susukgwan.user.dto.UpdateDTO;
import springbeam.susukgwan.user.vo.UserDetailVO;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> signUpUser(SignUpDTO signUpDTO) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRole() == Role.NONE) {
                // update role and name
                if (signUpDTO.getRole().equals("tutor")) {
                    user.setRole(Role.TUTOR);
                } else if (signUpDTO.getRole().equals("tutee")) {
                    user.setRole(Role.TUTEE);
                } else if (signUpDTO.getRole().equals("parent")) {
                    user.setRole(Role.PARENT);
                } else {
                    return ResponseEntity.badRequest().build();
                }
                user.setName(signUpDTO.getName());
                userRepository.save(user);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.internalServerError().build();
    }
    public ResponseEntity<?> getUserDetail() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok().body(
                    UserDetailVO.builder().role(user.getRole().getRole()).name(user.getName()).build()
            );
        }
        else return ResponseEntity.internalServerError().build();
    }
    public ResponseEntity<?> updateUser(UpdateDTO updateDTO) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(updateDTO.getName());
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.internalServerError().build();
    }
    public ResponseEntity<?> deleteUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.internalServerError().build();
    }
}
