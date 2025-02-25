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

| BE(L): 김형주                                                | BE: 김선희                                                   | BE: 공희진                                                   | BE: 신희연                                                   | BE: 임대일                                                   |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| <a href="https://github.com/kim0527"><img src="https://avatars.githubusercontent.com/u/143387515?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/kimseonhee126"><img src="https://avatars.githubusercontent.com/u/108293826?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/heejinkong"><img src="https://avatars.githubusercontent.com/u/113762366?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/RTPC01"><img src="https://avatars.githubusercontent.com/u/117623568?v=4" alt="profile" width="140" height="140"></a> | <a href="https://github.com/LimdaeIl"><img src="https://avatars.githubusercontent.com/u/131642334?v=4" alt="profile" width="140" height="140"></a> |
| <div align="center"><a href="https://github.com/kim0527" target="_blank"><img src="https://img.shields.io/badge/kim0527-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/kimseonhee126" target="_blank"><img src="https://img.shields.io/badge/kimseonhee126-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/heejinkong" target="_blank"><img src="https://img.shields.io/badge/heejinkong-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/RTPC01" target="_blank"><img src="https://img.shields.io/badge/RTPC01-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> | <div align="center"><a href="https://github.com/LimdaeIl" target="_blank"><img src="https://img.shields.io/badge/LimdaeIl-181717?style=for-the-social&logo=github&logoColor=white"/></a></div> |
| <div align="center">[![Tistory](https://img.shields.io/badge/Tistory-hj0527-orange?logo=tistory)](https://hj0527.tistory.com/)</div> | <div align="center">[![Tistory](https://img.shields.io/badge/Tistory-kseonhee126-orange?logo=tistory)](https://kseonhee126.tistory.com/)</div> | <div align="center">[![Velog](https://img.shields.io/badge/Velog-@heejinkong-20c997?logo=velog)](https://velog.io/@heejinkong/posts)</div> | <div align="center">[![Velog](https://img.shields.io/badge/Velog-@hyhy9501-20c997?logo=velog)](https://velog.io/@hyhy9501/posts)</div> | <div align="center">[![Tistory](https://img.shields.io/badge/Tistory-limdae94-orange?logo=tistory)](https://limdae94.tistory.com/)</div> |






<img src="https://github.com/user-attachments/assets/1297a0f9-9ce1-43f0-8fdf-b3b3e1da0c4f" alt="image-20250225154500974" width="500" style="zoom:30%;" />

<br>



| 👨‍👩‍👧‍👦구성원        | 💼 담당 업무                                                                                                |
| ----------------- |---------------------------------------------------------------------------------------------------------|
| **BE(L): 김형주** | 1️⃣ Product, Comment 도메인 담당<br />2️⃣ S3 연동 및 이미지 최적화<br />3️⃣ OpenAI 연동 <br />4️⃣ Github Actions CI/CD  |
| **BE: 김선희**    | 1️⃣ Payment, Review 도메인 담당<br />2️⃣ RabbitMQ 메시지 큐를 사용하여 성능 최적화                                         |
| **BE: 공희진**    | 1️⃣ User, Auth 도메인 담당<br />2️⃣ Spring Security+JWT 기반 인증/인가 설계 및 구현<br />3️⃣ Redis 기반 토큰 블랙리스트 및  캐싱 적용 |
| **BE: 신희연**    | 1️⃣ Order, OderDetail 도메인 담당<br />2️⃣ React, Next.js , BootStrap을 활용한 프론트엔드 개발                          |
| **BE: 임대일**    | 1️⃣ Category, Service 도메인 담당                                                                            |



<br>

## 📄 API docs



</div>
</details>

<details>
<summary><b>인증</b></summary>
- 이메일·닉네임 중복 검사부터 회원가입·로그인·로그아웃·토큰 재발급 등 인증 기능을 제공하는 API입니다.
<div markdown="1"><br/>
<img width="747" alt="스크린샷 2025-02-25 오후 5 51 22" src="https://github.com/user-attachments/assets/72b44b2c-fa67-487e-a04a-b56fbce87ef1" />
<img width="751" alt="스크린샷 2025-02-25 오후 5 51 41" src="https://github.com/user-attachments/assets/34d6abd5-9715-469e-8847-eae5902d698d" />

</div>
</details>

</div>
</details>

<details>
<summary><b>사용자</b></summary>
- 유저 정보 조회·수정·탈퇴 및 주소 관리를 통합적으로 제공하는 API입니다.
<div markdown="1"><br/>
<img width="752" alt="스크린샷 2025-02-25 오후 5 52 51" src="https://github.com/user-attachments/assets/3d66e89c-3e78-4154-9e1a-97cd989ed5ef" />
  
</div>
</details>


</div>
</details>

<details>
<summary><b>가게</b></summary>
- 새로운 가게 등록부터 정보 조회·수정·삭제, 카테고리·주소·활성화 상태 관리 등 매장 운영 전반을 담당하는 API입니다.
<div markdown="1"><br/>
<img width="757" alt="스크린샷 2025-02-25 오후 5 54 53" src="https://github.com/user-attachments/assets/80f5ca26-25c1-49b7-be8f-209ecb8e30a4" />
<img width="754" alt="스크린샷 2025-02-25 오후 5 55 17" src="https://github.com/user-attachments/assets/52b642fe-9cf7-4f09-adf8-5d30db3488d9" />


</div>
</details>


</div>
</details>

<details>
<summary><b>가게 카테고리</b></summary>
- 카테고리 생성·조회·수정·소프트 삭제 등 카테고리 관리를 제공하는 API입니다.
<div markdown="1"><br/>
<img width="751" alt="스크린샷 2025-02-25 오후 5 56 02" src="https://github.com/user-attachments/assets/f4fd500c-de8e-4663-8926-c7845dd36c41" />
<img width="745" alt="스크린샷 2025-02-25 오후 6 22 22" src="https://github.com/user-attachments/assets/caf24e0b-01cc-440c-a732-b7646902a96e" />

</div>
</details>

</div>
</details>

<details>
<summary><b>상품</b></summary>
- 상품 조회·추가·수정·삭제 등 상품 관리 기능을 제공하는 API입니다.
<div markdown="1"><br/>
<img width="750" alt="스크린샷 2025-02-25 오후 5 59 34" src="https://github.com/user-attachments/assets/d7e803dd-b50c-43db-8816-1c198712be32" />

</div>
</details>

</div>
</details>

<details>
<summary><b>상품 이미지</b></summary>
- 상품 이미지를 S3에 업로드·관리하는 API입니다.
<div markdown="1"><br/>
<img width="753" alt="스크린샷 2025-02-25 오후 6 04 57" src="https://github.com/user-attachments/assets/1b011858-e171-4266-be4a-6e7af6a1d69a" />

</div>
</details>

</div>
</details>

<details>
<summary><b>상품 설명</b></summary>
-AI를 통해 새로운 설명을 생성하고 저장하는 API입니다.
<div markdown="1"><br/>
<img width="750" alt="스크린샷 2025-02-25 오후 6 00 09" src="https://github.com/user-attachments/assets/00cbbde9-7696-40bf-8d9a-ad317e085b7e" />

</div>
</details>

</div>
</details>

<details>
<summary><b>주문</b></summary>
- 주문 생성·조회·취소·상태 변경 등 주문 처리 전반을 담당하는 API입니다.
<div markdown="1"><br/>
<img width="750" alt="스크린샷 2025-02-25 오후 6 01 43" src="https://github.com/user-attachments/assets/6d3e6e78-319b-4c1d-8c49-b604cad648c8" />
<img width="751" alt="스크린샷 2025-02-25 오후 6 02 00" src="https://github.com/user-attachments/assets/8cbaa123-6dfe-426c-bd23-5804237eac87" />
<img width="749" alt="스크린샷 2025-02-25 오후 6 02 16" src="https://github.com/user-attachments/assets/7abe29fd-c59e-4fdf-a622-a0cd6d46a05b" />


</div>
</details>

</div>
</details>

<details>
<summary><b>결제</b></summary>
- 결제 상태 조회·요청·취소·내역 관리를 담당하는 API입니다.
<div markdown="1"><br/>
<img width="749" alt="스크린샷 2025-02-25 오후 6 03 31" src="https://github.com/user-attachments/assets/49aefa35-ec71-4590-aecd-1117d71d58e5" />

</div>
</details>

</div>
</details>

<details>
<summary><b>리뷰</b></summary>
-  리뷰 작성·조회·수정·삭제 및 페이징·정렬 기능을 제공하는 API입니다.
<div markdown="1"><br/>
<img width="752" alt="스크린샷 2025-02-25 오후 6 04 21" src="https://github.com/user-attachments/assets/92ce5f4c-6efd-4a51-bf11-bb4ba20f9dc2" />

</div>
</details>



<br>

## 🚎 프로젝트 아키텍처

![image](https://github.com/user-attachments/assets/7f062246-333b-4f58-8609-c459aefe5aac)


<br>


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


<br>



## **🚀 Devopment Team Tool**

- `Slack`: 내일배움캠프 매니저님 및 멘토님으로부터 공지 사항을 전달받는 공간입니다.
- `zep`: 온라인 과정에 따라 실시간 화상 진행 및 스크럼을 진행하는 공간입니다.
- `Notion`: BE 일정 및 도출되는 모든 문서에 관해 형상 관리하는 공간입니다. 
- `Discord`: 해당 프로젝트 리포지토리에서 발생되는 모든 사항 알림 및 BE 팀원의 소통 창구입니다.
- `Github`: 열심히 구현한 코드를 관리하는 공간입니다.
- `Code with me`: 커밋 충돌 혹은 문제 발생하면 모두 함께 리뷰하는 도구입니다.
- `Erdcloud`: 데이터베이스 구성 요소를 시각화하기 위해 사용된 도구입니다.


<br>



## 📈 DataBase Schema

![image](https://github.com/user-attachments/assets/968bf20e-349b-4080-bee2-e77240cb43ce)


<br>

## 🍃 Contributors

**👜 Github Repository**

- [BEmin-Server](https://github.com/sperta-BEmin/BEmin-Server)
- [BEMin-Front](https://github.com/sperta-BEmin/BEMIN-Front)

**🫰Team Notion**

- [20팀 : BE민(BE달의 민족)](https://www.notion.so/20-BE-BE-198905993fa08023967dd734682db595)
