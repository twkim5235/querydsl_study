# QueryDsl



### QueryDsl 설정(의존성 및 각종 설정)

~~~groovy
group = 'study'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '1.8'
configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}
repositories {
    mavenCentral()
}
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
//querydsl 추가
implementation 'com.querydsl:querydsl-jpa'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: ‘org.junit.vintage’, module: ‘junit-vintage-engine'
    }
}
test {
    useJUnitPlatform()
}
//querydsl 추가 시작
def querydslDir = "$buildDir/generated/querydsl"
querydsl {
    jpa = true
           querydslSourcesDir = querydslDir
  }
  sourceSets {
      main.java.srcDir querydslDir
  }
  configurations {
      querydsl.extendsFrom compileClasspath
  }
  compileQuerydsl {
      options.annotationProcessorPath = configurations.querydsl
}
//querydsl 추가 끝

~~~

강의와 동일하게 환경설정을 해줬으나, Spring의 최신버전을 쓰다보니 해당 오류가 발생했다.

- `Unable to load class 'com.mysema.codegen.model.Type'.`

  ` This is an unexpected error. Please file a bug containing the idea.log file.`



**Spring 2.6이상에서는 Querydsl 5.0 버전을 쓰는대 Query 5.0 버전은 다르게 build.gradle 세팅을 해줘야 한다.**

~~~groovy
buildscript {
	ext {
		queryDslVersion = "5.0.0"
	}
}

plugins {
	id 'org.springframework.boot' version '2.6.4'
	id 'io.spring.dependency-management' version '1.0.11.RELEASE'
	id 'java'
	//querydsl 추가
	id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
}

group = 'study'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'

	//querydsl 추가
  //의존성을 받아오는 부분도 다르다.
	implementation "com.querydsl:querydsl-jpa:${queryDslVersion}"
	implementation "com.querydsl:querydsl-apt:${queryDslVersion}"
}

tasks.named('test') {
	useJUnitPlatform()
}

//querydsl 추가 시작
def querydslDir = "$buildDir/generated/querydsl"
querydsl {
	jpa = true
	querydslSourcesDir = querydslDir
}
sourceSets {
	main.java.srcDir querydslDir
}
compileQuerydsl {
	options.annotationProcessorPath = configurations.querydsl
}
//이부분이 다르다.
configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
	querydsl.extendsFrom compileClasspath
}
~~~



**compileQuerydsl을 실행하여 QEntity 파일을 생성해준다.**

![](./picture/querydsl_build.png)



#### QueryDsl의 검색 조건 - JPQL이 제공하는 모든 검색 조건을 다 제공함

~~~java
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'

member.username.isNotNull() //이름이 is not null

member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30

member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30

member.username.like("member%") //like 검색 member.username.contains("member") // like ‘%member%’ 검색 member.username.startsWith("member") //like ‘member%’ 검색
~~~



#### 결과 조회 메서드

- `fetch()`: 리스트 조회, 데이터 없으면 빈 리스트 반환
- `fetchOne()`: 단건 조회
  - 결과가 없으면: `null`
  - 결과가 둘 이상이면: `com.querydsl.core.NonUniqueResultExecption`
- `fetchFirst()`: `limit(1).fetchOne()`
- `fetchResults()`: 페이징 정보 포함, total count 쿼리 추가 실행
- `fetchCount()`: count 쿼리로 변경해서 count 수 조회



#### 정렬

- orderBy(): 정렬 메소드

  - 정렬하고 싶은 필드를 파라미터로 넘기면 된다

  - ex) `.orderBy(member.age.desc())`

    ​	  `.orderBy(member.age.asc())`

#### 페이징

- offset(): 데이터를 몇번부터 가져올지 설정한다.
- limit(): 데이터를 offset으로부터 몇개를 가져올지 정한다.



#### 집합

#####  집합 함수

- count(): 계수 함수
- sum(): 조회된 모든 단일 컬럼의 총합을 구하는 함수
- avg(): 조회된 모든  단일 컬럼의 평균을 구하는 함수
- max(): 조회된 모든 단일 컬럼 중 최상위값을 구하는 함수
- min(): 조회된 모든 단일 컬럼 중 최하위값을 구하는 함수
- groupBy(): 파라미터를 기준으로 그룹화
- having(): 그룹화후에 조건 처리



### 조인 - 기본 조인

#### 기본 조인

조인의 기본 문법은 첫번째 파라미터에 조인 대상을 지정하고, 두번째 파라미터에 별칭(alias)으로 사용할 Q타입을 지정하면 된다.

~~~java
join(조인 대상, 별칭으로 사용할 Q타입)
~~~



#### 세타 조인

연관관계가 없는 필드로 조인

~~~java
List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
~~~

- From 절에 여러 엔티티를 선택해서 세타 조인
- on을 사용하면 외부 조인 가능



### 조인 - on 절

- ON절을 활용한 조인(JPA 2.1 부터 지원)
  1. 조인 대상 필터링
  2. 연관관계가 없는 엔티티 외부 조인



1. 조인 대상 필터링

~~~java
List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
~~~



> 참고: ineer join을 할때 on으로 조건을 거나, where로 조건을 걸어도 똑같다. 내부 조인일때는 익숙한 where을 사용하고. on절은 외부조인이 필요한 경우에만 사용하자

```java
//동일한 결과가 출력됨

List<Tuple> result = queryFactory
        .select(member, team)
        .from(member)
        .join(member.team, team).on(team.name.eq("teamA"))
        .fetch();

List<Tuple> result = queryFactory
        .select(member, team)
        .from(member)
        .join(member.team, team)
  			.where(team.name.eq("teamA"))
        .fetch();
```



2. 연관관계가 없는 엔티티 외부 조인

```java
public void joinOnNoRelation() throws Exception{
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch();

    for (Tuple tuple : result) {
        System.out.println("tuple = " + tuple);
    }
}
```

- 하이버네이트 5.1 부터 `on` 을 사용해서 서로 관계가 없는 필드로 외부 조인하는 기능이 추가되었다. 물론 내부 조인도 가능하다.
- 문법을 잘 봐야 한다. **leftJoin()** 부분에 일반 조인과 다르게 엔티티 하나만 들어간다.
  - 일반 조인: `leftJoin(member.team, team)` 
  - on 조인: `leftJoin(team).on(조건)`
    - 일반 조인은 fk를 기준으로 조인을 하나, 연관관계가 없는 조인은 on절의 조건에 맞추어서 조인을 한다.



#### 조인 - 페치 조인

페치 조인은 SQL에서 제공하는 기능은 아니다. SQL 조인을 활용해서 연관된 엔티티를 SQL 한번에 조회하는 기능이다. 주로 성능 최적화에 사용하는 방법이다.



**일반 조인 절에 .fetchJoin()만 붙여주면 된다.**

~~~java
@Test
    public void fetchJoinUse() throws Exception{
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() 
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isTrue();
    }
~~~

























