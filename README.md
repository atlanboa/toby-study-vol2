## 1장 IOC 컨테이너 빈 설정정보

```java
package com.toby.vol2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DependencyTest {

    @Test
    public void registerBeanWithDependency() {
        // 코드로 설정 메타정보를 등록하는 기능을 제공하는 애플리케이션 컨텍스트
        StaticApplicationContext ac = new StaticApplicationContext();

        // 가장 기본적인 BeanDefinition 인터페이스의 구현체인 RootBeanDefinition 을 사용하여 StringPrinter 빈 설정 정보를 만들고
        // 이를 printer 라는 이름으로 빈을 등록해줍니다.
        ac.registerBeanDefinition("printer", new RootBeanDefinition(StringPrinter.class));


        // Hello 클래스도 빈 설정 정보를 만들어서 각 속성 값에 대한 정보를 추가해줍니다.
        // printer 속성의 경우 Runtime 에 참조하게 됨으로 RuntimeReference 에 "printer" 라는 빈을 참조하도록 설정합니다.
        BeanDefinition helloDef = new RootBeanDefinition(Hello.class);
        helloDef.getPropertyValues().addPropertyValue("name", "Spring");
        helloDef.getPropertyValues().addPropertyValue("printer", new RuntimeBeanReference("printer"));

        // 빈 설정을 등록합니다.
        ac.registerBeanDefinition("hello", helloDef);


        // 빈을 가져와서 실행해봅니다.
        Hello hello = ac.getBean("hello", Hello.class);
        hello.print();

        // print 메소드를 호출하면 Hello 가 참조하는 StringPrinter 가 호출됩니다.
        // 따라서 StringPrinter 의 buffer 에 Hello Spring 이라는 값이 append 되겠죠.
        assertEquals("Hello Spring", ac.getBean("printer").toString());
    }
}

```

### IoC 컨테이너 계층 구조
IoC 컨테이너 계층 구조는 트리형태이다. <br>
DI를 위해서 특정 빈을 찾기 위해서는 먼저 자기 자신이 가지고 있는지 확인하고, 그 다음 직계 부모로부터 타고 올라가서 확인한다.<br>
따라서 자식이나, 형제가 가지고 있는 빈에 대해서는 알지 못한다.

### 웹 어플리케이션
스프링은 모든 요청을 몇 개의 서블릿이 중앙집중식으로 모여 처리한다. 이를 프론트 컨트롤러 패턴이라 한다.<br>
웹 애플리케이션 안에서 동작하는 IoC 컨테이너는 보통 두 가지 방법으로 만들어진다.
1. 요청을 처리하는 서블릿 안에서 생성
2. 웹 애플리케이션 레벨에서 생성

일반적으로 이 두 가지 모두 사용한다.

웹 애플리케이션 레벨에 등록되는 컨테이너는 보통 루트 웹 애플리케이션이라 부름
따라서 이 컨텍스트는 서블릿 레벨에 등록되는 컨테이너들의 *부모 컨테이너*가 된다.

웹 어플리케이션에는 하나 이상의 애플리케이션에서 프론트 컨트롤러 역할을 하는 서블릿이 등록될 수 있음
이 서블릿에는 각각 독립적으로 애플리케이션 컨텍스트가 만들어짐

공유되는 빈은 웹 애플리케이션 레벨의 컨텍스트에 등록해서 사용할 수 있음

일반적으로 프론트 컨트롤러의 역할을 담당하는 *서블릿* 은 하나만 등록해서 사용

## 컨텍스트를 계층 구조로 만드는 이유
스프링이 제공하는 웹 기술을 사용하지 않는 경우가 있기에, 계층으로 분리해서 사용
확장성을 위함

따라서 XML 설정 파일은 성격에 따라 분리해서 사용하는 것을 권장

### 루트 애플리케이션 컨텍스트 등록
서블릿의 이벤트 리스너를 활용

