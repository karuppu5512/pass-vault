package com.pamvault.dao;

import com.pamvault.entity.PasswordHistory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PasswordHistoryDao extends BaseDao<PasswordHistory> {

    public PasswordHistoryDao() {
        super(PasswordHistory.class);
    }

    public List<PasswordHistory> findByAccount(Long accountId) {
        return query("from PasswordHistory h where h.account.id = :accountId order by h.changedAt desc, h.id desc")
                .setParameter("accountId", accountId)
                .list();
    }
}
