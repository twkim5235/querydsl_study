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

