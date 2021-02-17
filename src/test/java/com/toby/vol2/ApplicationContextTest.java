package com.toby.vol2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApplicationContextTest {

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

    @Test
    public void genericApplicationContext() {
        // XML 파일과 같은 외부 리소스에 있는 빈 설정 메타정보를 리더를 통해 읽어들여서 메타정보로 전환해서 사용하는 애플리케이션 컨텍스트
        GenericApplicationContext ac = new GenericApplicationContext();
        //XML 로 작성된 빈 설정정보를 읽어서 컨테이너에게 전달하는 대표적인 빈 설정정보 리더
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
        // 클래스 패스 경로에 있는 applicationContext.xml 을 읽음
        reader.loadBeanDefinitions("applicationContext.xml");

        // XmlBeanDefinitionReader 가 포함되어 있는 GenericXmlApplicationContext 를 사용해 한줄에 끝낼수도 있다.
        // GenericApplicationContext ac = new GenericXmlApplicationContext("applicationContext.xml");

        // 모든 메타 정보가 등록이 완료되었으니 애플리케이션 컨테이너를 초기화하라
        ac.refresh();

        Hello hello = ac.getBean("hello", Hello.class);
        hello.print();

        assertEquals("Hello Spring", ac.getBean("printer").toString());

        /**
         * 이 방식을 통해서 달성할 수 있는 것
         * 빈 설정 정보가 어디있던 그 위치에 리소스만 가지고 온다면 설정 정보로 사용할 수 있다는 것
         */
    }

    /**
     * 부모 자식 컨텍스트 계층 테스트
     *
     * 자식 설정 파일에는 Printer Bean 을 생성하지 않았습니다.
     * 계층을 통해서 부모의 Printer Bean 찾도록 합니다.
     *
     */
    @Test
    public void contextHierarchyTest() {
        // 부모 컨텍스트 생성
        ApplicationContext parent = new GenericXmlApplicationContext("parentContext.xml");
        // 자식이 부모와 계층을 맺게 지정
        GenericApplicationContext child = new GenericApplicationContext(parent);
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(child);
        // 자식 설정 컨텍스트 읽어오게 함
        reader.loadBeanDefinitions("childContext.xml");

        // 컨텍스트 초기화
        child.refresh();

        // printer bean 얻기
        Printer printer = child.getBean("printer", Printer.class);
        assertNotNull(printer);

        Hello hello = child.getBean("hello", Hello.class);
        hello.print();

        // 자식의 Hello 를 사용하면 Hello Child 가 나와야 함, 부모 컨텍스트 빈과 중복되면 자식 컨텍스트가 우선됌
        assertEquals("Hello Child", printer.toString());

    }

    @Test
    public void simpleBeanScanningTest() {
        ApplicationContext ctx = new AnnotationConfigApplicationContext("com.toby.vol2");
        AnnotatedHello hello = ctx.getBean("annotatedHello", AnnotatedHello.class);
        assertNotNull(hello);
    }

    @Test
    public void configurationContextTest() {
        // 빈 스캐닝 기능이 있는 컨텍스트를 사용해서 해당 클래스에 빈을 등록해줌
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotatedHelloConfig.class);
        AnnotatedHello hello = ctx.getBean("annotatedHello", AnnotatedHello.class);
        assertNotNull(hello);

        // @Configuration 어노테이션이 붙은 클래스 또한 Bean 으로 생성된다.
        AnnotatedHelloConfig config = ctx.getBean("annotatedHelloConfig", AnnotatedHelloConfig.class);
        assertNotNull(config);
    }
}
