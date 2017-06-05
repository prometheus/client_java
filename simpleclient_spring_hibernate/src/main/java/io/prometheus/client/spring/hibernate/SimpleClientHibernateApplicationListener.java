package io.prometheus.client.spring.hibernate;

import io.prometheus.client.hibernate.HibernateStatisticsCollector;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class SimpleClientHibernateApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
  @Autowired
  private EntityManager entityManager;

  @Value("${spring.application.name:bootstrap}")
  private String appName = "bootstrap";

  private HibernateStatisticsCollector collector = new HibernateStatisticsCollector();

  public SimpleClientHibernateApplicationListener() {

  }

  public SimpleClientHibernateApplicationListener(
      HibernateStatisticsCollector collector,
      EntityManager entityManager,
      String appName
  ) {
    this.collector = collector;
    this.entityManager = entityManager;
    this.appName = appName;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    if (entityManager == null) {
      return;
    }

    Session session = (Session) entityManager.getDelegate();

    collector.add(session.getSessionFactory(), appName);
    collector.register();
  }
}
