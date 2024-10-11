package crawling.crawler.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CaseContent {

    // 사건명
    private String name;

    //판시사항
    private String decision;

    //판결요지
    private String substance;

    //참조조문
    private String reference;

    //판례내용
    private String content;
}
