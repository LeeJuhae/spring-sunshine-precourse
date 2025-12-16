package study;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component; // Configuration 대신 Component 사용

import java.time.LocalDate;

@Component // Bean으로 등록
public class Functions {
    @Tool(description = "Calculate a date after adding days from today")
    public String addDaysFromToday(int days) {
        var result = LocalDate.now().plusDays(days);
        return result.toString();
    }
}