스프링의 시작과 종료 시 발생하는 이벤트를 처리하는 리스너 : **ServletContextListener**
이를 구현하여 웹 어플리케이션 전체에 적용 가능한 DB 연결 기능이나 로깅 같은 서비스를 만드는 데 유용

스프링은 루트 애플리케이션 초기화, 종료하는 기능을 제공하는 **ContextLoaderListener** 를 제공

사용하기 위해서는 단순히 리스너 선언을 넣어주면 된다.

리스너 선언에서 별다른 설정을 해주지 않으면?
- 애플리케이션 컨텍스트 클래스 : XmlWebApplicationContext
- XML 설정 파일 위치 : /WEB-INF/applicationContext.xml

### DispatcherServlet
스프링의 웹 기능을 지원하는 프론트 컨트롤러 서블릿
고유한 컨텍스트를 생성하고 초기화함
부모 컨텍스트로 웹 애플리케이션 레벨에 등록된 루트 애플리케이션 컨텍스트를 찾아 자신의 부모 컨텍스트로 사용

#### DispatcherServlet 사용시 주의점
DispatcherServlet 으로 만들어지는 애플리케이션 컨텍스트는 모두 독립적인 네임 스페이스를 가진다.
네임 스페이스는 <servlet-name> 으로 지정한 서블릿 이름에 -servlet 을 붙여서 만든다.

중요한 이유는 DispatcherSevlet 이 사용할 디폴트 XML 설정 파일의 위치를 네임스페이스를 이용해서 만든다.

만약 servlet-name 에 spring 으로 지정했으면 디폴트 설정 파일은 /WEB-INF/spring-servlet.xml 이 된다.

# 스프링에서 빈 정의 방법
1. XML: <bean> 태그 사용

2. XML: 네임스페이스와 전용 태그 (eg. <aop:pointcut> )

3. 스테레오타입 애노테이션과 빈 스캐너 (일일이 선언하기 귀찮을때, @Component 를 붙이면 지정된 클래스 패스 아래 모든 클래스에서 확인)
    - 모든 클래스를 탐색하는게 비효율적임으로 패키지를 지정해주는 게 좋음
    - 빈의 아이디는 클래스 이름에서 첫 글자를 소문자로 변경한 것
    - @Component("beanname") 으로 이름을 직접 지정할 수 있음
    - 스테레오 타입 종류 (@Repository, @Service, @Controller)
    
4. 자바 코드로 등록 : @Configuration 클래스의 @Bean 메소드
    ```java
    @Configuration
    public class AnnotatedHelloConfig {
        @Bean
        public AnnotatedHello annotatedHello() {
            return new AnnotatedHello();
        }
    }
    ```
   - @Configuration, @Bean 을 사용하는 클래스는 순수 오브젝트 팩토리 클래스라기보단 자바 코드로 표현하는 메타정보라 이해하는 것이 더 좋다.
   - XML 으로 정의하던 빈을 자바 코드로 표현함으로써 IDE, 컴파일러를 통해 타입 검증이 가능
   - 자동 완성 지원
   - 이해가 쉽다.
   - 복잡한 빈 설정, 초기화 작업이 손쉽다.
   - 같은 빈을 요청하는거라면 싱글톤을 보장함
5. 일반 빈 클래스의 @Bean 메소드
    - POJO 클래스에도 @Bean 을 사용할 수 있음
    - 싱글톤이 나닌 매번 새로운 오브젝트를 생성해줌. @Configuration 과의 차이점이다.
    - 싱글톤이 아니기에 DI 에 주의해야 한다.
    - @Bean 메소드를 통해 정의되는 빈이 클래스로 만들어지는 빈과 매우 밀접한 관계가 있는 경우, 종속적일때 사용. 빈의 존재가 노출되지 않으면서 설정 정보 
      공유할때 사용
    
# 자주사용되는 메타정보 구성 전략
1. XML 단독 사용
    - 모든 빈을 XML 에서 확인할 수 있으나 많아지면 복잡하고 번거롭기도 함
