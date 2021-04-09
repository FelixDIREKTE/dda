package com.dd.dda.repository;

import com.dd.dda.model.sqldata.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository  extends JpaRepository<Party, Long> {
}
