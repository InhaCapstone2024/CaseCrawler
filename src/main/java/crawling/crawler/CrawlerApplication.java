package crawling.crawler;

import crawling.crawler.domain.CaseContent;
import crawling.crawler.domain.CaseInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.time.LocalDate;

@SpringBootApplication
public class CrawlerApplication {

	public static void main(String[] args) throws Exception {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("crawler");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String url = "https://www.law.go.kr/DRF/lawSearch.do?OC=wkdtmf357&target=prec&type=XML";
		try {


			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(url);

			doc.getDocumentElement().normalize();

			NodeList count = doc.getElementsByTagName("PrecSearch");

			int totalCnt = Integer.parseInt(getTagValue("totalCnt", (Element) count.item(0)));

//			for (int page = 1; page <= (totalCnt / 20) ; page++){
			for (int page = 1; page <= 5; page++){
				String pageUrl= url + "&page=" + page;
				doc = dBuilder.parse(pageUrl);
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("prec");

				for(int temp = 0; temp <= nList.getLength(); temp++){
					Node nNode = nList.item(temp);

					if(nNode == null){
						continue;
					}



					if(nNode.getNodeType() == Node.ELEMENT_NODE){
						Element eElement = (Element) nNode;

						Long id = Long.parseLong(getTagValue("판례일련번호", eElement));
						CaseContent caseContent = getCaseContent(id);
						String caseName = getTagValue("사건명", eElement);
						String caseNumber = getTagValue("사건번호", eElement);
						LocalDate date = LocalDate.parse(getTagValue("선고일자", eElement).replaceAll("\\.", "-"));
						String courtName = getTagValue("법원명", eElement);
						String caseType = getTagValue("사건종류명", eElement);
						String judgeType = getTagValue("판결유형", eElement);
						String caseUrl = getTagValue("판례상세링크", eElement);

						String decision = caseContent.getDecision().replaceAll("<br\\s*/?>", "");
						String substance = caseContent.getSubstance().replaceAll("<br\\s*/?>", "");
						String reference = caseContent.getReference().replaceAll("<br\\s*/?>", "");
						String content = caseContent.getContent().replaceAll("<br\\s*/?>", "");

						CaseInfo caseInfo = new CaseInfo(id, caseName, caseNumber, date, courtName,
								caseType, judgeType, caseUrl, decision,
								substance, reference, content);

						em.persist(caseInfo);
					}

				}

			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}

	private static String getTagValue(String tag, Element eElement) {
		NodeList nList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
		Node nValue = (Node) nList.item(0);
		if (nValue == null) return "";
		return nValue.getNodeValue();
	}

	private static CaseContent getCaseContent(Long id) throws Exception {
		String url = "https://www.law.go.kr/DRF/lawService.do?OC=wkdtmf357&target=prec&type=XML"
				+ "&ID=" + Long.toString(id);

		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(url);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("PrecService");

			if (nList.getLength() == 0 || nList.item(0) == null) {
				throw new RuntimeException("No data found for the given id: " + id);
			}

			Element eElement = (Element) nList.item(0);
			CaseContent cc = new CaseContent(getTagValue("사건명", eElement),
					getTagValue("판시사항", eElement),
					getTagValue("판결요지", eElement),
					getTagValue("참조조문", eElement),
					getTagValue("판례내용", eElement));

			return cc;
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}

}