2. XML 과 빈 스캐닝 혼용
    - 애플리케이션 3계층의 핵심 로직을 담고 있는 빈 클래스를 사용할 때
    - 빈 스캐닝은 애플리케이션 컨텍스트 별로 진행되는 작업이므로 중복된 빈을 생성하지 않도록 패키지 지정을 잘 해줘야 한다.
    
# 빈 의존관계 설정 방법
1. XML :  <constructor-arg> , <property> 
    ```xml
       <bean>
           <property name="printer" ref="defaultPrinter"/>
       </bean>
   
       혹은음
   
       <bean id="hello" class="...">
           <constructor-arg index="0" value="Spring"/>
           <constructor-arg index="1" ref="porinter"/>
       </bean>
    ```
   - 위와 같은 방법으로 사용할 수 있음
   - 생성자 주입에는 파라미터 순서나 타입을 명시해야 함. 위처럼 순서를 명시한다. 생성자 파라미터의 이름을 지정하는 방법도 있음
   - 생성자 파라미터 이름을 사용하는 경우는 컴파일 과정에서 클래스 파일 디버깅 심벌을 제거하지 않을때 사용 가능
2. XML : 자동와이어링
    - XML 문서 양을 대폭 줄여줄 수 있음.
    - 이름으로 찾아주거나, 타입으로 찾아주는 자동와이어링 두가지가 존
    ```xml
       <bean id="hello" class="com.toby.vol2.Hello" autowire="byName">
           <property name="name" value="Spring"/>
       </bean>
       
       <bean id="printer" class="com.toby.vol2.StringPrinter"/>
    ```
   - Hello 클래스의 프로퍼티 이름이 printer 였고, 주입되야 할 빈 이름이 printer 이므로 byName 자동와이어링을 통해 사용
   - 동일한 이름이 없을때는 무시
   - byType 은 타입만 일치한다면 빈 이름과 상관없이 주입해준다.
   ```xml
      <bean id="hello" class="com.toby.vol2.Hello" autowire="byType">
          <property name="name" value="Spring"/>
      </bean>
      
      <bean id="defaultPrinter" class="com.toby.vol2.StringPrinter"/>
   ```
   - 같은 타입의 빈이 두 개 이상 존재하는 경우 적용되지 않는다.
   - XML 파일을 봐서 빈 사이의 의존관계 파악이 어렵다. (나는 안쓸듯)
3. XML : 네임스페이스와 전용 태그
    - 전용 태그를 참조하게 되는 경우 id 를 지정해주는 것이 원칙
4. @Resource
    - 내부 필드에 직접 DI 하는 방법, 수정자 또는 필드에서 붙일 수 있음
    - 수정자 메소드에 붙은 @Resource 는 XML 의 <property> 태그에 대응
    - @Resource 를 사용하여 DI 를 하려면 <context:annotation-config/> , <context:component-scan/> , 나
    AnnocationConfigApplicationContext , AnnotationConfigWebApplicationContxt 중 하나를 사용해야 한다.
    - 필드에 붙은 @Resource 는 참조할 빈 이름을 직접 지정 가능하며, 기본적으로 참조할 빈의 이름을 찾는데, name 엘리먼트가 비여있으면 타입으로 찾는다. 
    또한 반드시 참조할 빈이 있어야하며 없으면 에러를 발생시킨다.
5. @Autowired / @Inject
    - 기본적으로 타입에 의한 자동와이어링 방식
    - 수정자 메소드와 필드에서의 @Resource 와의 차이점은 필드나 프로퍼티 타입을 이용해 후보 빈을 탐색
    - 생성자에 @Autowired 를 통해서 필드에 중복되는 @Autowired 를 줄일 수 있음
    - 일반 메소드의 @Autowired 는 오브젝트 생성 이후에도 호출이 가능함
    - 동일한 인터페이스를 구현한 빈이 두 개 이상 존재할 때, Collection, Set, List, 배열을 사용해서 모두 DI 받을 수 있다.
    - 빈의 타입이 콜렉션인 경우는 @Autowired 사용이 불가능
    - 중복되는 타입의 빈이 2개 이상 존재하는 경우에 하나만 @Autowired 하고 싶으면 @Qualifier 를 사용해서 추가 정보를 제공한다.  (권장되는 사용 방식이 아님)
    - 이름으로 빈을 지정하려면 @Resource , 타입이라면 @Autowired
    
    
