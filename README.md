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

##### 집합 함수

- count(): 계수 함수
- sum(): 조회된 모든 단일 컬럼의 총합을 구하는 함수
- avg(): 조회된 모든  단일 컬럼의 평균을 구하는 함수
- max(): 조회된 모든 단일 컬럼 중 최상위값을 구하는 함수
- min(): 조회된 모든 단일 컬럼 중 최하위값을 구하는 함수
- groupBy(): 파라미터를 기준으로 그룹화
- having(): 그룹화후에 조건 처리