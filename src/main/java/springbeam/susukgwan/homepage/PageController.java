package springbeam.susukgwan.homepage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String homepage() {
        return "index";
    }
    @GetMapping("/redirect")
    public String redirectionPage() {
        return "redirection";
    }
    @GetMapping("/privacy-policy")
    public String privacyPolicyPage() {
        return "privacy_policy";
    }
}