# 자바 코드로 의존 관계 설정
@Configuration, @Bean 을 이용해서 자바 코드로 빈을 등록하는 경우 빈의 의존관계 설정하는 방법
### 애노테이션에 의한 설정 @Autowired, @Resource
빈을 자바 코드로 등록하더라도 @Autowired 같은 의존 관계 설정은 빈 오브젝트 등록 이후에 후처리기에 의해 별도 진행된다.
따라서 빈 등록만 해주면 후처리기가 알아서 해줌!

### @Bean 메소드 호출
@Bean 이 붙은 메소드를 호출하는 것.
DI 할때 @Bean 메소드를 호출해서 주입하는 경우 주의해야 할 점은 단 한가지다.
해당 클래스에 @Configuration 어노테이션이 반드시 붙어야 한다.
스프링에서 특별한 방식으로 동작하여 싱글톤을 보장해주기 때문이다.

### @Bean 과 메소드 자동와이어링

@Bean 이 붙은 메소드는 기본적으로 @Autowired 가 붙은 메소드처럼 동작한다.
따라서 @Bean 메소드의 파라미터로 주입하고자 하는 레퍼런스를 넣으면 된다. 

위의 다양한 방법들 중에 스프링 부트를 사용하는 나는 애노테이션 단독으로 사용하는 방식이 제일 적합해보인다.

# 프로퍼티 값 설정 방법
스프링에서 사용하는 Bean 들을 싱글톤이므로 필드 값을 변경하면 안된다. 동시성 문제가 발생하기 떄문이다.
따라서 값을 설정한다는 것은 초기값을 설정하는 것이라 생각해야 한다.

### <property> 와 전용태그
프로퍼티 값을 설정할 때 value 값에 넣는 값이 String 이면 사실 문제가 없다.
하지만 다른 타입인 경우에는 적절환 변환이 필요하다.
스프링 컨테이너는 XMl 의 문자열로 된 값을 프로퍼티 타입으로 변환해주는 변환 서비스를 내장하고 있다.

### @Value
매번 런타임시에 값을 주입해주는 이유는 환경에 따라 달라지는 값들이 존재하기 때문이다.
그 대표적인 예로 DataSource 의 설정값들이다.

또 다른 이유로 기본값을 가지고 있지만, 이 값을 변경해서 사용하고 싶을때도 가능하다.

이를 통해서 코드를 다시 컴파일 하지 않아도 설정 값을 변경할 수 있다는 점은 매우 유용하다.

@Value 어노테이션을 사용함으로서 외부 설정값을 읽어와서 이 값에 대한 의존성을 분리시킬 수 있다.

스프링에서는 다양한 값을을 변환할 수 있는 컨버터를 제공한다. 따라서 필요에 의해서 찾아보면 될듯!

다양한 방식으로 값을 주입할 수 있으나 null 값은 별도 태그를 사용해서 주입한다.

# 프로퍼티 파일을 이용한 값 설정
스프링 애플리케이션은 POJO 클래스와 설정정보로 구성
설정 정보를 XML 로 분리해두면 빈 클래스나 의존관계 정보를 소스코드 수정 없이도 간단히 조작이 가능

빈 설정메타 정보는 애플리케이션 구조가 바뀌지 않으면 자주 변경되지 않으나,
프로퍼티 값으로 제공되는 일부 설정 정보는 동작환경에 따라 자주 변경될 수 있음.

따라서 객체 지향의 원칙에 따라서 자주 변경되는 것들은 파일로 분리하는 것이 가장 적합한 방법

