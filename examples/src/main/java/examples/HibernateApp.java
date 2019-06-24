/**
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package examples;

import examples.jpa.Person;
import io.jooby.Jooby;
import io.jooby.hibernate.Hbm;
import io.jooby.hibernate.TransactionalRequest;
import io.jooby.hikari.Hikari;
import io.jooby.json.Jackson;

import javax.persistence.EntityManager;
import java.util.List;

public class HibernateApp extends Jooby {
  {
    install(new Hikari("mem"));
    install(new Hbm());
    install(new Jackson());

    decorator(new TransactionalRequest());

    get("/create", ctx -> {
      EntityManager em = require(EntityManager.class);
      Person p = new Person();
      em.persist(p);
      return p;
    });

    get("/people", ctx -> {

      EntityManager em = require(EntityManager.class);

      List<Person> people = em.createQuery("from Person", Person.class).getResultList();

      return people;
    });
  }

  public static void main(String[] args) {
    runApp(args, HibernateApp::new);
  }
}