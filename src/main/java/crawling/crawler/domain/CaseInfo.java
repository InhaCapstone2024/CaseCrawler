package crawling.crawler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity @Getter
@AllArgsConstructor @NoArgsConstructor
public class CaseInfo {

    @Id
    @Column(name = "CASE_ID")
    private Long id; //판례일련번호

    private String caseName; //사건명

    private String caseNumber; //사건번호

    private LocalDate date; // 날짜

    private String courtName; // 법원명

    private String caseType; // 사건종류명

    private String judgeType; // 판결유형

    private String caseUrl; // 판례상세링크

    @Lob
    private String decision; // 판시사항

    @Lob
    private String substance; // 판결요지

    @Lob
    private String reference; //참조조문

    @Lob
    private String content; //판례내용

}
