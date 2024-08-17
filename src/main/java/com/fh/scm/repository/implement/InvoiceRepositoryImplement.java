package com.fh.scm.repository.implement;

import com.fh.scm.pojo.Invoice;
import com.fh.scm.repository.InvoiceRepository;
import com.fh.scm.util.Pagination;
import com.fh.scm.util.Utils;
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
import java.time.LocalDateTime;
import java.util.*;

@Repository
@Transactional
@RequiredArgsConstructor
public class InvoiceRepositoryImplement implements InvoiceRepository {

    private final LocalSessionFactoryBean factory;

    private Session getCurrentSession() {
        return Objects.requireNonNull(factory.getObject()).getCurrentSession();
    }

    @Override
    public Invoice get(UUID id) {
        Session session = getCurrentSession();

        return session.get(Invoice.class, id);
    }

    @Override
    public void insert(Invoice invoice) {
        Session session = getCurrentSession();
        session.save(invoice);
    }

    @Override
    public void update(Invoice invoice) {
        Session session = getCurrentSession();
        session.update(invoice);
    }

    @Override
    public void delete(UUID id) {
        Session session = getCurrentSession();
        Invoice invoice = session.get(Invoice.class, id);
        session.delete(invoice);
    }

    @Override
    public void softDelete(UUID id) {
        Session session = getCurrentSession();
        Invoice invoice = session.get(Invoice.class, id);
        invoice.setActive(false);
        session.update(invoice);
    }

    @Override
    public void insertOrUpdate(Invoice invoice) {
        Session session = getCurrentSession();
        session.saveOrUpdate(invoice);
    }

    @Override
    public Long count() {
        Session session = this.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Invoice> root = criteria.from(Invoice.class);

        criteria.select(builder.count(root));
        Query<Long> query = session.createQuery(criteria);

        return query.getSingleResult();
    }

    @Override
    public Boolean exists(UUID id) {
        Session session = this.getCurrentSession();
        Invoice invoice = session.get(Invoice.class, id);

        return invoice != null;
    }

    @Override
    public List<Invoice> getAll(Map<String, String> params) {
        Session session = getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Invoice> criteria = builder.createQuery(Invoice.class);
        Root<Invoice> root = criteria.from(Invoice.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("isActive"), true));

        if (params != null) {
            Arrays.asList("isPaid", "taxId", "paymentTermsId", "fromCreatedAt", "toCreatedAt").forEach(key -> {
                if (params.containsKey(key) && !params.get(key).isEmpty()) {
                    switch (key) {
                        case "isPaid":
                            Boolean isPaid = Utils.parseBoolean(params.get(key));
                            if (isPaid != null) {
                                predicates.add(builder.equal(root.get("isPaid"), isPaid));
                            }
                            break;
                        case "taxId":
                            predicates.add(builder.equal(root.get("tax").get("id"), UUID.fromString(params.get(key))));
                            break;
                        case "paymentTermsId":
                            predicates.add(builder.equal(root.get("paymentTerms").get("id"), UUID.fromString(params.get(key))));
                            break;
                        case "fromCreatedAt":
                            LocalDateTime fromCreatedAt = Utils.parseDate(params.get(key));
                            predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), fromCreatedAt));
                            break;
                        case "toCreatedAt":
                            LocalDateTime toCreatedAt = Utils.parseDate(params.get(key));
                            predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), toCreatedAt));
                            break;
                    }
                }
            });
        }

        criteria.select(root).where(predicates.toArray(Predicate[]::new));
        Query<Invoice> query = session.createQuery(criteria);
        Pagination.paginator(query, params);

        return query.getResultList();
    }
}
