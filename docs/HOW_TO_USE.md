## 사용 방법

### 1. AbstractAsyncRestItemReader

아래와 같이 AbstractAsyncRestItemReader 를 상속하여 convertResponse() 에서 response body 를 List 형태로 파싱한다.

```java
public class SampleRestItemReader extends AbstractAsyncRestItemReader<User> {
    @Override
    protected List<User> convertResponse(ResponseEntity<String> responseEntity, Map<String, ?> uriVariable) {
        
        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
            String body = responseEntity.getBody();
            // parse to List
        }
        
        return null;
    }
}
```

```xml
<bean id="sampleRestItemReader" class="com.naver.spring.batch.sample.job.rest_reader.SampleRestItemReader">
    <property name="apiUri" value="http://sample.com/api/items?arg={arg}&amp;page={page}"/>
    <property name="uriVariables">
        <list>
            <map key-type="java.lang.String" value-type="java.lang.Object">
                <entry key="arg" value="abc"/>
                <entry key="page" value="1"/>
            </map>
            <map key-type="java.lang.String" value-type="java.lang.Object">
                <entry key="arg" value="abc"/>
                <entry key="page" value="2"/>
            </map>
        </list>
    </property>
</bean>
```

## 2. SimpleBeanJdbcPagingItemReader

```java
public class User {
  int id;
  String username;
  int age;
}
```
```xml

<bean id="simpleBeanJdbcPagingItemReader" class="com.naver.spring.batch.extension.item.database.SimpleBeanJdbcPagingItemReader" scope="step">
    <constructor-arg value="User"/>
    <property name="dataSource" ref="dataSource" />
    <property name="sortKey" value="id" />
</bean>

```

## 3. UnmodifiedItemFilterProcessor

* 변경 여부 판단 방법
    * HashUnmodifiedItemChecker
        * 처리되는 모든 Item 에 대해 message digest 값을 구하여 hash repository 에 저장한다.
        * 이후 처리시 해당 Item 에 대해 hash(message digest) 값을 구하고, 이전에 저장된 값과 비교하여 다른 경우에만 Writer 로 보낸다.
        * message digest 는 [MessageDigest](https://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html) 가 지원하는 모든 알고리즘 사용 가능. 기본값 MD5.
        * 저장된 hash 값 유효기간 설정 가능.

![UnmodifiedItemFilterProcessor diagram](./unmodified_item_processor_diagram.png)

* HashUnmodifiedItemChecker 에서 JdbcHashRepository 를 사용하고자 할 경우에는 아래 스크립트를 통해 테이블을 생성한다.
  * h2 : hash-schema-h2.sql (naver-spring-batch-extension/src/main/resources/com.naver.spring.batch.extension)
  * mysql : hash-schema-mysql.sql (naver-spring-batch-extension/src/main/resources/com.naver.spring.batch.extension)
* JobParameter `'UnmodifiedItemFilter-skip'` 를 true 로 전달하면 모든 아이템은 필터링 되지 않는다.

* Spring batch Processor 체인에 UnmodifiedItemFilterProcessor 연결 방법

```xml
<bean id="processorChain" class="com.naver.spring.batch.extension.item.ListenerSupportCompositeItemProcessor">
    <property name="delegates">
        <list>
            <ref bean="actualProcessor"/>
            <ref bean="unmodifiedFilter"/>
        </list>
    </property>
</bean>

<bean id="unmodifiedFilter" class="com.naver.spring.batch.extension.item.filter.UnmodifiedItemFilterProcessor" scope="step">
    <property name="checker">
        <bean class="com.naver.spring.batch.extension.item.filter.HashUnmodifiedItemChecker">
            <property name="hashRepository" ref="jdbcHashRepository"/>
            <property name="keyPropertyNames">
                <list>
                    <value>id</value>
                </list>
            </property>
        </bean>
    </property>
</bean>
```

## 4. URL Exist Validation

아래와 같이 처리될 Item 객체의 url 프로퍼티에 `@UrlExists` annotation 을 통해 유효성 검증 설정

```java
public class Sample {
    private int id;
    
    @UrlExists
    private String homepageUrl;
}
```


* Spring batch Processor 체인에 ValidatingItemProcessor 연결 방법

![ValidatingItemFilterProcessor diagram](./validating_item_processor_diagram.png)

```xml
<bean id="processorChain" class="org.springframework.batch.item.support.CompositeItemProcessor">
    <property name="delegates">
        <list>
            <ref bean="validatingItemProcessor"/>
            <ref bean="actualProcessor"/>
        </list>
    </property>
</bean>
    
<bean id="validatingItemProcessor" class="org.springframework.batch.item.validator.ValidatingItemProcessor">
    <property name="validator">
        <bean class="org.springframework.batch.item.validator.SpringValidator">
            <property name="validator">
                <bean class="org.springframework.validation.beanvalidation.LocalValidatorFactoryBean"/>
            </property>
        </bean>
    </property>
</bean>
```