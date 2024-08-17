package com.fh.scm.repository.implement;

import com.fh.scm.pojo.InventoryDetails;
import com.fh.scm.repository.InventoryDetailsRepository;
import com.fh.scm.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.Query;
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
public class InventoryDetailsRepositoryImplement implements InventoryDetailsRepository {

    private final LocalSessionFactoryBean factory;

    private Session getCurrentSession() {
        return Objects.requireNonNull(factory.getObject()).getCurrentSession();
    }

    @Override
    public InventoryDetails get(UUID id) {
        Session session = this.getCurrentSession();

        return session.get(InventoryDetails.class, id);
    }

    @Override
    public void insert(InventoryDetails inventoryDetails) {
        Session session = this.getCurrentSession();
        session.save(inventoryDetails);
    }

    @Override
    public void update(InventoryDetails inventoryDetails) {
        Session session = this.getCurrentSession();
        session.update(inventoryDetails);
    }

    @Override
    public void delete(UUID id) {
        Session session = this.getCurrentSession();
        InventoryDetails inventoryDetails = session.get(InventoryDetails.class, id);
        session.delete(inventoryDetails);
    }

    @Override
    public void softDelete(UUID id) {
        Session session = this.getCurrentSession();
        InventoryDetails inventoryDetails = session.get(InventoryDetails.class, id);
        inventoryDetails.setActive(false);
        session.update(inventoryDetails);
    }

    @Override
    public void insertOrUpdate(InventoryDetails inventoryDetails) {
        Session session = this.getCurrentSession();
        session.saveOrUpdate(inventoryDetails);
    }

    @Override
    public Long count() {
        Session session = this.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<InventoryDetails> root = criteria.from(InventoryDetails.class);

        criteria.select(builder.count(root));
        Query<Long> query = session.createQuery(criteria);

        return query.getSingleResult();
    }

    @Override
    public Boolean exists(UUID id) {
        Session session = this.getCurrentSession();
        InventoryDetails inventoryDetails = session.get(InventoryDetails.class, id);

        return inventoryDetails != null;
    }

    @Override
    public List<InventoryDetails> getAll(Map<String, String> params) {
        Session session = this.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<InventoryDetails> criteria = builder.createQuery(InventoryDetails.class);
        Root<InventoryDetails> root = criteria.from(InventoryDetails.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("isActive"), true));

        if (params != null) {
            String productId = params.get("productId");
            if (productId != null && !productId.isEmpty()) {
                predicates.add(builder.equal(root.get("product").get("id"), UUID.fromString(productId)));
            }

            String inventoryId = params.get("inventoryId");
            if (inventoryId != null && !inventoryId.isEmpty()) {
                predicates.add(builder.equal(root.get("inventory").get("id"), UUID.fromString(inventoryId)));
            }
        }

        criteria.select(root).where(predicates.toArray(Predicate[]::new));
        Query<InventoryDetails> query = session.createQuery(criteria);
        Pagination.paginator(query, params);

        return query.getResultList();
    }
}
