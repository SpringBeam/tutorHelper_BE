package springbeam.susukgwan.auth.info;

import java.util.Map;

public class KakaoOAuth2UserInfo {
    private Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes=attributes;
    }
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getId() {
        return attributes.get("id").toString();
    }
    public String getName() {
        Map<String, Object> properties = ((Map<String, Object>)attributes.get("properties"));
        if (properties == null) return null;
        return properties.get("nickname").toString();
    }
}
