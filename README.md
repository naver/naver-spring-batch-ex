# Naver Spring Batch Extension
> Naver Spring Batch Extension 은 Chunk-Oriented Processing 으로 구성된 spring-batch job 에서 사용 가능한 몇가지 추가적인 모듈을 제공한다.

## dependency
 아래와 같이 spring-batch application 의 pom.xml 또는 build.gradle 에 dependency 를 추가하여 사용한다.

maven
```xml
<dependency>
    <groupId>com.naver.spring.batch</groupId>
    <artifactId>naver-spring-batch-extension</artifactId>
    <version>${version}</version>
</dependency>
```
gradle
```groovy
compile('com.naver.spring.batch:naver-spring-batch-extension:${version}')
```

## 제공 모듈

### AbstractAsyncRestItemReader
 페이징 처리된 Rest API 로부터 데이터를 읽어 들이기 위한 Item Reader

### SimpleBeanJdbcPagingItemReader
 SQL 을 작성할 필요없이 Item 객체의 구조를 통해 paging query 를 자동으로 생성하고 JDBC 를 통해 database record 를 읽어 들이기 위한 Item Reader

### UnmodifiedItemFilterProcessor
 Processor Chain 에서 처리되는 Item 에 대해 변경 여부를 확인하고 이전에 처리된 Item 에서 변경되지 않았다면 Iter Writer 로 넘어가지 않도록 필터링 한다.

### URL Exist Validation (@UrlExists)
 ValidatingItemProcessor 를 통해 Processor Chain 에서 처리되는 Item 객체 내부의 url 의 접근 여부에 대한 유효성 검증을 제공한다.

## 사용 방법

 [HOW_TO_USE](docs/HOW_TO_USE.md)
 
## LICENSE

```
Copyright 2018 NAVER Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
