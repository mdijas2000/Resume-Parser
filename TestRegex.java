import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {
    public static void main(String[] args) {
        String duration = "March 2023 – July 2025";
        Matcher matcher = Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(duration);
        int count = 0;
        while (matcher.find()) {
            count++;
            System.out.println("Found: " + matcher.group());
        }
        System.out.println("Count: " + count);
    }
}
