package sit.int221.nw1.Utils;
import org.springframework.util.StringUtils;

public class StringUtil {
    public static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}