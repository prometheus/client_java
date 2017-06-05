package io.prometheus.client.spring.hibernate;

import io.prometheus.client.hibernate.HibernateStatisticsCollector;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class SimpleClientHibernateApplicationListenerTest {
  @Mock
  private HibernateStatisticsCollector collector;

  @Mock
  private EntityManager entityManager;

  @Mock
  private Session session;

  @Mock
  private SessionFactory sessionFactory;

  @Mock
  private ApplicationReadyEvent event;

  @Before
  public void setUp() throws Exception {
    initMocks(this);

    when(entityManager.getDelegate()).thenReturn(session);

    when(session.getSessionFactory()).thenReturn(sessionFactory);
  }

  @Test
  public void onApplicationEventWithEventManagerAndAppName() throws Exception {
    (new SimpleClientHibernateApplicationListener(collector, entityManager, "MySpringApp"))
        .onApplicationEvent(event);

    verify(collector).add(sessionFactory, "MySpringApp");
    verify(collector).register();
  }

  @Test
  public void onApplicationEventWithoutEventManager() throws Exception {
    (new SimpleClientHibernateApplicationListener()).onApplicationEvent(event);
  }
}