# systemProperties, systemEnvironment
스프링 컨테이너가 직접 등록하는 빈 중에서 타입이 아니라 이름을 통해 접근할 수 있는 두 가지 빈이다.
각각 Properties , Map 타입이기 때문에 타입에 의한 접근 방법은 적절치 않다.

따라서 이름을 통해서 빈에 접근하게 한다.

@Resource Properties systemProperties;

로 사용하게 되면 systemProperties 빈을 통째로 가져온다.
또한 여기서 systemProperties 는 Map 연산자임으로 아래처럼 값을 가져올 수 있다.

@Value("#{systemProperties['os.name']") String osName;

systemEnvironment 도 동일하게 주입받아 사용 가능하다.

또한 두 개의 빈들은 스프링에서 자동으로 추가해줌으로 이 빈들의 이름을 사용해서 빈들 정의하면 안된다.

# 프로토 타입 빈
스프링 컨테이너에서 관리된느 빈들은 싱글톤임을 보장한다.
하지만 @Scope("prototype") 으로 만들어진 빈들은 스프링 컨테이너가 생성하고 DI 까지는 해주지만
그 이후부터 이 객체에 대해서는 스프링 컨테이너가 관리하지 않는다.

프로토타입 빈은 언제 쓰는걸까?
사용자의 요청별로 독립적인 정보나 작업 상태를 저장해둘 오브젝트를 만들때 사용한다. 

예를 들어 매번 새로운 요청에 대해서 새롭게 오브젝트를 생성하며 DI 도 함께 적용하여 사용할 수 있는 것이 바로 프로토 타입 빈이다.

특수한 목적으로 사용되는 프로토 타입 빈이기에 프로토 타입 빈 자체를 DI 에서 사용하는 것은 문제가 된다.

이 때 프로토 타입 빈 자체 또한 싱글톤으로 만들어져서 데이터가 뒤죽박죽 되는 문제점이 발생하기 때문이다.

따라서 프로토 타입 빈을 사용할때는 DL(Dependency Look up) 방식이 적합하다.

# 프로토 타입 빈 코드에서 사용하기
1. ApplicationContext, BeanFactory
    - @Autowired, @Resource 를 이용해 ApplicationContext, BeanFactory 를 직접 DI 하여 getBean 을 호출하는 방식
2. ObjectFactory, ObjectFactoryCreatingFactoryBean
3. ServiceLocatorFactoryBean
4. Method DI
    - ApplicationContext 를 직접 이용하는 방식은 스프링 API 에 의존적인 코드를 만든다. ObjectFactory, ServiceLocatorFactoryBean 은 새로운
    빈을 추가해야 된다는 번거로움이 있다. 이를 해결하기 위해서 메소드 코드 자체를 주입하는 방식을 사용하며 ㄴ된다. 이는 일정 규칙을 따르는 추상 메소드를 작성하여
    ApplicationContext 와 getBean() 메소드를 사용하여 새로운 프로토타입 빈을 가져오는 기능을 담당하는 메소드를 런타임 시에 추가해주는 기술이다.
    ```java
           abstract public ServiceRequest getServiceRequest();
           
           public void serviceRequestFormSubmit(HttpServletRequest request) {
               ServiceRequest serviceRequest = this.getServiceRequest();
               serviceRequest.setCustomerByCustomerNo(request.getParameter("custno"));
           }
    ```
   - 추상 메소드를 추가한다. 이 메소드는 팩토리 역할을 하는 메소드라 보면 된다.
4. Provider<T>
    - @Inject 와 함께 추가된 표준 인터페이스인 Provider 는 <T> 타입 파라미터와 get() 이라는 팩토리 메소드를 가진 인터페이스이다.
    - 빈을 등록해주지 않아도 되며 단순히 DI 되도록 지정하면 스프링이 자동으로 Provider 를 구현한 오브젝트를 생성해 주입해준다.
    ```java
       @Inject Provider<ServiceRequest> serviceRequestProvider;
       
       public void serviceRequestFormSubmit(HttpServletRequest request) {
           ServiceRequest serviceRequest = this.serviceRequestProvider.get();
           serviceRequest.setCustomerByCustomerNo(request.getParameter("custno"));
       }
    ``` 
   
