package springbeam.susukgwan.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    // user를 id로 찾도록 설정 UserPrincipal과 관련
    // AuthTokenProvider의 getAuthentication에서 id 값이 String username으로 인증 객체에 담기고
    // JwtAuthenticationFilter에서 그 인증 객체가 SecurityContext의 보안 인증으로 등록된다.
    // DaoAuthenticationProvider가 요청 유저의 username, password를 검증하는 역할을 하는데,
    // 이 DaoAuthenticationProvider가 인증 유저 확인에 UserDetailsService의 loadUserByUsername을 이용한다.
    // 그래서 이를 implement하여 저장된 User객체가 있으면 UserPrincipal(UserDetails)를 반환해주도록 한다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findById(Long.valueOf(username));
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Cannot find username.");
        }
        return UserPrincipal.create(userOptional.get());
    }
}
