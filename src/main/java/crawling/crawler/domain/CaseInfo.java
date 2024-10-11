package crawling.crawler.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity @Getter
@AllArgsConstructor @NoArgsConstructor
public class CaseInfo {

    @Id
    private String id; //판례일련번호

    private String caseName; //사건명

    private String caseNumber; //사건번호

    private LocalDateTime date; // 날짜

    private String courtName; // 법원명

    private String caseType; // 사건종류명

    private String judgeType; // 판결유형

    private String url; // 판례상세링크

    private String decision; // 판시사항

    private String substance; // 판결요지

    private String reference; //참조조문

    private String content; //판례내용

}