### 정리
프로토타입 빈을 DL 방식으로 사용할 때는 ApplicationContext 처럼 무거운 스프링 컨테이너 API를 직접 사용하는 방식은 피해야 한다.



# 스코프
스프링은 싱글톤, 프로토타입 외 요청, 세션, 글로벌 세션, 애플리케이션이라는 네가지 스코프를 기본적으로 제공한다.

또한 요청, 세션, 글로벌세션, 애플리케이션 스코프는 모두 웹 환경에서만 의미가 있다.

애플리케이션을 제외한 세가지 스코프는 싱글톤과 다르게 독립적인 상태를 저장해두고 사용하는 데 필요

서버에서 만들어지는 빈 오브젝트에 상태를 저장해둘 수 있는 이뉴느 사용자마다 빈이 만들어지는 덕분

### 요청 스코프
하나의 웹 요청 안에서 만들어지고 해당 요청이 끝날 때 제거

각 요청별 독립적인 빈이 생성되기에 빈 오브젝트에 상태값 저장해도 안전

### 세션 스코프, 글로벌세션 스코프
HTTP 세션과 같은 존재 범위를 갖는 빈으로 만들어주는 스코프
HTTP 세션은 사용자별로 만들어지고 브라우저를 닫거나 세션 탕미이 종료될 때까지 유지되기 때문에 로그인 정보나 사용자별 선택 옵션 등을 저장하기 유용

### 애플리케이션 스코프
서블릿 컨텍스트에 저장되는 빈 오브젝트, 싱글톤 스코프와 비슷한 존재 범위
따라서 상태값을 저장하지 않도록 만들어야 함.

## 스코프 빈의 사용 방법

애플리케이션 스코프를 제외하고 나머지는 한 개 이상의 빈 오브젝트가 생성된다.
프로토 타입과는 다르게 스프링이 생성부터 초기화, DI, DL 그리고 제거까지 전 과정을 관리
그래서 언제 만들어지고 사용되는지 파악할 수 있음

하나 이상의 오브젝트가 생성됨으로 DI 해서 사용할 수 없음.

그래서 프로토 타입 빈처럼 Provider, ObjectFactory 같은 DL 방식을 사용해야 함.

### 세션 스코프 빈
세션 스코프 빈을 사용해보자.
```java
@Scope("session")
public class LoginUser {
    String loginId;
    String name;
    Date loginTime;
}
```

그리고 이를 서비스 계층에서 사용해보자

```java
public class LoginService {
    @Autowired Provider<LoginUser> loginUserProvider;
    
    public void  login(Login login) {
        LoginUser loginUser = loginUserProvider.get();
        loginUser.setLoginId(...);
        loginUser.setName(...);
        ...
    }
}
```

위 처럼 사용할 수 있을 것이다. 이는 Provider 를 사용하는 DL 방식이다.

그럼 DI 방식으로 사용할 수 없나?

DI 방식을 사용하려면 프록시의 도움이 필요하다.

이는 proxyMode 엘리먼트 지정을 통해서 가능하다.

그럼 client -> Scoped Proxy --> Session Scope Objects

와 같은 구조로 사용할 수 있게 된다.

여기서 client 를 LoginService 빈이라고 생각하면 싱글톤이므로 세션 스코프 빈을 DI 받더라도 사용자마다 다른 정보를 저장할 방법이 없다.

그렇기에 스코프 프록시의 도움이 필요한 것이고 스포크 프록시는 각 요청에 연결된 HTTP 세션 정보를 참고해서 사용자마다 다른 LoginUser 오브젝트를

사용하게 해준다.

