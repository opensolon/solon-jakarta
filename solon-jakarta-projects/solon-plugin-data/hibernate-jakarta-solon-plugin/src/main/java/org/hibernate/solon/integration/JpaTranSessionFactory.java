package org.hibernate.solon.integration;

import jakarta.persistence.metamodel.Metamodel;
import org.hibernate.*;
import org.hibernate.Cache;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.graph.RootGraph;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.relational.SchemaManager;
import org.hibernate.stat.Statistics;
import org.noear.solon.data.tran.TranListener;
import org.noear.solon.data.tran.TranUtils;

import jakarta.persistence.*;
import jakarta.persistence.Query;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JpaTranSessionFactory（通过静态代理实现事务管理）
 *
 * @author noear
 * @since 2.5
 */
public class JpaTranSessionFactory implements SessionFactory {
    private SessionFactory real;

    public JpaTranSessionFactory(SessionFactory real) {
        this.real = real;
    }

    private <T extends EntityManager> T tranTry(T entityManager){
        if(TranUtils.inTrans()){
            EntityTransaction transaction = entityManager.getTransaction();

            if(transaction.isActive() == false) {
                transaction.begin();

                TranUtils.listen(new TranListener() {
                    @Override
                    public void beforeCommit(boolean readOnly) throws Throwable {
                        if (readOnly) {
                            transaction.setRollbackOnly();
                        }
                        transaction.commit();
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status == TranListener.STATUS_ROLLED_BACK) {
                            transaction.rollback();
                        }
                    }
                });
            }
        }

        return entityManager;
    }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        return real.getSessionFactoryOptions();
    }

    @Override
    public SessionBuilder withOptions() {
        return real.withOptions();
    }

    @Override
    public Session openSession() throws HibernateException {
        return tranTry(real.openSession());
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return tranTry(real.getCurrentSession());
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        return real.withStatelessOptions();
    }

    @Override
    public StatelessSession openStatelessSession() {
        return real.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
        return real.openStatelessSession(connection);
    }

    @Override
    public Statistics getStatistics() {
        return real.getStatistics();
    }

    @Override
    public SchemaManager getSchemaManager() {
        return real.getSchemaManager();
    }

    @Override
    public HibernateCriteriaBuilder getCriteriaBuilder() {
        return real.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return real.getMetamodel();
    }

    @Override
    public void close() throws HibernateException {
        real.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return real.getProperties();
    }

    @Override
    public boolean isClosed() {
        return real.isClosed();
    }

    @Override
    public Cache getCache() {
        return real.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return real.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        real.addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return real.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        real.addNamedEntityGraph(graphName, entityGraph);
    }

    @Override
    public Set getDefinedFilterNames() {
        return real.getDefinedFilterNames();
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return real.getFilterDefinition(filterName);
    }

    @Override
    public Set<String> getDefinedFetchProfileNames() {
        return real.getDefinedFetchProfileNames();
    }

    @Override
    public boolean containsFetchProfileDefinition(String name) {
        return real.containsFetchProfileDefinition(name);
    }

    @Override
    public Reference getReference() throws NamingException {
        return real.getReference();
    }

    @Override
    public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) {
        return real.findEntityGraphsByType(entityClass);
    }

    @Override
    public RootGraph<?> findEntityGraphByName(String s) {
        return real.findEntityGraphByName(s);
    }

    @Override
    public EntityManager createEntityManager() {
        return tranTry(real.createEntityManager());
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return tranTry(real.createEntityManager(map));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return tranTry(real.createEntityManager(synchronizationType));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return tranTry(real.createEntityManager(synchronizationType, map));
    }


    @Override
    public boolean isOpen() {
        return real.isOpen();
    }
}