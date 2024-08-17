package com.fh.scm.repository.implement;

import com.fh.scm.enums.ShipmentStatus;
import com.fh.scm.pojo.Shipment;
import com.fh.scm.repository.ShipmentRepository;
import com.fh.scm.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Repository
@Transactional
@RequiredArgsConstructor
public class ShipmentRepositoryImplement implements ShipmentRepository {

    private final LocalSessionFactoryBean factory;

    private Session getCurrentSession() {
        return Objects.requireNonNull(this.factory.getObject()).getCurrentSession();
    }

    @Override
    public Shipment get(UUID id) {
        Session session = this.getCurrentSession();

        return session.get(Shipment.class, id);
    }

    @Override
    public void insert(Shipment shipment) {
        Session session = this.getCurrentSession();
        session.save(shipment);
    }

    @Override
    public void update(Shipment shipment) {
        Session session = this.getCurrentSession();
        session.update(shipment);
    }

    @Override
    public void delete(UUID id) {
        Session session = this.getCurrentSession();
        Shipment shipment = session.get(Shipment.class, id);
        session.delete(shipment);
    }

    @Override
    public void softDelete(UUID id) {
        Session session = this.getCurrentSession();
        Shipment shipment = session.get(Shipment.class, id);
        shipment.setActive(false);
        session.update(shipment);
    }

    @Override
    public void insertOrUpdate(Shipment shipment) {
        Session session = this.getCurrentSession();
        session.saveOrUpdate(shipment);
    }

    @Override
    public Long count() {
        Session session = this.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Shipment> root = criteria.from(Shipment.class);

        criteria.select(builder.count(root));
        Query<Long> query = session.createQuery(criteria);

        return query.getSingleResult();
    }

    @Override
    public Boolean exists(UUID id) {
        Session session = this.getCurrentSession();
        Shipment shipment = session.get(Shipment.class, id);

        return shipment != null;
    }

    @Override
    public List<Shipment> getAll(Map<String, String> params) {
        Session session = this.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Shipment> criteria = builder.createQuery(Shipment.class);
        Root<Shipment> root = criteria.from(Shipment.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("isActive"), true));

        if (params != null) {
            String trackingNumber = params.get("trackingNumber");
            if (trackingNumber != null && !trackingNumber.isEmpty()) {
                predicates.add(builder.like(root.get("trackingNumber"), UUID.fromString(trackingNumber).toString()));
            }

            String statusStr = params.get("status");
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    ShipmentStatus status = ShipmentStatus.valueOf(statusStr.toUpperCase(Locale.getDefault()));
                    predicates.add(builder.equal(root.get("status"), status));
                } catch(IllegalArgumentException e) {
                    LoggerFactory.getLogger(ShipmentRepositoryImplement.class).error("An error parse CriteriaType Enum", e);
                }
            }
        }

        criteria.select(root).where(predicates.toArray(Predicate[]::new));
        Query<Shipment> query = session.createQuery(criteria);
        Pagination.paginator(query, params);

        return query.getResultList();
    }
}