LoginService 즉 클라이언트 입장에서 같은 오브젝트를 사용하는 것 같지만, 사실 스코프 프록시를 통해서 LoginUser 오브젝트로 클라이언트 호출을

해줌으로써 각각 다른 오브젝트를 사용할 수 있게 함.


아래처럼 사용이 가능

```java
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class LoginUser {}
```

클라이언트에서는 DI 해서 사용하면 됌
```java
public class LoginService {
    @Autowired LoginUser loginUser;
    
    public void login(Login login) {
        this.loginUser.setLoginId(...);
    }
}
```

장점은 스코프 빈이지만 마치 싱글톤 빈처럼 사용할 수 있게 해주는 것

# 빈 식별자
빈을 식별하기 위한 두 가지 방법
1. id
2. name

id 는 제약 조건이 있다.
name 은 제약 조건이 없으며, 여러 개의 이름을 가지도록 콤마, 세미콜론으로 지정할 수 있다.

이름을 여러개로 지정해서 얻는 장점?
사용하는 환경마다 빈의 목적성을 이름에 부여하는 경우가 있음.
이럴때 다른 이름으로 빈을 참조함으로써 달성할 수 있음.

XML , 어노테이션 두 방법으로 모두 가능함.

# 애노테이션 빈 이름 지정 방법

해당 클래스가 빈 스캐너로 자동 등록되는 경우

