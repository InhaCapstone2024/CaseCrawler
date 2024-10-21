package crawling.crawler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@AllArgsConstructor @NoArgsConstructor
public class LegalTerm {

    @Id
    @Column(name = "TERM_ID")
    private Long id; // 법령용어 ID

    private String termName; // 법령용어명

    

}
