# 20팀 : BE민(BE달의 민족)<a href="https://fe-project-tau.vercel.app/seoul-signiel"><img src="https://github.com/user-attachments/assets/1d13b544-c1c3-4eb6-b39f-6120c919f2e8" align="left" width="100"></a>

[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https://github.com/sperta-BEmin/BEmin-Server&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)
[![GitHub issues](https://img.shields.io/github/issues/Final-Project-Team6/BE_Project.svg)](https://github.com/sperta-BEmin/BEmin-Server/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-closed/Final-Project-Team6/BE_Project.svg)](https://github.com/sperta-BEmin/BEmin-Server/pulls)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-green.svg?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.0-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![AWS](https://img.shields.io/badge/AWS-Amazon_Web_Services-orange.svg?logo=amazon-aws)](https://aws.amazon.com/)



## ✨Ch.1 AI 활용 비즈니스 프로젝트 구현 배경

- 음식 주문 관리 플랫폼 개발 프로젝트는 Spring Boot 기반의 모놀리식 아키텍처를 채택했습니다.
- 프론트 개발도 함께 진행하면서 빠른 프로토타입 개발을 목표로 삼았습니다.
- 생성형 AI API를 연동하여 상품 설명 자동 추천 기능을 도입했습니다.
- 요구사항을 준수하며 시스템 확장성과 유지보수성을 고려한 설계를 진행했습니다.
- 기존 주문 관리 플랫폼과 달리 온라인 주문뿐 아니라 매장 내 직접 주문도 처리할 수 있도록 관리자 화면과 백엔드 시스템을 구축했습니다.

<br>

##  👨‍👩‍👧‍👦  구성원

| BE(L): 김형주                                                | BE: 김선희                                                   | BE: 공희진                                                   | BE: 신희연                                                | BE: 임대일                                                |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| <a href="https://github.com/kim0527"><img src="https://avatars.githubusercontent.com/u/143387515?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/kimseonhee126"><img src="https://avatars.githubusercontent.com/u/108293826?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/heejinkong"><img src="https://avatars.githubusercontent.com/u/113762366?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/RTPC01"><img src="https://avatars.githubusercontent.com/u/117623568?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/LimdaeIl"><img src="https://avatars.githubusercontent.com/u/131642334?v=4" alt="profile" width="140" height="140"></a> |
| <div align="center"><a href="https://github.com/kim0527" target="_blank"><img src="https://img.shields.io/badge/kim0527-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/kimseonhee126" target="_blank"><img src="https://img.shields.io/badge/kimseonhee126-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/heejinkong" target="_blank"><img src="https://img.shields.io/badge/heejinkong-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/RTPC01" target="_blank"><img src="https://img.shields.io/badge/RTPC01-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/LimdaeIl" target="_blank"><img src="https://img.shields.io/badge/LimdaeIl-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> |

<img src="https://github.com/user-attachments/assets/1297a0f9-9ce1-43f0-8fdf-b3b3e1da0c4f" alt="image-20250225154500974" width="1200" style="zoom:30%;" />

<br>



| 👨‍👩‍👧‍👦구성원        | 💼 담당 업무                                                  |
| ----------------- | ------------------------------------------------------------ |
| **BE(L): 김형주** | 1️⃣Product, Comment 도메인 담당<br />2️⃣S3 연동 및 이미지 최적화<br />3️⃣OpenAI 연동 CI/CD |
| **BE: 김선희**    | 1️⃣User, Auth API 설계 및 구현<br />2️⃣Spring Security+JWT 기반 인증/인가 설계 및 구현<br />3️⃣Redis 기반 토큰 블랙리스트 및  캐싱 적용 |
| **BE: 공희진**    | 1️⃣Payment, Review 도메인 담당<br />2️⃣RabbitMQ 메시지 큐를 사용하여 성능 최적화 |
| **BE: 신희연**    | 1️⃣Order, OderDetail 도메인 담당<br />2️⃣React, Next.js , BootStrap을 활용한 프론트엔드 개발 |
| **BE: 임대일**    | 1️⃣Category, Service 도메인 담당                               |



<br>




## 🚎 프로젝트 아키텍처

<img src="https://cdn.discordapp.com/attachments/1340603753719398410/1343841948699459686/image_2.png?ex=67bebdd7&is=67bd6c57&hm=b5b0ab8ad0c77d9cf916eac42fb519b88375f63e70a4588cd0bcd54ab6d4eecf&" alt="img" style="zoom:50%;" />



## 📈BE: Dependency And Devopment Team Tool

- Spring Boot: 3.4.2
- Java: 17
- Spring Boot Data JPA: 3.4.2
- Spring Boot Web: 3.4.2
- Lombok: (version managed)
- Spring Boot Devtools: 3.4.2
- PostgreSQL Driver: 42.6.0  (compatible with PostgreSQL 17)
- Spring Boot Test: 3.4.2
- Spring Boot Validation: 3.4.2
- JWT Security (JJWT API/Impl/Jackson: 0.11.5, Spring Boot Starter Security: 3.4.2)
- OpenAI GPT-3 Java Client: 0.11.1
- OpenAI GPT-3 Java API: 0.11.1
- Querydsl JPA: 5.0.0
- Spring Cloud AWS (S3): 3.0.0
- Scrimage Core: 4.1.3
- Scrimage WebP: 4.1.3
- Jackson Datatype Hibernate6: 2.18.2
- Spring Boot Data Redis: 3.4.2  (Lettuce: 6.2.6.RELEASE)
- Spring Boot Cache: 3.4.2
- Spring Boot AMQP: 3.4.2  (RabbitMQ Client: 5.17.0)
- Springdoc OpenAPI Webmvc UI: 2.5.0





## **🚀 Devopment Team Tool**

- `Slack`: 내일배움캠프 매니저님 및 멘토님으로부터 공지 사항을 전달받는 공간입니다.
- `zep`: 온라인 과정에 따라 실시간 화상 진행 및 스크럼을 진행하는 공간입니다.
- `Notion`: BE 일정 및 도출되는 모든 문서에 관해 형상 관리하는 공간입니다. 
- `Discord`: 해당 프로젝트 리포지토리에서 발생되는 모든 사항 알림 및 BE 팀원의 소통 창구입니다.
- `Github`: 열심히 구현한 코드를 관리하는 공간입니다.
- `Code with me`: 커밋 충돌 혹은 문제 발생하면 모두 함께 리뷰하는 도구입니다.
- `Erdcloud`: 데이터베이스 구성 요소를 시각화하기 위해 사용된 도구입니다.





## 📈 DataBase Schema

![image](https://github.com/user-attachments/assets/968bf20e-349b-4080-bee2-e77240cb43ce)



## 🍃 Contributors

**👜 Github Repository**

- [BEmin-Server](https://github.com/sperta-BEmin/BEmin-Server)
- [BEMin-Front](https://github.com/sperta-BEmin/BEMIN-Front)

**🫰Team Notion**

- [20팀 : BE민(BE달의 민족)](https://www.notion.so/20-BE-BE-198905993fa08023967dd734682db595)