@Component
public class UserService {

이 경우는 userService 가 빈의 이름이 된다.

@Configuration 애노테이션에서 @Bean 메소드를 이용하는 경우

@Configuration
public class Config {
    @Bean
    public UserDao userDao() {
    
이 경우는 메소드의 이름이 빈의 이름이 된다. userDao

직접 빈 이름 지정하는 방법

자동 스캐닝 대상이라면

@Component("beanName")
public class UserService {

디폴트 엘리먼트 값 지정으로 빈 이름을 지정할 수 있음

@Named 어노테이션

@Component
@Named("beanName")
public class UserService {


@Bean 메소드의 경우

@Configuration
public class Config {
    @Bean(name="beanName")
    public UserDao userDao() {
    
name 엘리먼트에 값 지정으로 사용한다.

여러 개의 이름을 등록하는 방법은

@Bean(name={"name1", "name2"}) 

와 같은 방식으로 여러 개의 이름을 지정할 수 있음.

@Named 와 스테레오 타입 애노테이션을 동시에 사용할때 한 가지 이름만 사용하도록 해야 함.

# 빈 생명주기 메소드

## 초기화 메소드
빈 오브젝트가 생성되고 DI 까지 끝낸 다음 실행되는 메소드를 말함
이 시점에 초기화해야 되는 경우가 있을 때 사용.

4가지 방법이 있음.
1. 초기화 콜백 인터페이스 : InitializingBean 인터페이스를 구현해서 빈을 작성, 권장하지 않음
2. init-method 지정 : XML 로 빈 등록하는 경우 <bean id ...  init-method="initResource"/> 로 실행할 메소드 등록, 코드에 안보여 헤깔
3. @PostConstruct : 초기화 담당 메소드 코드에 @PostConstruct 부여해주면 됨. 제일 권장되는 방식 
4. @Bean(init-method) : @Bean(init-method:"initResource") 로 직접 지정

## 제거 메소드
컨테이너가 종료될 때 호출돼서 빈이 사용한 리소스를 반환하거나 종료 전에 처리해야 할 작업 수행

3가지 방법
1. 제거 콜백 인터페이스 : DisposableBean 인터페이스 구현해서 destroy() 를 구현하는 방법. API 에 종속되는 코드가 생겨서 비추
2. destroy-method : 빈 태그에 메소드를 지정하는 방식. 코드에 안보여서 별로.
3. @PreDestroy : 제거 메소드에 어노테이션 부여
4. @Bean(destroyMethod) : 초기화 메소드 4번과 동일

 
## <context:annotation-config />
@Configuration 과 @Bean 을 통해서 해당하는 클래스와 @Bean 이 있는 메소드를 빈으로 만들어준다 했다.
하지만 이는 스프링 IoC/DI 가 하는 것이 아니라 <context:annotation-config> 태그를 처리하는 핸들러를 통해 특정 빈이 등록되게 한다.
이 과정에서 등록되는 빈은 스프링 컨테이너를 확장해서 빈의 등록과 관계 설정, 후처리 등에 새로운 기능을 부여하는 빈이다.

## <context:component-scan base-package=".." />
이 태그는 annotation-config 태그를 포함한다.

# 빈 역할과 역사
스프링에서는 크게 3가지로 빈의 역할을 분류한다.
애플리케이션 로직 빈, 애플리케이션 인프라 빈, 컨테이너 인프라 빈으로 구성한다.

여기서 3.0 버전 이전에는 컨테이너 인프라 빈를 자바 코드로 등록할 수 없었는데 3.1로 들어서면서

**모든 빈들을 자바 코드로 정의하는게 가능해졌다.**

# @ComponentScan

컴포넌트 스캔으로 @Component 어노테이션을 가지고 있는 클래스를 빈으로 등록해준다.

여기서 주의할 점은 @Configuration 을 포함하고 있는 클래스 또한 @Component 를 가지고 있기때문에

자신 또한 스캐닝의 대상이 된다. 중복된 빈 등록이 되면 안됨으로 이때는 @ComponentScan 의 엘리먼트인 excludeFilter 를 사용하여 @Configuration

어노테이션이 붙은 클래스를 제외할 수 있다. (eg. @ComponentScan(basePackage="myproject", excludeFilter=@Filter(Configuration.class)))

혹은 자기 자신만 빼주고 싶다면 excludeFilter=@Filter(type=FilterType.ASSIGNABLE_TYPE, value=MyClass.class) 를 사용하면 된다.

# @Import

@Configuration 클래스에 빈 메타정보를 추가할 때 사용

예를 들어 데이터 엑세스 기술 관련 빈들만 모여있는 DataConfig 클래스를 AppConfig 클래스에서 사용하고자 할때

@Import(DataConfig.class) 처럼 사용 가능

# @ImportResource
XML 에서 사용되던 주요한 전용 태그를 자바 클래스에서 애노테이션과 코드로 대체할 수 있게 해줌
@ImportResource("location") 으로 사용하면 된다.

# @EnableTransactionManagement

@Configuration 클래스에서 사용할 수 있는 어노테이션으로 트랜잭션 속성을 지정할 수 있게 해주는 AOP 관련 빈들을 등록해주는 것.

# 웹 어플리케이션 IoC 컨테이너 구성
웹 환경에서 보통 루트 애플리케이션 컨텍스트와 서블릿 애플리케이션 컨텍스트를 두 단계로 분리해 사용하는 것이 일반적.

루트 컨텍스트와 서블릿 컨텍스트는 각각 web.xml의 <listener> 와 <servlet> 에 컨텍스트의 설정 관련 정보를 정어서 웹 애플리케이션 시작에 만들게 함

두 애플리케이션 컨텍스트가 사용하는 기본 메타정보는 XMl

## 루트 애플리케이션 컨텍스트 등록
<listener> 를 등록해주면 디폴트 컨텍스트 클래스인 XmlWebApplicationContext 를 이용해 애플리케이션 컨텍스트를 만들고 

/WEB-INF/applicationContext.xml 을 설정 파일로 사용함.

<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>

xml 파일 대신 @Configuration 클래스로 루트 애플리케이션 컨텍스트를 생성할 수도 있음.
이때는 contextClass, contextConfigLocation 파라미터가 필요함

루트 애플리케이션 컨텍스트는 contextClass 컨텍스트 파라미터로 컨텍스트 클래스 지정이 가능

<context-param>
    <param-name>contextClass</param-name>
    <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
</context-param>


# 1장 마무리.

모르던 내용 위주로 정리해서 공부했음. 
끝
