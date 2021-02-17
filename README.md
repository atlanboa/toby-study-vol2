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