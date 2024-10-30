package crawling.crawler;

import crawling.crawler.domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.time.LocalDate;

public class CrawlerApplication {

	private static final int BATCH_SIZE = 50; // 배치 사이즈

	public static void main(String[] args)  {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("crawler");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		// 판례 URL (XML)
		String caseUrl = "https://www.law.go.kr/DRF/lawSearch.do?OC=wkdtmf357&target=prec&type=XML&org=400202";

		// 용어 URL (XML)
		// dicKndCd: 010101 -> 법령 용어
		// dicKndCd: 010102 -> 일상 용어
		String termUrl = "https://www.law.go.kr/DRF/lawSearch.do?OC=wkdtmf357&target=lstrm&type=XML&dicKndCd=010101";

		// DB 저장 로직
		try {
			saveCaseInfo(caseUrl, em);
//			saveTermInfo(termUrl, em);

			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}

	// 판례 저장 메소드
	private static void saveCaseInfo(String url, EntityManager em) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		Document doc = dBuilder.parse(url);

		doc.getDocumentElement().normalize();

		NodeList count = doc.getElementsByTagName("PrecSearch"); // PrecSearch Tag로 분류

		int totalCnt = Integer.parseInt(getTagValue("totalCnt", (Element) count.item(0))); // 총 데이터 수
		int batchCnt = 0; // 배치 카운팅

		// 페이지 당 20건의 자료
//		for (int page = 1; page <= (totalCnt / 20); page++){
		for (int page = 1; page <= 10; page++){
			String pageUrl= url + "&page=" + page;
			doc = dBuilder.parse(pageUrl);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("prec"); // Prec Tag로 각 데이터 분류

			for(int temp = 0; temp <= nList.getLength(); temp++){
				Node nNode = nList.item(temp);

				if(nNode == null){
					continue;
				}

				if(nNode.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element) nNode;

					Long id = Long.parseLong(getTagValue("판례일련번호", eElement)); // 판례일련번호
					CaseContent caseContent = getCaseContent(id);
					if(caseContent == null){ // 상세 사항이 비어있는 경우 존재
						continue;
					}
					String caseName = getTagValue("사건명", eElement); // 사건명
					String caseNumber = getTagValue("사건번호", eElement); // 사건번호
					LocalDate date = LocalDate.parse(getTagValue("선고일자", eElement).replaceAll("\\.", "-")); // 선고일자
					String courtName = getTagValue("법원명", eElement); // 법원명
					String caseType = getTagValue("사건종류명", eElement); // 사건종류명
					String judgeType = getTagValue("판결유형", eElement); // 판결유형
					String caseUrl = getTagValue("판례상세링크", eElement); // 판례상세링크

					if(!caseType.equals("형사"))
						continue;


					// br 태그 전처리
					String decision = caseContent.getDecision().replaceAll("<br\\s*/?>", ""); // 판시사항
					String substance = caseContent.getSubstance().replaceAll("<br\\s*/?>", ""); // 판결요지
					String reference = caseContent.getReference().replaceAll("<br\\s*/?>", ""); // 참조조문
					String content = caseContent.getContent().replaceAll("<br\\s*/?>", ""); // 판례내용

					int startIndex = content.indexOf("주    문") + "주    문".length();
					int endIndex = content.indexOf("【이    유】");

					String judge = content.substring(startIndex, endIndex).trim();

					WinStatus winStatus;

					if (judge.matches(".*(유죄|구속|사형|징역\\s*\\d+|금고|벌금\\s*\\d+|과료|몰수|자격상실|자격정지).*")) {
						winStatus = WinStatus.PLAINTIFF;
					} else if (judge.matches(".*(무죄|무혐의|불기소|기소유예|각하).*")) {
						winStatus = WinStatus.DEFENDANT;
					} else if (judge.contains("파기") && judge.matches(".*(징역\\s*\\d+|벌금\\s*\\d+|몰수|자격상실|자격정지).*")) {
						winStatus = WinStatus.PLAINTIFF; // 파기 후 처벌이 있는 경우 원고 승소
					} else if (judge.contains("피고") && judge.contains("항소") && judge.contains("기각")) {
						winStatus = WinStatus.PLAINTIFF; // "피고인의 항소를 기각"은 원고 승소로 해석
					} else if (judge.contains("항소") && judge.contains("기각")) {
						winStatus = WinStatus.DEFENDANT; // "항소 기각"만 있는 경우 피고 승소로 해석
					} else {
						winStatus = WinStatus.AMBIGUOUS;
					}


					CaseInfo caseInfo = new CaseInfo(id, caseName, caseNumber, date, courtName,
							caseType, judgeType, caseUrl, decision,
							substance, reference, content, winStatus);

					em.persist(caseInfo); // 레코드 저장
					batchCnt++;

					// 배치 사이즈마다 flush
					if(batchCnt % BATCH_SIZE == 0){
						em.flush();
						em.clear();
					}
				}

				// 남아있는 자료 처리
				if(batchCnt > 0) {
					em.flush();
					em.clear();
				}

			}

		}
	}

	// 용어 저장 메소드
	private static void saveTermInfo(String url, EntityManager em) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		// 한글 데이터만 추출
		String[] alpha = {"ga", "na", "da", "ra", "ma", "ba", "sa", "ah", "ja", "cha", "ka", "ta", "pa", "ha"};

		for(int i = 0; i < 14; i++) {
			String parsed_url = url + ("&gana=" + alpha[i]);
			Document doc = dBuilder.parse(parsed_url);

			doc.getDocumentElement().normalize();

			NodeList count = doc.getElementsByTagName("LsTrmSearch");

			int totalCnt = Integer.parseInt(getTagValue("totalCnt", (Element) count.item(0))); // 총 페이지 수
			int batchCnt = 0; // 배치 카운트

			for (int page = 1; page <= 1; page++){
				String pageUrl = parsed_url + "&page=" + page;
				doc = dBuilder.parse(pageUrl);
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("lstrm");

				for (int temp = 0; temp <= nList.getLength(); temp++) {
					Node nNode = nList.item(temp);

					if (nNode == null) {
						continue;
					}

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;

						Long id = Long.parseLong(getLastNumber(getTagValue("법령용어ID", eElement))); // 법령용어ID
						TermContent tc = getTermContent(id);
						if (tc == null) { // 상세 사항 비어 있는 경우 존재
							continue;
						}
						String termName = tc.getTermName(); // 법령용어명
						String source = tc.getSource(); // 출처
						String description = tc.getDescription(); // 법령용어정의
						// 다른 링크로 또 연결되는 경우
						if(description.startsWith("<a"))
							continue;

						TermInfo termInfo = new TermInfo(id, termName, source, description);

						// 이미 존재하는 경우에 대한 처리
						if (em.find(TermInfo.class, id) != null) {
							em.merge(termInfo);
						} else
							em.persist(termInfo);

						batchCnt++;

						if (batchCnt % BATCH_SIZE == 0) {
							em.flush();
							em.clear();
						}
					}

					if (batchCnt > 0) {
						em.flush();
						em.clear();
					}

				}

			}
		}
	}

	// Tag 분류 메소드
	private static String getTagValue(String tag, Element eElement) {
		if(eElement.getElementsByTagName(tag).item(0) == null)
			return "";

		NodeList nList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
		Node nValue = nList.item(0);
		if (nValue == null) return "";
		return nValue.getNodeValue();
	}

	// 최신 자료 ID 추출 메소드
	private static String getLastNumber(String input) {
		if (input.contains(",")) {
			// 쉼표와 공백을 기준으로 분리한 후, 마지막 요소 반환
			String[] parts = input.split(",\\s*");
			return parts[parts.length - 1]; // 마지막 숫자 반환
		} else {
			// 쉼표가 없으면 입력 전체를 반환
			return input;
		}
	}

	// 판례 상세정보 추출 메소드
	private static CaseContent getCaseContent(Long id) {
			String url = "https://www.law.go.kr/DRF/lawService.do?OC=wkdtmf357&target=prec&type=XML"
				+ "&ID=" + id;

		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(url);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("PrecService");

			if (nList.getLength() == 0 || nList.item(0) == null) {
				return null;
			}

			Element eElement = (Element) nList.item(0);

            return new CaseContent(getTagValue("사건명", eElement),
                    getTagValue("판시사항", eElement),
                    getTagValue("판결요지", eElement),
                    getTagValue("참조조문", eElement),
                    getTagValue("판례내용", eElement));
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	// 용어 상세정보 추출 메소드
	private static TermContent getTermContent(Long id) {
		String url = "https://www.law.go.kr/DRF/lawService.do?OC=wkdtmf357&target=lstrm&type=XML"
				+ "&trmSeqs=" + id;

		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(url);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("LsTrmService");

			if (nList.getLength() == 0 || nList.item(0) == null) {
				return null;
			}

			Element eElement = (Element) nList.item(0);

            return new TermContent(getTagValue("법령용어명_한글", eElement),
                    getTagValue("출처", eElement),
                    getTagValue("법령용어정의", eElement));
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}

}
