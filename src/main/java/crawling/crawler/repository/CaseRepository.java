package crawling.crawler.repository;

import crawling.crawler.domain.CaseContent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CaseRepository {
    public static void main(String[] args) throws Exception {
        String url = "https://www.law.go.kr/DRF/lawSearch.do?OC=wkdtmf357&target=prec&type=XML";
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(url);

            doc.getDocumentElement().normalize();

            NodeList count = doc.getElementsByTagName("PrecSearch");

            int totalCnt = Integer.parseInt(getTagValue("totalCnt", (Element) count.item(0)));

//            for (int page = 1; page <= (totalCnt/20) ; page++){
              for (int page = 1; page <= 5; page++) {
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

                        String caseNumber = getTagValue("판례일련번호", eElement);
                        CaseContent caseContent = getCaseContent(Long.parseLong(caseNumber));
                        String decision = caseContent.getDecision().replaceAll("<br\\s*/?>", "");
                        String substance = caseContent.getSubstance().replaceAll("<br\\s*/?>", "");
                        String reference = caseContent.getReference().replaceAll("<br\\s*/?>", "");
                        String content = caseContent.getDecision().replaceAll("<br\\s*/?>", "");

                        System.out.println("------------------------------");
                        System.out.println("판례일련번호 : " + caseNumber);
                        System.out.println("사건명      : " + getTagValue("사건명", eElement));
                        System.out.println("사건번호    : " + getTagValue("사건번호", eElement));
                        System.out.println("법원명      : " + getTagValue("법원명", eElement));
                        System.out.println("사건종류명   : " + getTagValue("사건종류명", eElement));
                        System.out.println("판결유형    : " + getTagValue("판결유형", eElement));
                        System.out.println("판례상세링크 : " + getTagValue("판례상세링크", eElement));
                        System.out.println("판시사항    : " + decision);
                        System.out.println("판결요지    : " + substance);
                        System.out.println("참조조문    : " + reference);
                        System.out.println("판례내용    : " + content);
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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


