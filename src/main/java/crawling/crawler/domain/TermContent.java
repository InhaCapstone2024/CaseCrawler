package crawling.crawler.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class TermContent {
    private String termName; // 법령용어명

    private String source; // 출처

    private String description; // 법령용어정의

}
