# Project Title

국가법령정보 공동활용 Open API를 이용한 판례 및 법률 용어 크롤러

## Getting Started
### Prerequisites
- 국가 법령 정보 공동활용 API 신청 및 승인 필요
- CrawlerApplication 내 URL의 OC 정보 수정 필요
### Example
```
String caseUrl = "https://www.law.go.kr/DRF/lawSearch.do?OC=<your_id>&target=prec&type=XML";
String termUrl = "https://www.law.go.kr/DRF/lawSearch.do?OC=<your_id>&target=lstrm&type=XML&dicKndCd=010101";
String url = "https://www.law.go.kr/DRF/lawService.do?OC=<your_id>&target=prec&type=XML" + "&ID=" + id;
String url = "https://www.law.go.kr/DRF/lawService.do?OC=<your_id>&target=lstrm&type=XML" + "&trmSeqs=" + id;
```

META-INF 디렉토리 및 하위 파일 persistence.xml 작성 필요
### Example
```
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2" xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <persistence-unit name="crawler">
        <properties>
            <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="jakarta.persistence.jdbc.user" value="sa"/>
            <property name="jakarta.persistence.jdbc.password" value=""/>
            <property name="jakarta.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
```

## Built With

* [Spring](https://spring.io/) - 웹 프레임워크
* [Spring JPA](https://spring.io/projects/spring-data-jpa) - ORM
* [Gradle](https://gradle.org/) - 의존성 관리 빌드 툴
